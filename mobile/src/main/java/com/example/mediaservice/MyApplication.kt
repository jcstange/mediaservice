package com.example.mediaservice

import android.app.Application
import com.example.mediaservice.koin.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
           androidLogger()
           androidContext(this@MyApplication)
           modules(appModules)
        }
    }
}