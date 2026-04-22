# SkylandsMod (Minecraft Mod)
[![Build status](https://teamcity.lolhens.de/app/rest/builds/buildType:SkylandsMod_Build/statusIcon.svg)](https://teamcity.lolhens.de/viewType.html?buildTypeId=SkylandsMod_Build&guest=1)

This mod is a port of the old skylands dimension that was experimented with in Minecraft beta 1.6. It could be unlocked through modifications of the game and then replaced the overworld dimension.

[Minecraft Wiki: Sky dimension](https://www.curseforge.com/linkout?remoteUrl=http%253a%252f%252fminecraft.gamepedia.com%252fMentioned_features%252fSky_dimension)

This mod adds back the skylands as a seperate dimension. You start your game as normal but you can later travel to the skylands.

![](https://raw.githubusercontent.com/LolHens/SkylandsMod/master/screenshots/2017-03-05_13.04.43.png)

In order to get to the skylands dimension you have to find a magic bean in a dungeon chest.
Once you plant this bean, a giant bean stalk shoud grow and you will be able to climb up into the skylands.

![](https://raw.githubusercontent.com/LolHens/SkylandsMod/master/screenshots/2017-03-05_13.01.29.png)

![](https://raw.githubusercontent.com/LolHens/SkylandsMod/master/screenshots/2017-03-05_13.01.38.png)

## Requirements

### Minecraft 1.21.1

**Fabric:**
- [Fabric Loader](https://fabricmc.net/use/) ≥ 0.16.0
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [krysztal-language-scala](https://maven.krysztal.dev/releases) ≥ 3.3.0 — Scala 3 language adapter for Fabric

**NeoForge:**
- [NeoForge](https://neoforged.net/) ≥ 21.1
- [Scalable Cat's Force](https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force) (SCF) — the shared Scala 3 language provider for Scala-backed NeoForge mods. Skylands loads against it at runtime rather than bundling its own Scala library, so it coexists with other Scala-backed mods (Thaumcraft NF, QuarryPlus, etc.).

### Minecraft 1.12.2 (legacy)
[<img src="https://avatars2.githubusercontent.com/u/1390178" width="32"> Minecraft Forge](https://files.minecraftforge.net/) — see the `master` branch.

## Licensing
This project uses the Apache 2.0 License. See the file called LICENSE.
