package com.memorygym.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MemoryGymApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
} 