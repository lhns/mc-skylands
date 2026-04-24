# Beanstalk generation — step by step

How a planted bean turns into a wobbly, sky-spanning trunk that bridges the
Overworld into Skylands. Direct port of 1.12.2
`org.lolhens.skylands.generator.BeanstalkGenerator` by lhns & Th3Falc0n;
the structural choices below preserve the original behaviour, and the
adjustments made during the port (dirt-encasement gate, downward roots,
NBT persistence) are called out as separate sections at the end.

Files referenced:

- `common/src/main/scala/skylands/blockentity/BeanPlantBlockEntity.scala`
- `common/src/main/scala/skylands/worldgen/BeanstalkGenerator.scala`
- `common/src/main/scala/skylands/worldgen/BlockArray.scala`
- `common/src/main/scala/skylands/worldgen/CloudGenerator.scala`
- `common/src/main/scala/skylands/block/BeanstalkBlock.scala`

---

## 1. The 1.12.2 algorithm, annotated

This is the shape of the original generator (lhns's 2017 comments kept
verbatim, trimmed for Scala-3 / 1.21 syntax):

```scala
class BeanstalkGenerator(world, position):

  // join overworld + skylands along Y so every write propagates the seam
  val syncedWorld = BlockArray.syncVertical(overworld, skylands, overlap)

  // 7-octave sine wobble, symmetric amplitudes, geometric octave decrease
  val sineLayerAmplitudes = tabulate(7)(i =>
    (rand - 0.5) * 2 * amplitudeMax * (1 / octaveDecrease^i))
  val sineLayerOffset     = tabulate(7)(i =>
    rand * 2π * (minSineFreqDivider - sineFreqDividerDecrease^i))

  var progress     = 0
  var lastBlockPos = position

  def drawBlock(pos, state) =
    // only overwrite replaceable or terrain (grass/dirt/stone) blocks
    if syncedWorld.isReplaceable(pos) || syncedWorld.isTerrainBlock(pos):
      syncedWorld.setBlockState(pos, state)

  def drawLayer(destinationPosition) =
    // offset block by 1 in Y and in XZ plane by a random amount
    // weighted by the XZ growth direction
    val dir  = destinationPosition - lastBlockPos
    val rd   = dir + (rand ± maxSingleOffsetX/2, 0, rand ± maxSingleOffsetZ/2)
    val len  = sqrt(max(rd.x², rd.y², rd.z²))
    val step = (round(rd.x/len), 1, round(rd.z/len))
    lastBlockPos += step

    // place stem center (at XZ offset above the last one)
    drawBlock(lastBlockPos, stem.withCenter(true))

    // steps decides the stem thickness
    def recursiveFunction(currentPos, steps = 0):
      if steps > 600 then return
      val size = log1p(steps * 1.4).toInt

      // draw stem disk around stem center —
      // won't override existing stem blocks and will therefore leave
      // stem center untouched
      for x,z in [-size, size]²:
        if BlockPos(x,0,z).inSphere(size + 0.1):
          drawBlock(currentPos + (x,0,z), stem.default)

      // don't execute this if it was called from this
      // "thickness increase" recursion call
      if currentPos.y >= position.y:
        // find bean stalk center below and increase the stalks thickness
        for x,z in [-1, 1]²:
          val below = currentPos + (x, -1, z)
          if syncedWorld.getBlockState(below).isBeanCenter && below ≠ currentPos:
            // "thickness increase" recursion call
            recursiveFunction(below, steps + 1)

    // draw the layer above
    recursiveFunction(lastBlockPos)

    if lastBlockPos.y ∈ [245, 255]:
      new CloudGenerator(world, lastBlockPos)

  override def update() =
    // generate spiralling offset (Th3Falc0n)
    val xOffset = Σ_i sin(progress / (minSineFreqDivider - sineFreqDividerDecrease^i)
                          + offset_i) * amp_i
    val zOffset = Σ_i cos(progress / (minSineFreqDivider - sineFreqDividerDecrease^i)
                          + offset_i) * amp_i
    val destinationPosition = position + (xOffset, progress + 3, zOffset)

    // self destruct bean block after growing to 430 blocks tall
    if destinationPosition.y > 430:
      world.setBlockState(position, stem.default)

    drawLayer(destinationPosition)
    progress += 1    // increment y
```

Conceptually this is four pieces:

1. **The wobble shape** — 7 sine octaves seeded once per beanstalk from
   `scala.util.Random`. Amplitudes decrease geometrically with `i`, so
   octave 0 gives the big lazy arc and higher octaves add small jitter.
2. **The destination per tick** — a point `(progress + 3)` blocks above
   the bean, pushed sideways by the sine sum. `progress` doubles as tick
   count and sine phase.
