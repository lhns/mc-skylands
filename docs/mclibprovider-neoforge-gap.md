# mc-lib-provider — NeoForge entry-point gap

This document describes a structural gap that prevents mc-skylands (and any
NeoForge mod that needs `IEventBus` access during construction) from porting
to mc-lib-provider on NeoForge. Fabric is unaffected.

The Fabric port lives on this branch (`multi-1.21.1-mclibprovider-fabric`).
The NeoForge subproject still uses Scalable Cat's Force pending the changes
below.

## Symptom

mc-skylands' NeoForge entry is, per standard FML pattern:

```scala
@Mod("skylands")
class SkylandsNeoForge(modBus: IEventBus):
  SkylandsPlatform.install(new NeoForgePlatform(modBus))
  SkylandsCommon.init()
```

`NeoForgePlatform` registers `RegisterEvent` and `FMLClientSetupEvent`
listeners on `modBus` during construction, because those events are gated
to the per-mod event bus and fire *before* any runtime event the mod could
otherwise hook into.

mc-lib-provider's `McLibLanguageLoader.loadMod` constructs the entry like
this (`neoforge/.../McLibLanguageLoader.java:241-248`):

```java
return EntrypointAdapter.forLang(lang).construct(entryClass, info);
```

Only `IModInfo` is passed. There is no `IEventBus` available to the mod.
A `(IEventBus)` constructor cannot be matched by `JavaEntrypointAdapter`'s
arity-walk because the bus isn't in `args`.

## Why it can't be worked around in the mod

Two options that look workable on the surface but don't:

### 1. Look up our own bus from `ModList` after construction

```scala
val bus = ModList.get
  .getModContainerById(info.getModId)
  .orElseThrow()
  .getEventBus
```

Doesn't work. `loadMod` returns `new McLibModContainer(info, instance)`
*after* the entry is constructed, so during the constructor `ModList` does
not yet hold our `McLibModContainer`. The lookup throws.

### 2. Defer to a later event

`RegisterEvent`, `FMLClientSetupEvent`, and friends are mod-bus events.
Subscribing to them requires the mod bus. We cannot subscribe without the
bus, so we cannot defer to them.

`NeoForge.EVENT_BUS` (the global runtime bus) *is* accessible statically,
but it does not dispatch `RegisterEvent` — by the time any of its events
fire (e.g. `ServerStartingEvent`), the registry phase has already closed.

NeoForge's `@EventBusSubscriber` annotation auto-registers static handlers
without needing a bus reference, but FML only scans for it from
`FMLModContainer` (`fml/javafmlmod`). `McLibModContainer` does not run that
scan, so the annotation is silently inert under mc-lib-provider.

## What mc-lib-provider needs to add

The `EntrypointAdapter` interface already documents the intended shape
(`core/.../EntrypointAdapter.java:7-9`):

> All implementations accept up to three Object arguments to allow per-platform
> ctor-arity dispatch (e.g. NeoForge: `(IEventBus, ModContainer, Dist)`;
> Fabric: arity-0). Unused arguments should be passed as null and ignored
> by the adapter if its target ctor doesn't accept them.

The adapter will already select a `(IEventBus)` constructor over a no-arg
one when extras are passed, by virtue of the arity-walk (it tries `args.length`
down to 0 and returns the first match). The gap is at the call-site, not
the dispatch.

### Required changes

1. **`McLibLanguageLoader.loadMod` — build the bus before `construct()`.**

   Re-order so the `IEventBus` (and `Dist`, and a `ModContainer` reference)
   exist before the entry constructor runs:

   ```java
   // Build the bus up-front (same shape as McLibModContainer's current ctor).
   IEventBus eventBus = BusBuilder.builder()
       .setExceptionHandler(...)
       .markerType(IModBusEvent.class)
       .allowPerPhasePost()
       .build();
   Dist dist = FMLEnvironment.dist;

   Class<?> entryClass = loadEntryClass(info, loader, modId);
   Object instance = construct(manifest.lang(), entryClass, info, eventBus, dist);

   return new McLibModContainer(info, instance, eventBus);
   ```

2. **`McLibModContainer` — accept a pre-built bus** instead of building its
   own. Trivial signature change:

   ```java
   public McLibModContainer(IModInfo info, Object modInstance, IEventBus eventBus) {
       super(info);
       this.modInstance = modInstance;
       this.eventBus = eventBus;
   }
   ```

3. **`McLibLanguageLoader.construct` — forward the extras.**

   ```java
   private static Object construct(String lang, Class<?> entryClass,
                                   IModInfo info, IEventBus bus, Dist dist) {
       try {
           return EntrypointAdapter.forLang(lang)
               .construct(entryClass, info, bus, dist);
       } catch (ReflectiveOperationException e) {
           throw new IllegalStateException(...);
       }
   }
   ```

`JavaEntrypointAdapter` already walks arities `args.length` down to 0, so
existing `(IModInfo)`-only entries keep working — and existing `(IEventBus)`
entries (the FML default shape) start working immediately.

`ScalaEntrypointAdapter` already falls through to the Java adapter when no
`MODULE$` singleton exists, so Scala class-with-bus-constructor entries work
the same way.

### Argument ordering

Two reasonable conventions:

- **`(IModInfo, IEventBus, ModContainer, Dist)`** — mc-lib-provider native
  order, `IModInfo` first because it's already used for entries that don't
  need the bus.
- **`(IEventBus, ModContainer, Dist)`** — matches FML's `FMLModContainer`
  default constructor signature, so `@Mod`-style entries port verbatim.

The arity-walk handles both via prefix-matching as long as the FML-default
order isn't broken. Suggest passing **`(info, bus, container, dist)`** —
matches the `EntrypointAdapter` javadoc's existing example wording (the
javadoc currently lists `(IEventBus, ModContainer, Dist)` but adding `info`
as the leading `args[0]` is a strict superset and keeps the existing
`(IModInfo)` callers working).

### Test-mod coverage

The current `test-mods/neoforge-example` mod uses an `object` entry with no
bus access — that's fine for the cats/circe smoke but doesn't exercise the
mod-bus surface. Adding an `(IEventBus)`-taking variant (or a class-form
example with a `RegisterEvent` listener) would lock in the contract.

## Out of scope here

- `@EventBusSubscriber` static-handler scanning. Not needed if mods take the
  bus via constructor; can be considered later for parity with `javafmlmod`.
- `@Mod` annotation discovery. `modproperties.<modId>.entrypoint` already
  fills that role; preserving `@Mod` would create two competing discovery
  paths.

## Tracking

When mc-lib-provider gains the bus-passing API, the follow-up port for
mc-skylands is:

1. `neoforge/build.gradle` — drop SCF, apply mclibprovider, add
   `modImplementation 'io.github.mclibprovider:neoforge:0.1.0-SNAPSHOT'`,
   move `scala3-library_3` to `mcLibImplementation`.
2. `neoforge/src/main/resources/META-INF/neoforge.mods.toml` — set
   `modLoader = "mclibprovider"` and add
   `[modproperties.skylands] entrypoint = "skylands.neoforge.SkylandsNeoForge"`.
3. `SkylandsNeoForge` — drop `@Mod`, keep the `(IEventBus)` constructor.
   Optionally accept `(info: IModInfo, bus: IEventBus)` to also use the
   info object.

Until then, NeoForge stays on Scalable Cat's Force on this branch.
