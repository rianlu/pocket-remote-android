# AGENTS.md

给在本仓库写代码的 AI。产品协议在 `docs/PROTOCOL.md`，本端规格在 `docs/SPEC.md`。先读这两份再改代码。

## 本仓库是什么

GitHub 仓库名：**pockettv-remote-android**  
安卓**手机**遥控客户端（口袋遥控）。电视助手在独立仓库 `pockettv-helper-android`。

不要在本仓库实现电视端、不要写鸿蒙。

## 开工前

1. 读 `docs/PROTOCOL.md`（禁止改端口、JSON `type`、字段名）。
2. 读 `docs/SPEC.md`（页面、minSdk、验收）。
3. 缺规格就问用户，不要发明第二套协议。

## 技术约束

- Kotlin + AndroidX + Jetpack Compose
- minSdk 21，compileSdk / targetSdk 35
- 网络：OkHttp 4；协程
- 中文 UI
- 常量类必须与 PROTOCOL 表逐字一致

## 禁止

- 改 PROTOCOL 中的端口、NSD 类型、magic、JSON 字段
- 输入法、无障碍、自动弹键盘
- ADB 当遥控；第一期不要扫 5555/厂商口去装包
- MQTT、广告 SDK、ConnectSDK
- 完整文件管理器（只传 `inbox` / `apk`）
- 把电视助手代码放进本仓库

## 改协议

不要直接改 `docs/PROTOCOL.md` 凑实现。先改工作区 `pocket-tv-android/docs/TV遥控器开发参考.md`，再同步两端 PROTOCOL。

## 完成标准

对照 `docs/SPEC.md` 验收顺序。声称完成前说明在哪些真机/模拟器上验证过；发现必须在真盒子上验证。