3. **`drawLayer`** — step one block toward the destination with a random
   ±15 jitter on X/Z; write a CENTER stem at the new tip; then thicken
   the trunk down from the tip via a recursive shell pass.
4. **Cloud spawns at Y ∈ [245, 255]** — every time the tip crosses the
   seam band it instantiates a `CloudGenerator`, which scatters cloud
   clusters once in its constructor.

---

## 2. The synced `BlockArray`

All writes go through `BlockArray.syncVertical`:

- `y <= 255` → Overworld.
- `y > 255` → Skylands, offset by `overlap - 255` on Y.
- Writes in the overlap band (`y ∈ [255 - overlap, 255]`, `overlap = 15`)
  hit **both** levels so block changes stay consistent across the seam.

If Skylands is unavailable (`SkylandsWorldgen.SKYLANDS_LEVEL` resolves to
`null`), the generator falls back to a single-level `BlockArray` — the
stalk still grows, just without the sky-side mirror.

`isReplaceable` (`air | canBeReplaced | leaves | saplings | vine | cloud`)
and `isTerrainBlock` (`grass | dirt | stone`) are the two predicates that
`drawBlock` consults before it touches a cell.

---

## 3. Trunk thickening — reading the recursion

The recursive shell pass is the part most worth re-reading. Call it with
`steps = 0` at the freshly-placed tip:

- Draw a disk of `stem.default` blocks around the tip, radius
  `log1p(steps · 1.4)`. `steps = 0` → disk of size 0 (just the tip). Far
  down the trunk (`steps` large) → fat column.
- For each of the 8 surrounding cells one block below the current pos:
  if it's an existing CENTER stem (previous layers' tips), recurse with
  `steps + 1`.

Two invariants make this work:

- Each `drawLayer` writes exactly one new CENTER block — the new tip.
  Every other write is a non-CENTER shell.
- The recursion only follows CENTER cells downward, so it walks the
  trunk's actual spine — not the shell — and redraws each layer's shell
  at the new (larger) thickness.

That's why the base of a tall beanstalk keeps fattening as the tip
climbs — every growth tick re-runs the shell pass all the way down.

The `currentPos.y >= position.y` guard stops the recursion descending
below the bean so a thickness pass triggered at the base doesn't keep
stepping into negative depth.

---

## 4. BlockEntity tick loop

`BeanPlantBlockEntity.serverTick` is the driver. Each server tick:

1. Cast `Level` to `ServerLevel` (ignore client ticks).
2. `sl.getRandom.nextInt(3) == 0` — 1-in-3 throttle. Matches the 1.12.2
   cadence without a separate counter.
3. Acquire the generator:
   - **in-memory** → use it;
   - **pending NBT** (just loaded) → construct a fresh generator, apply
     `readNbt`, skip all gates;
   - **cold start** → apply the spawn gates; if they pass, construct.
4. `gen.update()` — advance one layer.
5. `setChanged()` — mark the BE dirty so its NBT gets saved.

---

# Port adjustments (things that differ from the 1.12.2 original)

The three sections below are the changes layered on top of the faithful
port. They are all in `BeanPlantBlockEntity` / `BeanstalkGenerator` and
leave the growth algorithm itself untouched.

## A. Spawn gates — only sprout in the Overworld, only when fully encased in dirt

Cold start gates both must hold:

- **Dimension gate:** `sl.dimension() == Level.OVERWORLD`. Beanstalks are
  the anchor into Skylands — only the Overworld is a valid source
  dimension. A bean placed in the Nether stays inert. Matches the
  one-way-teleport invariant in `CloudBlock.scala`.
- **Dirt encasement:** all six face-adjacent blocks match
  `BlockTags.DIRT` (dirt, coarse dirt, grass block, podzol, rooted dirt,
  mycelium, mud, moss block). Forces the player to deliberately bury the
  bean rather than leave one sitting on the ground.

Both gates apply only to the **first** `update()`. Once the generator
exists, mining out a neighbouring dirt block mid-grow doesn't stop the
stalk, and the reload path (see §C) bypasses the gates entirely — a
save file is authoritative, regardless of what the player has since done
to the surroundings.

## B. Downward roots — break out the underside of the dirt pocket

`growRoots()` runs at the start of every `update()`. This is the part of
the generator that doesn't exist in the 1.12.2 original, and it's needed
because the spawn gate now requires the bean to be *fully* encased in
dirt — without a dedicated down-pass, the stalk would be sealed in from
below.

Two reasons the roots pass can't just be folded into the trunk
recursion:

