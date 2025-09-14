package com.roocode.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RooCodeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}