# mcdp port

How the `mcdp-1.21.1` branch replaced the per-loader Scala language providers
with a single one — [mcdp](https://gitea.lhns.de/lhns/mcdp), the *MC
Dependency Provider* — across Fabric and NeoForge.

## Why

The 1.21.1 multiloader port shipped with two different Scala 3 language
providers, one per loader:

- **Fabric:** Krysztal's `krysztal-language-scala` — an active fork of
  fabric-language-scala. Available only from `maven.krysztal.dev`, which
  has had multi-day 502 outages, so the artifact lives vendored in
  `gradle/offline-maven/` to keep CI green.
- **NeoForge:** Kotori316's *Scalable Cat's Force* with the
  `:with-library` classifier. The `:with-library` flat-merged classifier
  exists specifically because NeoForge's JPMS module layer rejects the
  default jar-in-jar layout — `cats-kernel` ships keyword package names
  (`scala`, `byte`, `int`, …) that `ModuleDescriptor.Builder.packages`
  blows up on. Kotori works around this by forking cats, which then
  breaks compatibility with anything compiled against vanilla cats
  (circe, fs2, …).

mcdp solves both with one mechanism: each mod that opts in gets its own
`URLClassLoader`. Maven deps stay in that loader's *unnamed* JPMS module
(so the keyword check never fires) and each mod sees its own copy of
every dep (so version conflicts can't happen). No bytecode rewriting,
no library forks. Same provider on both loaders means one set of
dependency-loading bugs to debug, not two.

## Build wiring

mcdp lives at `C:/Users/pierr/Documents/git/mc-scala` (sibling checkout).
The skylands build pulls it in via composite include:

- **`settings.gradle`** — `pluginManagement { includeBuild('../mc-scala') }`
  so the Gradle plugin id `de.lhns.mcdp` resolves without a publish.
- **`build.gradle` (root)** — declares `id 'fabric-loom' apply false` and
  `id 'de.lhns.mcdp' apply false` in the root `plugins {}` block so all
  subprojects share *one* plugin classloader. Without this, common and
  fabric each loaded their own copy of fabric-loom; loom's
  `BuildSharedServiceManager` then failed cross-classloader casts inside
  `RemapJarTask`. Subprojects re-`apply plugin: …`.
- **`fabric/build.gradle` & `neoforge/build.gradle`** — `mavenLocal()` is
  added to the per-subproject repositories. The plugin is composite-built,
  but the *runtime* artifacts (`de.lhns.mcdp:mcdp-fabric`,
  `de.lhns.mcdp:mcdp-neoforge`) are shadow jars and have to be published
  to mavenLocal explicitly. Workflow before any rebuild:

  ```
  ../mc-scala/gradlew :fabric:publishToMavenLocal :neoforge:publishToMavenLocal
  ```

  (mc-scala's ADR-0012 explains why composite substitution can't replace
  the shadow jar — Fabric's `ClasspathModCandidateFinder` rejects the
  per-subproject classes-dir output.)

## Per-loader changes

### Fabric (`fabric/build.gradle`, `fabric/src/main/resources/fabric.mod.json`)

- Drop `dev.krysztal:krysztal-language-scala`.
- Drop the `gradle/offline-maven` repo and the `maven.krysztal.dev` entry
  (the offline jar still exists under `gradle/offline-maven/dev/krysztal/`
  but nothing references it; safe to delete).
- Add `modImplementation 'de.lhns.mcdp:mcdp-fabric:0.1.0-SNAPSHOT'`.
- Move `org.scala-lang:scala3-library_3` from `implementation` to
  `mcdepImplementation` — that's mcdp's opt-in bucket. The Gradle plugin
  walks its transitive closure and emits each library into the
  manifest. (`scala-library:2.13.x` comes along as a transitive of
  `scala3-library_3` — that's just how Scala 3 is packaged, not an mcdp
  artifact.)
- Add `mcdepprovider { lang.set('scala') }`.
- `fabric.mod.json`: `"adapter": "scala"` → `"adapter": "mcdepprovider"`,
  and depends `"krysztal-language-scala"` → `"mcdepprovider"`.

### NeoForge (`neoforge/build.gradle`, `neoforge/src/main/resources/META-INF/neoforge.mods.toml`)

- Drop `com.kotori316:scalablecatsforce-neoforge:*:with-library` and the
  `kotori_scala` modLoader.
- Apply `id 'de.lhns.mcdp'` alongside `net.neoforged.moddev`.
- Add `implementation 'de.lhns.mcdp:mcdp-neoforge:0.1.0-SNAPSHOT'`.
- Move `scala3-library_3` from `compileOnly` to `mcdepImplementation`
  (no `excludeGroup` / platform-enumeration bookkeeping needed — the
  plugin auto-subtracts anything actually platform-provided).
- Add `mcdepprovider { lang.set('scala') }`.
- `neoforge.mods.toml`: `modLoader = "mcdepprovider"`, with
  `loaderVersion = "[1,)"`. **No** `[modproperties.skylands]
  entrypoint = "…"` block — mcdp rediscovers `@Mod`-annotated classes
  via `ModFileScanData.getAnnotatedBy(Mod.class)` exactly like vanilla
  javafml, so the standard FML pattern just works.

`SkylandsNeoForge.scala` is unchanged:

```scala
@Mod("skylands")
class SkylandsNeoForge(modBus: IEventBus):
  SkylandsPlatform.install(new NeoForgePlatform(modBus))
  SkylandsCommon.init()
```

mcdp's NeoForge loader matches FML's default `(IEventBus)` constructor
shape directly. `@EventBusSubscriber`-annotated classes are picked up
via NeoForge's own `AutomaticEventSubscriber.inject` — no special case.

## Runtime contract

mcdp is a separate mod, not bundled into skylands. Drop **both** jars
into `mods/`:

- Fabric: `skylands-fabric-…jar` + `mcdp-fabric-0.1.0-SNAPSHOT.jar`
  (built from mc-scala via `:fabric:shadowJar`).
- NeoForge: `skylands-neoforge-…jar` + `mcdp-neoforge-0.1.0-SNAPSHOT.jar`
  (built from mc-scala via `:neoforge:shadowJar`).

On first launch, mcdp downloads `scala3-library_3` and `scala-library`
from Maven Central into `~/.cache/mc-lib-provider/libs/<sha>.jar`. Net
access is required only for the first run; subsequent runs hit the
cache.

A built skylands jar contains `META-INF/mcdepprovider.toml` listing
each declared dependency's coords, URL, and SHA-256. mcdp reads that
manifest at boot, verifies SHAs against the cache, and serves each lib
through the per-mod URLClassLoader.

## Files touched on this branch

```
build.gradle                                     plugins {} apply-false block
common/build.gradle                              re-apply pattern
fabric/build.gradle                              mcdp + mcdepImplementation
fabric/src/main/resources/fabric.mod.json        adapter + depends
gradle.properties                                mcdp_version
neoforge/build.gradle                            mcdp + mcdepImplementation
neoforge/src/main/resources/META-INF/neoforge.mods.toml   modLoader
settings.gradle                                  includeBuild('../mc-scala')
docs/mcdp-port.md                                this file
```

## Out of scope

- Deleting the vendored `gradle/offline-maven/dev/krysztal/` jar +
  POM. Worth a follow-up cleanup once nothing on any active branch
  references them.
- A non-snapshot mcdp release. v0.1.0 is not on Maven Central yet;
  composite + mavenLocal is the only resolution path.
