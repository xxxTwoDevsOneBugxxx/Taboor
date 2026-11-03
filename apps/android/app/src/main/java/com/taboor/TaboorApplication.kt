package com.taboor

import android.app.Application
import com.taboor.core.di.AppContainer
import com.taboor.core.localization.LocaleManager

class TaboorApplication : Application() {

    val appContainer: AppContainer by lazy {
        AppContainer(this)
    }

    override fun onCreate() {
        super.onCreate()
        LocaleManager.init(this)
        com.taboor.core.services.SessionManager.init(this)
        com.taboor.core.services.BiometricService.init(this)
    }
}
