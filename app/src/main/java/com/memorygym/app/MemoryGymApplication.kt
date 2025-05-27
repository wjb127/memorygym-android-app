package com.memorygym.app

import android.app.Application
import com.memorygym.app.ads.AdManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MemoryGymApplication : Application() {
    
    @Inject
    lateinit var adManager: AdManager
    
    override fun onCreate() {
        super.onCreate()
        
        // AdMob 초기화
        adManager.initialize(this)
        // 첫 번째 광고 미리 로드
        adManager.loadInterstitialAd(this)
    }
} 