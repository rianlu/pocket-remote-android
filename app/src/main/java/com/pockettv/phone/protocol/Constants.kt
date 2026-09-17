package com.pockettv.phone.protocol

/** 协议冻结常量，必须与 docs/PROTOCOL.md 逐字一致。 */
object Constants {
    const val CONTROL_PORT = 17880
    const val UDP_PORT = 17882
    const val NSD_TYPE = "_pockettv._tcp."
    const val NSD_NAME = "PocketTV"
    const val WS_PATH = "/ws"
    const val UDP_MAGIC = "PTVDISC1"
    const val PROTOCOL_V = 1
    const val DIR_INBOX = "inbox"
    const val DIR_APK = "apk"

    const val TYPE_HELLO = "hello"
    const val TYPE_HELLO_OK = "hello_ok"
    const val TYPE_NEED_PIN = "need_pin"
    const val TYPE_ERROR = "error"
    const val TYPE_KEY = "key"
    const val TYPE_TEXT = "text"
    const val TYPE_APPS = "apps"
    const val TYPE_APPS_OK = "apps_ok"
    const val TYPE_APP_OPEN = "app_open"
    const val TYPE_APP_UNINSTALL = "app_uninstall"

    const val KEY_UP = 19
    const val KEY_DOWN = 20
    const val KEY_LEFT = 21
    const val KEY_RIGHT = 22
    const val KEY_OK = 23
    const val KEY_BACK = 4
    const val KEY_HOME = 3
    const val KEY_MENU = 82
    const val KEY_VOL_UP = 24
    const val KEY_VOL_DOWN = 25
    const val KEY_MUTE = 164
}
