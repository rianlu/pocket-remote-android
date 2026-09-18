# Pocket Remote

**口袋遥控** — Android 手机遥控客户端。

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-3DDC84.svg?logo=android&logoColor=white)](https://www.android.com)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://developer.android.com/guide/topics/manifest/uses-sdk-element#ApiLevels)
[![Version](https://img.shields.io/badge/version-0.2.5-blue.svg)](https://github.com/rianlu/pocket-remote-android)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)

在同一 Wi-Fi 下控制已安装 [口袋遥控助手](https://github.com/rianlu/pocket-remote-helper-android) 的安卓电视 / 盒子。手机只发协议，不在本机注入按键。

| | |
|---|---|
| 桌面名 | 口袋遥控 |
| GitHub | https://github.com/rianlu/pocket-remote-android |
| applicationId | `com.pocketremote` |
| 版本 | 0.2.5 |
| minSdk / targetSdk | 26（Android 8.0） / 35 |
| UI | Jetpack Compose · Material 3 |
| 配对仓库 | [pocket-remote-helper-android](https://github.com/rianlu/pocket-remote-helper-android) |

## 功能

- 局域网发现（NSD，失败则 UDP，再失败手填 IP）
- 6 位 PIN 配对，token 持久化
- 遥控：方向键 / 确认 / 返回 / 主页 / 菜单 / 音量 / 数字；增强模式下相对鼠标
- 键盘：向电视当前焦点发文本
- 应用：打开、卸载（用户应用）、提取 APK、从手机安装
- 设置：连接信息只读、外观、清理电视后台；设备信息为子页
- 连接后底栏 Tab（遥控 / 键盘 / 应用 / 设置）保活，共用一条 WebSocket

## 不做

投屏、电源键、完整文件管理器、输入法、无障碍、ADB 当遥控通道。

## 要求

- Android 8.0 及以上手机
- 与电视同一局域网
- 电视上已打开「口袋遥控助手」

连接前可选 **增强**（系统插件）或 **ADB**（需电视打开网络调试），连上后本会话不可改。

## 构建

```bash
export ANDROID_HOME=/path/to/Android/sdk
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

真机与盒子同一 Wi-Fi：打开「口袋遥控」→ 搜索或「手动添加」填 IP。模拟器里的电视请用 `adb forward`，不要填 Mac 上看不到的模拟器地址。

## 文档

| 文件 | 内容 |
|---|---|
| [`docs/PROTOCOL.md`](docs/PROTOCOL.md) | 协议权威文本（须与助手仓库逐字一致） |
| [`docs/SPEC.md`](docs/SPEC.md) | 本端实现 |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | 形态与范围 |
| [`AGENTS.md`](AGENTS.md) | 给贡献者 / AI 的仓库规矩 |

## 相关仓库

- 电视助手：[rianlu/pocket-remote-helper-android](https://github.com/rianlu/pocket-remote-helper-android)

## 许可证

[Apache License 2.0](LICENSE)。可商用、可修改、可再分发，保留版权与许可证声明即可。
