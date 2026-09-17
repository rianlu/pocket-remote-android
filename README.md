# PocketTV Remote (Android)

安卓手机遥控客户端（口袋遥控）。配对电视助手 [pockettv-helper-android](https://github.com/PLACEHOLDER/pockettv-helper-android) 后，在局域网控制安卓电视/盒子。

| | |
|---|---|
| applicationId | `com.pockettv.phone` |
| minSdk | 26（Android 8.0） |
| 协议（权威） | [`docs/PROTOCOL.md`](docs/PROTOCOL.md) |
| 本端规格 | [`docs/SPEC.md`](docs/SPEC.md) |
| 架构说明 | [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) |
| AI 开发说明 | [`AGENTS.md`](AGENTS.md) |

本仓库只含手机端。

## 构建

```bash
export ANDROID_HOME=/path/to/Android/sdk
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

与电视助手同一 Wi-Fi：打开「口袋遥控」→ 搜索或手填 IP（模拟器里的电视请填 Mac 上看不到的地址，需 `adb reverse`/`forward` 视拓扑而定；真机连真盒子最直接）。

许可证：[Apache License 2.0](LICENSE)。可商用、可修改、可再分发，保留版权与许可证声明即可。
