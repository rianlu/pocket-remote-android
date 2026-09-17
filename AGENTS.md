# AGENTS.md

给本仓库写代码的 AI。先读文档再改代码。

| 文件 | 用途 |
|---|---|
| [`docs/PROTOCOL.md`](docs/PROTOCOL.md) | 协议权威文本，须与 `pockettv-helper-android` 的同名文件一致 |
| [`docs/SPEC.md`](docs/SPEC.md) | 本仓库（手机端）怎么实现 |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | 形态、范围、技术栈，不是 UI 规范 |

## 本仓库

**pockettv-remote-android**：安卓手机遥控（口袋遥控）。电视在独立仓库 `pockettv-helper-android`。不要在本仓库写电视端或鸿蒙。

## 目录结构（禁止把业务类平铺在根包）

源码尚未落地时按此创建，落地后必须维持：

```
app/src/main/java/com/pockettv/phone/
  PocketTvApp.kt            Application
  MainActivity.kt           入口
  protocol/                 与 PROTOCOL.md 一致的常量、JSON 编解码
  net/                      WebSocket 客户端、NSD/UDP 发现、HTTP 上传
  session/                  PIN、token 持久化
  ui/                       Compose 页面（设备列表、PIN、遥控板、键盘、文件、应用）
```

新类必须进对应子包。不要在 `com.pockettv.phone` 根包堆网络或协议实现。

## 注释

- 语言：**中文**。
- 每个 **public 类 / 重要 Composable 文件** 必须有一句话说明职责。
- 非显而易见的逻辑（握手、发现回退、token 存储）写注释。
- 不要给显而易见的 Compose 预览或简单 getter 写空话。
- 不要用英文复述方法名。

## 改协议

只改 `docs/PROTOCOL.md`，并在 **helper 仓库做同样修改**，保持两份文件一致。不要为了省事只改一端。不要发明第二套字段。`protocol` 包常量必须与 PROTOCOL 表逐字一致。

## 技术

- Kotlin + AndroidX + Jetpack Compose；minSdk 21；targetSdk 35
- 网络：OkHttp 4；协程
- 中文 UI

## 禁止

- 输入法、无障碍、自动弹键盘
- ADB 当遥控；第一期不扫厂商口装包
- MQTT、广告 SDK、ConnectSDK
- 完整文件管理器（只传 `inbox` / `apk`）
- 把电视助手代码放进本仓库
- 在本仓库实现 `input keyevent`（注入只发生在电视端）

缺规格就问，不要猜。
