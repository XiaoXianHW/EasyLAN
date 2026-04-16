# EasyLAN CI Hub

这个分支只用来放统一的 GitHub Actions 聚合构建入口，不承载任何 Mod 业务代码。

## 用法

- 进入 `Actions`
- 选择 `Aggregate Build`
- 选择 `profile`
  - `all`: 构建全部活动维护分支
  - `forge`: 只构建 Forge 活动分支
  - `fabric`: 只构建 Fabric 活动分支
  - `neoforge`: 只构建 NeoForge 活动分支
- 如果要更细控制，可以在 `branches` 里手填分支名
  - 支持逗号、空格、换行分隔
  - 只允许活动维护分支，不允许 `z-acrhive-*`

## 构建行为

- Workflow 会自动拉取目标代码分支
- 自动发现各分支里的 `versions/*/project`
- 自动按版本选择需要的 Java 运行时
- 自动跳过 `1.7.2` 和 `1.7.10`
- 每个版本都会单独构建并上传 jar
- 最后会再汇总成一个总 zip，下载一次就够

## 当前活动维护分支

- `forge-1.7.2-1.11.2`
- `forge-1.12.2`
- `forge-1.13.2-1.15.2`
- `forge-1.16.4-1.18.2`
- `forge-1.19.2-1.21.11`
- `fabric-1.14.4-1.15.2`
- `fabric-1.16.4-1.16.5`
- `fabric-1.17.1-1.20.1`
- `fabric-1.20.6-1.21.11`
- `neoforge-1.20.1-1.21.11`
