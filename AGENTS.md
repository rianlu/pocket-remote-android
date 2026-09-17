# AGENTS.md

给本仓库写代码的 AI。先读文档再改代码。

| 文件 | 用途 |
|---|---|
| [`docs/PROTOCOL.md`](docs/PROTOCOL.md) | 协议权威文本，须与 `pockettv-helper-android` 的同名文件一致 |
| [`docs/SPEC.md`](docs/SPEC.md) | 本仓库（手机端）怎么实现 |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | 形态、范围、技术栈，不是 UI 规范 |

## 本仓库

**pockettv-remote-android**：安卓手机遥控（口袋遥控）。电视在独立仓库 `pockettv-helper-android`。不要在本仓库写电视端或鸿蒙。

## 改协议

只改 `docs/PROTOCOL.md`，并在 **helper 仓库做同样修改**，保持两份文件一致。不要为了省事只改一端。不要发明第二套字段。

## 技术

- Kotlin + AndroidX + Compose；minSdk 21；targetSdk 35
- OkHttp 4、协程
- 常量与 PROTOCOL 表逐字一致

## 禁止

- 输入法、无障碍、自动弹键盘
- ADB 当遥控；第一期不扫厂商口装包
- MQTT、广告 SDK、ConnectSDK
- 完整文件管理器
- 把电视助手代码放进本仓库

缺规格就问，不要猜。
