# Pocket Remote（Android 手机端）规格

本仓库只实现**手机遥控客户端**。协议以 [`PROTOCOL.md`](PROTOCOL.md) 为准（须与 helper 仓库同名文件一致）。背景见 [`ARCHITECTURE.md`](ARCHITECTURE.md)。

- 桌面名：口袋遥控
- applicationId：`com.pocketremote`
- minSdk **26**（Android 8.0），compileSdk 35，targetSdk 35
- UI：Jetpack Compose + Material 3；跟随系统深浅色、壁纸动态色（Android 12+）、可选手动主题色；启动图标为自适应图标（含单色层）
- 网络：OkHttp 4 + WebSocket；发现用 `NsdManager`，失败则 UDP，再失败手填 IP

不要在本仓库写电视助手、不要写鸿蒙。

---

## 职责

手机只发协议消息，**不注入按键**。发现设备 → PIN 配对 → 发 `key` / `text` / `pointer` → HTTP 传文件 → 应用列表/打开/卸载。

不做：输入法、无障碍、自动弹键盘、完整文件管理器（不浏览/下载电视文件）、触控滑动切焦点、ADB 遥控、MQTT、广告 SDK。

---

## 功能

发现与配对：搜索局域网（NSD，失败 UDP）、6 位 PIN、token 持久化。连接页在连上前可选 **增强**（`plugin`）或 **ADB**（`adb`），写入 `hello.inject`；连上后不可改，断开回连接页才能再选。找不到设备时点「手动添加」弹窗手填 IP（低频）。WebSocket 15s ping；异常断开自动重连（非用户点断开，沿用本次已选通道）。

遥控，两种模式，默认**按键**，可切**鼠标**：

- 按键：方向、确认、返回、主页、菜单、音量加减/静音、设置、信号源、数字 0–9（可连按、可删一位）
- 鼠标：相对移动电视光标 + 单击；同时可返回、主页、音量。非增强时「鼠标」灰色，点按提示不支持
- 本地点击立即有按压反馈，不等电视 ACK；方向键最短间隔约 80ms
- 可用连接模式只有：增强 / ADB / 标准（4.x）。`none` 不是遥控模式，表示方向键等注入失败，界面提示打开电视网络调试，不把它当一种可用模式。

键盘：向电视当前焦点发文本（中英文）；删除、清空。不监听电视输入框。

应用：只列出电视上可打开的应用（LAUNCHER / Leanback），带图标、名称、占用大小；搜索、刷新。点击列表项弹出菜单（打开；非系统可卸载；可提取则提取）。系统应用、分体包不提供卸载/提取（系统包提取会得到损坏文件）。可从本机安装 APK。

设置：电视名称、地址、连接模式（只读）、助手版本；外观（深浅色、主题色）；清理后台为列表项（不删缓存）。设备信息为子页（厂商、品牌、Android/API、固件、硬件、SoC、CPU、ABI、MAC、分辨率、DPI、内存、存储）。瞬时提示用 Snackbar，不在各页顶栏重复。`info` / `clean`。

已连接后可在遥控、键盘、应用、设置之间切换（底栏 Tab，四页保活不销毁；电视连接为全局一份 WebSocket）。设备信息叠在设置上。不做投屏。

文本由电视剪贴板粘贴。设置键发 `176`，由电视端打开系统设置。信号源发 `178`（智能电视切输入源；无电视输入的盒子可能无效果）。

权限：`INTERNET`、`ACCESS_WIFI_STATE`、`CHANGE_WIFI_MULTICAST_STATE`；Android 13+ 按需 `NEARBY_WIFI_DEVICES`。明文 HTTP 仅 RFC1918，用 `networkSecurityConfig`。

Token 存 EncryptedSharedPreferences（或等价加密存储）。

---

## 验收顺序

1. 手填 IP，hello / hello_ok（电视已启动）
2. PIN 配对，token 重启后仍可用
3. NSD，失败 UDP `PKTREMT1`
4. 按键模式在真机上有效；设置打开电视系统设置页
5. 键盘发送英文；中文可用；删除/清空
6. 增强模式下鼠标光标可见、单击有效
7. 上传 APK（有进度）并调起安装
8. 应用列表 / 搜索 / 打开 / 卸载确认

主测试机：Android 8+ 手机 + Android 4.2–4.4 盒子 + 一台较新盒子或智能电视。
