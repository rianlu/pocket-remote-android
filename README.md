# Pocket Remote

**口袋遥控** — 用 Android 手机遥控安卓电视或盒子。

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-3DDC84.svg?logo=android&logoColor=white)](https://www.android.com)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://developer.android.com/guide/topics/manifest/uses-sdk-element#ApiLevels)
[![Version](https://img.shields.io/badge/version-0.2.9-blue.svg)](https://github.com/rianlu/pocket-remote-android)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)

手机和电视在同一 Wi-Fi。电视先打开 [口袋遥控助手](https://github.com/rianlu/pocket-remote-helper-android)，手机发现设备、输入 6 位配对码，之后方向键、鼠标、文字和应用操作都发给电视执行。手机自己不注入按键。

| | |
|---|---|
| 桌面名 | 口袋遥控 |
| applicationId | `com.pocketremote` |
| 版本 | 0.2.9 |
| minSdk / targetSdk | 26（Android 8.0） / 35 |
| UI | Jetpack Compose · Material 3 |
| 电视端 | [pocket-remote-helper-android](https://github.com/rianlu/pocket-remote-helper-android) |

## 界面

<p align="center">
  <img src="docs/screenshots/devices.png" width="220" alt="发现附近设备，选择增强或 ADB 模式">
  <img src="docs/screenshots/pin.png" width="220" alt="输入电视上的 6 位配对码">
  <img src="docs/screenshots/remote.png" width="220" alt="按键遥控">
</p>
<p align="center">
  <img src="docs/screenshots/mouse.png" width="220" alt="增强模式下的鼠标触控板">
  <img src="docs/screenshots/apps.png" width="220" alt="电视应用列表，可发送 APK">
  <img src="docs/screenshots/device.png" width="220" alt="我的设备：连接、外观和清理后台">
</p>

## 怎么用

1. 电视安装并打开「口袋遥控助手」。
2. 手机打开「口袋遥控」。同一 Wi-Fi 下会列出附近设备；搜不到就点右上角手动填 IP。
3. 连接前选择模式。连上之后，这一次会话不能改：
   - **增强模式**：电视装有系统插件，可以用鼠标和按键。
   - **ADB 模式**：电视已打开网络调试，只能发按键。
4. 输入电视上的 6 位配对码，满 6 位自动提交。配对结果会记住。

连上后底栏是 **遥控 / 应用 / 设备**，共用一条 WebSocket。

- **遥控**：返回、主页、菜单、方向键和确认、音量、静音、数字键盘。纸飞机把文字发到电视当前焦点。增强模式下可切换触控板，滑动移动光标，点按单击。
- **应用**：搜索电视上的应用，打开，卸载用户应用，或把手机里的 APK 发到电视安装。
- **设备**：查看连接地址，断开连接，改控制台颜色，开关按键振动，清理电视后台。清理只结束后台应用，不删文件，也不清缓存。

## 不做

投屏、电源键、完整文件管理器、输入法、无障碍。ADB 只作为注入通道，不是遥控协议本身。

## 构建

```bash
export ANDROID_HOME=/path/to/Android/sdk
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

模拟器里的电视请用 `adb forward`，不要填 Mac 上看不到的模拟器地址。

## 文档

| 文件 | 内容 |
|---|---|
| [`docs/PROTOCOL.md`](docs/PROTOCOL.md) | 协议权威文本（须与助手仓库逐字一致） |
| [`docs/SPEC.md`](docs/SPEC.md) | 本端实现 |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | 形态与范围 |
| [`AGENTS.md`](AGENTS.md) | 给贡献者 / AI 的仓库规矩 |

## 许可证

[Apache License 2.0](LICENSE)。可商用、可修改、可再分发，保留版权与许可证声明即可。
