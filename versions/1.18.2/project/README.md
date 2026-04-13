# EasyLAN
**A Minecraft Forge Mod for Custom LAN Servers Related settings for customizing the LAN server (built-in server)**<br>
| [中文文档](https://github.com/XiaoXianHW/EasyLAN/blob/1.7.2/README_CN.md) |

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/easylan)
- [Modrinth](https://modrinth.com/mod/easylan)
- [MC百科](https://www.mcmod.cn/class/11373.html)

<br>

## Configurable List

### *Custom*

- Custom Port（**100-65535**）
- Custom Max Player（**2-500000**）
- Custom Motd（**100 Word Count**）

### *Server Basic Setting*

- Allow PVP（**True/False**）
- Online Mode（**True/False**）
- Spawn Animals（**True/False**）
- Spawn NPCs（**True/False**）
- Allow Flight（**True/False**）

### *Server Command Support*

- WhiteList（**/whitelist [on/off/add/remove/...]**）
- Banned（**/ban|/ban-ip | /pardon|/pardon-ip**）
- Operator（**/op | /deop**）
- SaveAll（**/save-all | /save-off | /save-on**）

### *Extensions*
**Extensions have not been tested for compatibility, please try to disable them if you have problems with the game crashing.**

- HttpAPI Info（HTTPApi Support | port: 28960 | address: 127.0.0.1）<br>
  ~~(There are compatibility issues with HttpAPI, which may cause a crash when exiting the game. For details, please refer to https://github.com/XiaoXianHW/EasyLAN/issues/2)~~<br>
 **Fixed in v1.5**
- LAN output（Game Chat Output LAN Server Info）

<br>

**You can also configure this plugin through `.minecraft\config\easylan.cfg` (similar to server.properties)**

## Support Version

- 1.7.2 - Latest version (maybe [Forge]<br>
  **Unsupported 1.13.2**<br>
  **1.7.2 Untested, usability is not yet certain**
- Please delete the `.minecraft\config\easylan.cfg` file when updating from an old version of EasyLAN to a new version, otherwise the game may crash (see the update log for details)

<br>

## Translation Contribution

This MOD supports multiple languages;<br>
To contribute translations, please refer to and upload your language files to `src/main/resources/assets/easylan/lang`<br>
Contributed translations will be added in the **next Minecraft version** (or full version if it's a major refactoring update)

- 1.7.2 - 1.12.2 (xx_XX.lang; **eg: zh_CN.lang**)
- 1.14.4 - Latest version (xx_xx.json; **eg: zh_cn.json**)

<br>

## Developers
Build
```
git clone https://github.com/XiaoXianHW/EasyLAN.git
./gradlew build
```

For IntelliJ IDEA
```
./gradlew genIntellijRuns
```

For Eclipse
```
./gradlew genEclipseRuns
```

**If you are using `runClient` in the IDEA or Eclipse compilation environment, you need to set the `DevMode` boolean value in `EasyLAN.java` to `true`, otherwise it cannot be executed normally**
