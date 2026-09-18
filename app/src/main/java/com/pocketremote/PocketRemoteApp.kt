package com.pocketremote

import android.app.Application
import com.pocketremote.ui.theme.ThemeStore

/** 手机遥控 Application。 */
class PocketRemoteApp : Application() {
    lateinit var themeStore: ThemeStore
        private set

    override fun onCreate() {
        super.onCreate()
        themeStore = ThemeStore(this)
    }
}
