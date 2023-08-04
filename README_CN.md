# EasyLAN
**用于自定义 LAN 服务器（内置服务端）相关设置的 Minecraft Mod**

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/easylan)
- [Modrinth](https://modrinth.com/mod/easylan)
- [MC百科](https://www.mcmod.cn/class/11373.html)
- [Wiki](https://docs.axtn.net/docs/EasyLAN/)（writing..）

<br>

## 配置列表

### *自定义*

- 自定义端口（**100-65535**）
- 自定义最大玩家数（**2-500000**）
- 自定义Motd（**100 字数**）

### *服务端基础设置*

- 允许PVP（**开启/关闭**）
- 正版验证（**开启/关闭**）
- 生成动物（**开启/关闭**）
- 生成NPC（**开启/关闭**）
- 允许飞行（**开启/关闭**）

### *服务端指令支持*

- 白名单（**/whitelist [on/off/add/remove/...]**）
- 封禁（**/ban|/ban-ip | /pardon|/pardon-ip**）
- 管理员权限（**/op | /deop**）
- 世界保存（**/save-all | /save-off | /save-on**）

### *其他*

- HttpAPI 信息（HTTPApi 支持 | [相关文档](https://docs.axtn.net/docs/EasyLan/HttpAPI)）<br>
  ***(HttpAPI存在兼容性问题，可能会导致退出游戏时崩溃。 详情请参阅https://github.com/XiaoXianHW/EasyLAN/issues/2)***
- LAN 信息输出（游戏里输出内置服务端的一些信息）

<br>

**您也可以通过`.minecraft\config\easylan.cfg`配置此插件（类似于server.properties）**

## 支持版本

- 1.7.2 - 1.20.1 [Forge]<br>
  **不支持 1.13.2**
- 在从EasyLAN旧版本更新至新版本时请删除`.minecraft\config\easylan.cfg`文件，否则可能会导致游戏崩溃（具体说明可以看更新日志）

<br>

## 翻译贡献

该MOD支持多种语言;<br>
要贡献翻译，请参阅Mod本体的语言文件，并将您的语言文件Pull requests到 `src/main/resources/assets/easylan/lang`<br>
贡献的翻译将添加到 **下一个 Minecraft 版本**（如果是Mod本体版本号更新，则添加至完整版本）

- 1.7.2 - 1.12.2 (xx_XX.lang; **例如: zh_CN.lang**)
- 1.14.4 - 1.20.1 (xx_xx.json; **例如: zh_cn.json**)

<br>

## 开发
构建
```
git clone https://github.com/XiaoXianHW/EasyLAN.git
./gradlew build
```

IntelliJ IDEA 开发
```
./gradlew genIntellijRuns
```

Eclipse 开发
```
./gradlew genEclipseRuns
```

**如果您在IDEA或Eclipse编译环境中使用`runClient`，需要将`EasyLAN.java`中的`DevMode`的值设置为`true`，否则无法正常运行**
