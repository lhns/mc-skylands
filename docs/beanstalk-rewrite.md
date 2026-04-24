# Beanstalk generator — deterministic rewrite

Companion to `beanstalk-generation.md` (which documents the earlier
port of the 1.12.2 design). This file covers the rewrite: why the old
design was replaced, and what the new one looks like.

Files:

- `common/src/main/scala/skylands/worldgen/BeanstalkGenerator.scala`
- `common/src/main/scala/skylands/worldgen/BlockArray.scala`
- `common/src/main/scala/skylands/worldgen/CloudGenerator.scala`
- `common/src/main/scala/skylands/blockentity/BeanPlantBlockEntity.scala`

---

## 1. What was wrong with the old design

### 1.1 Mid-air full-thickness slabs

The old generator thickened the trunk by walking down from the new tip
along CENTER cells, bumping a `steps` counter per hop. Two independent
ways the walk lost the spine:

- **Obstruction gap (primary).** A CENTER write went through
  `drawBlock`'s overwrite rule. If the destination cell held something
  the rule didn't match — player-placed wood, obsidian, etc. — the
  CENTER was silently dropped. Next tick the recursion looked one
  block down for a CENTER neighbour, found none, and restarted fresh
  at `steps = 0`. Later ticks revisiting that disconnected segment at
  larger `steps` would then paint a full-thickness slab floating in
  mid-air, disconnected from the base.
- **Jitter gap.** `drawLayer`'s ±15 random XZ step could land the new
  tip on a cell not directly above a previous CENTER, even with no
  obstruction — same recursion-dead-end mechanism.

The rewrite doesn't use block-placement to find the spine. Every layer
is indexed by an integer `p`, so a missing CENTER is a local cosmetic
miss; the spine itself is never lost.

### 1.2 Roots were disproportionately complex

The old root pass had a dedicated method (`growRoots`) with:

