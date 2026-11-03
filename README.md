# Techguns Remake [Unofficial]

An unofficial port of **Techguns 2** (Minecraft 1.12.2) by **pWn3d_1337** and the Techguns team
to modern Minecraft: guns, power armor, machines and multiblocks, NPC soldiers, structures,
radiation and the in-game manual.

**This is not the official Techguns and is not supported by the original authors.
Please do not report problems with this port to them.**

- Original mod: https://www.curseforge.com/minecraft/mc-mods/techguns
- Original source (1.12.2): https://github.com/pWn3d1337/Techguns2
- This port on CurseForge: https://www.curseforge.com/minecraft/mc-mods/techguns-remake-1-20-1

## Versions (branches)

| Branch | Minecraft | Loader |
|---|---|---|
| `1.20.1` | 1.20.1 | Forge 47.x |
| `1.21.1` | 1.21.1 | NeoForge 21.1.x |
| `1.19.2` | 1.19.2 | Forge 43.x |

Each branch is a complete, separately buildable project.

## Building

Requires Java 17 (Java 21 for the `1.21.1` branch).

```
./gradlew build          # Linux / macOS
gradlew.bat build        # Windows
```

The mod jar is written to `build/libs/`. The Patchouli manual library is bundled inside the jar (Jar-in-Jar).

Development client: `gradlew runClient` (add `-PwithJei=true -PwithPatchouli=true` for JEI and the manual).

## License

The original Techguns code, models, textures and sounds belong to their original authors and remain under the
original **Techguns Mod License** (see `LICENSE.txt`). The porting work is published here so the source of this
port is available. Patchouli is by Vazkii (CC BY-NC-SA 3.0).

---

**RU:** Неофициальный порт мода Techguns 2 (автор оригинала — pWn3d_1337) на 1.20.1 Forge, 1.21.1 NeoForge и 1.19.2 Forge.
Каждая версия — в своей ветке. Сборка: `gradlew build`. Оригинальный контент остаётся под лицензией Techguns Mod License.
