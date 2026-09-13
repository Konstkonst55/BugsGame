package com.kxnst.bugsgame

import android.app.Application
import com.kxnst.bugsgame.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BugsGameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@BugsGameApplication)
            modules(appModule)
        }
    }
}