- a dy=0 shell-only pass (otherwise the bean's own ring stayed dirt);
- a "stop at the bean" safety check;
- an "already stem → leave block, still redraw shell" branch;
- a `BlockTags.DIRT || isReplaceable` gate;
- a hard stop on stone-like cells;
- an aggressive-replace rule for `y <= bean.y + 1` that overwrote
  almost anything except the bean or stems.

All of that existed because the trunk recursion couldn't cross the bean
(the bean isn't a CENTER cell) and because the downward punch was
straight along `bean.below(dy)`. In the rewrite, roots are just the
same layer sweep with `p < 0`, so they ride the sine wobble and share
the trunk's overwrite rule.

### 1.3 Two overwrite predicates doing one job

`BlockArray.isReplaceable` and `BlockArray.isTerrainBlock` always acted
together: `drawBlock` accepted a cell if either held. The predicates
also hardcoded `grass | dirt | stone` as "terrain". Merged into one
`canOverwrite` keyed on `BlockTags.DIRT` + `BlockTags.BASE_STONE_OVERWORLD`
(which covers stone, granite, diorite, andesite, tuff, deepslate)
plus the original replaceable list.

### 1.4 Hardcoded y = 255 seam

`BlockArray.syncVertical` and the cloud trigger band were written for
1.12.2's build-height-255. In 1.21 overworld build height is 320, so
the seam was sitting 65 blocks below the top and cloud clusters spawned
in an invisible band in the middle of the sky. The seam is now
parameterised from `LevelHeightAccessor.getMaxBuildHeight - 1`.

### 1.5 World-seed wobble + NBT-heavy persistence

Old wobble was seeded from `scala.util.Random`, which meant shape was
lost on reload. The port patched around it by persisting the two 7-
element sine arrays as `LongArray`s of raw double bits. Works, but
`~128 bytes` of NBT for something that's really `1 Long` of entropy.

Now a single `Long` bean-seed is rolled at cold start and persisted;
wobble amplitudes / phase offsets are reconstructed from it on load.

### 1.6 Update-time recursion

`update` composed "walk up by one layer" with a recursive "walk down
the CENTER spine to re-thicken" — two passes that together do one
thing, at the cost of (a) the bugs in §1.1 and (b) reading the world
back to decide what to do next.

The rewrite does it in a flat sweep indexed by `p`: one tip extension
plus one re-thicken sweep, both pure functions of `progress` and
`seed`.

---

## 2. The new design

The stalk is a deterministic 1-D curve in Y. Three functions do
everything:

```
stalkCenter(p: Int): BlockPos            // (dx, p, dz) offset from bean
thickness(distFromTip: Int): Int         // log1p(dist * 1.4) as radius
drawDisk(center, radius, state): Unit    // idempotent write through canOverwrite
```

### 2.1 Per-bean seed

Rolled once at cold start from `level.getRandom.nextLong()`; persisted
in NBT; drives every random draw in the generator. No coupling to
world seed, no reliance on `scala.util.Random`.

`seed` and `progress` are **constructor parameters**, not reassignable
fields. Three constructors:

```scala
BeanstalkGenerator(level, pos, seed: Long, progress: Int)   // primary
BeanstalkGenerator(level, pos)                              // cold start
BeanstalkGenerator(level, pos, tag: CompoundTag)            // restore
```

Restore builds a generator from the persisted NBT directly — no
post-construction `readNbt` / mutable-seed dance.

### 2.2 `stalkCenter(p)` — deterministic sine wobble

Same 7-octave sum as the 1.12.2 port. Amplitudes and phase offsets are
derived from `seed` via two `RandomSource`s (one primary, one seeded
with `seed ^ 0x5A1E` to stay order-independent between the arrays).
Same seed → same wobble, always.

`p` is signed: trunk at `p > 0`, bean Y at `p = 0`, roots at `p < 0`.
Roots ride the same curve, tilted downward.

### 2.3 `thickness(distFromTip)` — age-based taper

`distFromTip(p) = progress - |p|`. Both tips (trunk at `p = progress`
and root floor at `p = -min(progress, rootDepth)`) are fresh when they
appear, so a freshly-born layer has `distFromTip = 0` → radius 0 (a
single CENTER cell). As `progress` climbs, every still-existing
layer's `distFromTip` grows, and its thickness grows with it. Gradual
fattening falls out for free — no separate "how many ticks has this
layer been alive" counter.

The `|p|` works as the birth tick because `update` extends both tips
by one per call: trunk layer `p` is born on tick `p`, root layer `-k`
is born on tick `k`.

Formula: `log1p(distFromTip * 1.4)` truncated to `Int` — same visual
feel as the 1.12.2 port.

### 2.4 `drawDisk` — one idempotent helper

Square-iterated disk, `canOverwrite` gate per cell. Cells that fail
the gate are skipped silently. Next tick re-attempts every layer's
draw at the same center, so transient obstructions resolve themselves
if the blocking block ever becomes overwritable.

### 2.5 `update()` — extend both tips, re-sweep the stalk

```scala
if stalkCenter(progress).y > seamY + 130 then return    // hard cap
progress += 1
rootFloor = -min(progress, rootDepth)
for p in rootFloor..progress:
  drawDisk(stalkCenter(p), thickness(distFromTip(p)), shellState)
  trySet(stalkCenter(p), centerState)                   // spine marker
if stalkCenter(progress).y in [seamY-10, seamY]:
  new CloudGenerator(level, stalkCenter(progress))
```

Both tips extend by one per update (up to `rootDepth` for the root
direction). O(progress · diskArea) per tick, same order as the old
recursion. The integer `log1p` formula means most layers' radii don't
change most ticks, so the writes are mostly idempotent no-ops.

### 2.6 Seam parameterisation

`BlockArray.syncVertical(bottom, top, overlap, seamY)` now takes the
seam as a parameter. Callers use `level.getMaxBuildHeight - 1`.
`SkylandsOverlap = 15` stays — it's the band width, not an absolute Y.

### 2.7 Overwrite predicates — generic + beanstalk-local

The central "is this block free to paint over?" predicate lives on
`BlockArray` and is shared across every generator:

```scala
// BlockArray.isReplaceable
s.isAir
|| s.canBeReplaced
|| s.is(BlockTags.LEAVES)
|| s.is(BlockTags.SAPLINGS)
|| s.is(Blocks.VINE)
|| s.is(SkylandsBlocks.CLOUD.get())
```

Beanstalks additionally punch through dirt and natural overworld
stone, but that widening is local to the beanstalk generator and
doesn't affect `CloudGenerator` or any future caller:

```scala
// BeanstalkGenerator.canOverwrite
syncedWorld.isReplaceable(pos)
  || state.is(BlockTags.DIRT)
  || state.is(BlockTags.BASE_STONE_OVERWORLD)
```

`BASE_STONE_OVERWORLD` covers stone, granite, diorite, andesite, tuff,
and deepslate. Bean blocks and stem blocks match nothing in either
predicate and are therefore implicitly protected. Player builds
(wood, planks, glass, stone bricks, etc.) are also protected — the
overwrite set is "naturally-occurring overworld terrain plus
vegetation".

### 2.8 NBT

```
{ progress: Int, seed: Long }
```

That's all. `amps` / `offsets` are reconstructed from `seed`;
`lastBlockPos` is gone (each layer's position is `stalkCenter(p)`, no
dead reckoning). `BeanPlantBlockEntity` is agnostic to the internal
shape — it just stashes the generator's compound under the `generator`
key and passes it back to the restore constructor on reload.

---

## 3. Preserved behaviour

What intentionally stayed the same:

- **Spawn gates** — Overworld-only and fully-encased-in-dirt. Living in
  `BeanPlantBlockEntity`, unchanged.
- **Restore bypasses gates** — a reload of a beanstalk whose dirt shell
  has since been mined out still finishes growing.
- **1/3 tick throttle** — the "grow on every third tick on average"
  cadence from the 1.12.2 port.
- **Sine-octave wobble shape** — same formula, same constants, same
  look. Only the seed source changed.
- **Cloud clusters at the seam** — still triggered when the tip enters
  the top-10 band of the seam Y (previously `[245, 255]`; now
  `[seamY - 10, seamY]`).

---

## 4. At a glance

```
tick
 └─ BeanPlantBlockEntity.serverTick
     ├─ 1/3 random gate
     ├─ acquire generator:
     │    ├─ memory → use
     │    ├─ pending NBT → new BeanstalkGenerator(sl, pos, nbt)
     │    └─ cold → OVERWORLD + dirt-encased → new BeanstalkGenerator(sl, pos)   (rolls seed)
     ├─ gen.update()
     │    ├─ hard-cap check
     │    ├─ progress += 1
     │    ├─ rootFloor = -min(progress, rootDepth)
     │    ├─ for p in rootFloor..progress:
     │    │    ├─ drawDisk(stalkCenter(p), thickness(progress - |p|))
     │    │    └─ trySet spine marker
     │    └─ tip in seam band → new CloudGenerator
     └─ setChanged()
```
