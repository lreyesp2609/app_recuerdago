package com.example.recuerdago

import android.app.Application
import org.osmdroid.config.Configuration

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().userAgentValue = packageName
    }
}