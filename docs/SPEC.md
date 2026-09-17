# PocketTV Remote（Android 手机端）规格

本仓库只实现**手机遥控客户端**。协议以 [`PROTOCOL.md`](PROTOCOL.md) 为准（须与 helper 仓库同名文件一致）。背景见 [`ARCHITECTURE.md`](ARCHITECTURE.md)。

- 桌面名：口袋遥控
- applicationId：`com.pockettv.phone`
- minSdk **26**（Android 8.0），compileSdk 35，targetSdk 35
- UI：Jetpack Compose，中文
- 网络：OkHttp 4 + WebSocket；发现用 `NsdManager`，失败则 UDP，再失败手填 IP

不要在本仓库写电视助手、不要写鸿蒙。

---

## 职责

手机只发协议消息，**不注入按键**。发现设备 → PIN 配对 → 发 `key`/`text` → HTTP 传文件 → 应用列表/打开/卸载。

不做：输入法、无障碍、自动弹键盘、完整文件管理器、ADB 遥控、MQTT、广告 SDK。

---

## 界面（最少）

1. 设备列表：NSD / UDP / 手填 IP
2. PIN 输入
3. 遥控板：方向、确认、返回、主页、菜单、音量、数字、键盘按钮
4. 键盘页：输入文本后发送 `text`（不监听电视焦点）
5. 传文件：选本机文件，PUT 到 `inbox` 或 `apk`
6. 应用列表：`apps` → 打开 / 卸载

`hello_ok.sdk < 21` 且文本含非 ASCII 时 toast「中文可能无效」，仍允许发送。  
`injectOk == false` 时提示按键可能无效。

按键本地点击立即反馈，不要等 ACK 再亮按钮。最短间隔约 80ms。

权限：`INTERNET`、`ACCESS_WIFI_STATE`、`CHANGE_WIFI_MULTICAST_STATE`；Android 13+ 按需 `NEARBY_WIFI_DEVICES`。明文 HTTP 仅 RFC1918，用 `networkSecurityConfig`。

Token 存 EncryptedSharedPreferences（或等价加密存储）。

---

## 验收顺序

1. 手填 IP，hello / hello_ok（电视已启动）
2. PIN 配对，token 重启后仍可用
3. NSD，失败 UDP `PTVDISC1`
4. 遥控板按键在真盒子上有效
5. 键盘发送英文；中文按 sdk 提示
6. 上传 APK
7. 应用列表 / 打开 / 卸载

主测试机：API 21+ 手机 + Android 4.2–4.4 盒子 + 一台 API 21+ 盒子。
