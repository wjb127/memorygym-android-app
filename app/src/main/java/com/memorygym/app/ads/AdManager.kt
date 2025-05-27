package com.memorygym.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.memorygym.app.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor() {
    
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    
    companion object {
        private const val TAG = "AdManager"
        // BuildConfig에서 광고 단위 ID 가져오기 (보안)
        private val INTERSTITIAL_AD_UNIT_ID = BuildConfig.ADMOB_INTERSTITIAL_ID
    }
    
    fun initialize(context: Context) {
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "AdMob initialized: ${initializationStatus.adapterStatusMap}")
        }
    }
    
    fun loadInterstitialAd(context: Context) {
        if (isLoading || interstitialAd != null) {
            Log.d(TAG, "Ad is already loading or loaded")
            return
        }
        
        isLoading = true
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Failed to load interstitial ad: ${adError.message}")
                    interstitialAd = null
                    isLoading = false
                }
                
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isLoading = false
                    
                    // 광고 콜백 설정
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            Log.d(TAG, "Ad was clicked")
                        }
                        
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad dismissed fullscreen content")
                            interstitialAd = null
                            // 광고가 닫힌 후 새로운 광고 로드
                            loadInterstitialAd(context)
                        }
                        
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "Ad failed to show fullscreen content: ${adError.message}")
                            interstitialAd = null
                        }
                        
                        override fun onAdImpression() {
                            Log.d(TAG, "Ad recorded an impression")
                        }
                        
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Ad showed fullscreen content")
                        }
                    }
                }
            }
        )
    }
    
    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit = {}) {
        if (interstitialAd != null) {
            Log.d(TAG, "Showing interstitial ad")
            
            // 광고 닫힘 콜백 추가
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad dismissed fullscreen content")
                    interstitialAd = null
                    onAdClosed()
                    // 새로운 광고 로드
                    loadInterstitialAd(activity)
                }
                
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Ad failed to show fullscreen content: ${adError.message}")
                    interstitialAd = null
                    onAdClosed()
                }
                
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content")
                }
            }
            
            interstitialAd?.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad is not ready")
            onAdClosed()
        }
    }
    
    fun isAdReady(): Boolean {
        return interstitialAd != null
    }
} 