1. The recursive shell pass only follows CENTER beanstalk cells. The
   bean itself is not a CENTER cell (it's a different block), so the
   recursion dead-ends at the bean and never reaches anything below it.
2. Shell thickness is driven by `steps` in the trunk recursion, which
   isn't a useful measure for roots. Roots instead size their shells
   from `progress + max(dy, 1)` — they fatten alongside the trunk as
   growth continues.

Algorithm, for `dy ∈ [0, MaxRootDepth = 5]`:

- `rootPos = position.below(dy)`.
- `dy = 0` → shell-only pass at the bean's own y. The bean itself is
  preserved (the aggressive shell rule, below, makes an explicit
  exception), but its surrounding dirt ring gets replaced with stem
  shell so the base isn't left trapped in an untouched band.
- `dy > 0`:
  - if `rootPos` is the bean → stop (safety; wouldn't hit on a healthy
    first tick);
  - if `rootPos` is already stem → leave block in place, still redraw
    the shell;
  - else if block is in `BlockTags.DIRT` or is replaceable → overwrite
    with a CENTER stem;
  - else (stone, cobble, bedrock, …) → stop. Don't breach real terrain
    — hard rock is a natural root-stop.
- Always draw a shell disk of size `progress + max(dy, 1)` at that y.

### Aggressive shell-write rule near the base

Because the roots push through a dirt pocket and the player's build
often surrounds that pocket, `drawShellBlock` has a second mode for
`pos.y <= bean.y + 1`:

- **Near the base** (aggressive): overwrite almost anything (walls,
  planks, deepslate, …) except the bean itself and existing stem blocks.
  Keeps the dirt encasement + whatever the player built around it from
  leaving an uncarved ring that hides the trunk.
- **Above the base** (1.12.2 rule): fall back to `drawBlock`, which only
  overwrites replaceable or terrain blocks. Protects player builds from
  a stalk drilling through them mid-sky.

## C. NBT persistence — resume growth across reload

The 1.12.2 generator is in-memory only. Without NBT, a reload mid-grow
would:

- regenerate `sineLayerAmplitudes` / `sineLayerOffset` (trunk snaps to a
  new wobble shape, since these come from `scala.util.Random`),
- reset `progress` to 0,
- lose `lastBlockPos`,
- leave the previously-drawn trunk stranded and start growing a second
  stalk from the bean.

### State that gets persisted

| Field | NBT | Why |
| --- | --- | --- |
| `progress` | `Int` | tick count, drives sine phase and height |
| `lastBlockPos` | `Long` (via `BlockPos.asLong()`) | current tip |
| `sineLayerAmplitudes` | `LongArray` of 7 raw double bits | wobble shape |
| `sineLayerOffset` | `LongArray` of 7 raw double bits | wobble phase |

NBT in 1.21.1 has no `DoubleArray` tag, so the two wobble arrays travel
as `LongArray`s of `Double.doubleToRawLongBits` — exact round-trip,
fixed length 7. The sine arrays were `val`s in the original; they became
`var`s here so `readNbt` can overwrite them.

### Save / load flow

- `saveAdditional` — if a generator exists, write its state under the
  `generator` compound key.
- `loadAdditional` — if the key is present, stash the compound in
  `pendingGeneratorNbt`. **Don't** build the generator here:
  `loadAdditional` runs before the BE is attached to a level, and
  `new BeanstalkGenerator(...)` needs
  `level.getServer.getLevel(SkylandsWorldgen.SKYLANDS_LEVEL)`.
- `serverTick` — the first tick after load sees `pendingGeneratorNbt`,
  builds a fresh generator, `readNbt`s into it, and continues. The
  spawn gates (§A) are skipped on this path.
- `setChanged()` on every growth tick — without it, the BE's updated NBT
  wouldn't reliably make it into the next chunk save.

### Intentional non-determinism

`drawLayer`'s ±15 jitter uses `level.getRandom`, not a BE-local RNG, so
the trunk path after a reload isn't bit-exact with a no-reload
reference. What the player notices — the big wobble **shape** — is
preserved because the sine arrays are restored from NBT. A fully
deterministic rewrite would need a level-seeded `RandomSource` and is
out of scope.

---

## 5. At a glance

```
tick
 └─ BeanPlantBlockEntity.serverTick
     ├─ 1/3 random gate
     ├─ acquire generator:
     │    ├─ memory → use
     │    ├─ pending NBT → new + readNbt (skip gates)          (C)
     │    └─ cold → OVERWORLD + dirt-encased → new             (A)
     ├─ gen.update()
     │    ├─ growRoots                                         (B)
     │    ├─ sine sum → dest
     │    ├─ drawLayer(dest)
     │    │    ├─ jittered unit step → new tip
     │    │    ├─ write CENTER at tip
     │    │    ├─ recursiveFunction  // thicken trunk downward
     │    │    └─ y ∈ [245, 255] → new CloudGenerator
     │    └─ progress += 1
     └─ setChanged()                                           (C)
```
