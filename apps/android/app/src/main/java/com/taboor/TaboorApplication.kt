package com.taboor

import android.app.Application
import com.taboor.core.localization.LocaleManager

class TaboorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LocaleManager.init(this)
    }
}
