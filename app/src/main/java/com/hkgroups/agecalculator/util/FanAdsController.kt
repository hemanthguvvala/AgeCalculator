package com.hkgroups.agecalculator.util

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdSettings
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.facebook.ads.NativeAd
import com.facebook.ads.NativeAdListener

/**
 * FanAdsController — single owner of Facebook Audience Network state.
 *
 * One instance lives in the Application; everywhere else accesses it through
 * the [LocalAdController] composition local. Centralizing here means we can
 * later swap to a mediator (AppLovin MAX) without touching screen code.
 *
 * **Rate-limiting** is applied here, not at the call site, because it's a
 * cross-screen concern: an interstitial can only show every 90 seconds, never
 * within the first 30 seconds of a session, and never if ads are disabled
 * (premium tier or settings opt-out).
 */
class FanAdsController(
    private val applicationContext: Context,
    private val adsDisabledProvider: () -> Boolean = { false }
) {
    companion object {
        private const val TAG = "FanAds"

        // Real placement IDs from the FAN dashboard. Constants are not secrets —
        // they're embedded in shipped binaries by every other app on the network.
        const val BANNER_PLACEMENT = "1294666858897421_1294668815563892"
        const val INTERSTITIAL_PLACEMENT = "1294666858897421_1294670082230432"
        const val NATIVE_PLACEMENT = "1294666858897421_1294772915553482"

        // Rewarded video isn't available for non-game apps in this FAN account
        // — leave null. Banner + interstitial + native is the full stack.
        val REWARDED_PLACEMENT: String? = null

        private const val MIN_INTERSTITIAL_GAP_MS = 90_000L
        private const val SESSION_GRACE_MS = 30_000L
    }

    private val sessionStart = System.currentTimeMillis()
    private var lastInterstitialShownAt = 0L
    private var pendingInterstitial: InterstitialAd? = null
    private var pendingInterstitialReady = false
    private var initialized = false

    /** Idempotent. Initializes the SDK and starts preloading the first interstitial. */
    fun initialize() {
        if (initialized) return
        initialized = true

        // Test mode auto-enabled on debug builds so we never burn real impressions
        // during dev. Production builds serve real ads.
        if (com.hkgroups.agecalculator.BuildConfig.DEBUG) {
            AdSettings.setTestMode(true)
        }

        AudienceNetworkAds
            .buildInitSettings(applicationContext)
            .withInitListener { result ->
                Log.d(TAG, "FAN init: success=${result.isSuccess} message=${result.message}")
                if (result.isSuccess) preloadInterstitial()
            }
            .initialize()
    }

    /** Returns a fresh `AdView` for the caller to host — typically inside an
     *  AndroidView in Compose. The caller is responsible for `destroy()` on
     *  composable disposal. */
    fun createBannerAdView(context: Context): AdView? {
        if (adsDisabledProvider()) return null
        val view = AdView(context, BANNER_PLACEMENT, AdSize.BANNER_HEIGHT_50)
        view.loadAd(view.buildLoadAdConfig().build())
        return view
    }

    /**
     * Creates and starts loading a native ad. Listener fires `onLoaded` when
     * the ad surface is ready to render via NativeAdLayout.
     *
     * Caller owns the lifetime — must call `destroy()` on disposal.
     */
    fun createNativeAd(
        context: Context,
        onLoaded: (NativeAd) -> Unit,
        onFailed: (String) -> Unit = {}
    ): NativeAd? {
        if (adsDisabledProvider()) return null
        val ad = NativeAd(context, NATIVE_PLACEMENT)
        ad.loadAd(
            ad.buildLoadAdConfig()
                .withAdListener(object : NativeAdListener {
                    override fun onAdLoaded(p0: Ad?) {
                        if (p0 === ad && ad.isAdLoaded && !ad.isAdInvalidated) {
                            onLoaded(ad)
                        }
                    }
                    override fun onError(p0: Ad?, error: AdError?) {
                        onFailed(error?.errorMessage ?: "unknown")
                    }
                    override fun onAdClicked(p0: Ad?) {}
                    override fun onLoggingImpression(p0: Ad?) {}
                    override fun onMediaDownloaded(p0: Ad?) {}
                })
                .build()
        )
        return ad
    }

    /** Preload the next interstitial in the background. Runs after init and
     *  again after each show so one is always warm when needed. */
    private fun preloadInterstitial() {
        if (adsDisabledProvider()) return
        val ad = InterstitialAd(applicationContext, INTERSTITIAL_PLACEMENT)
        ad.loadAd(
            ad.buildLoadAdConfig()
                .withAdListener(object : InterstitialAdListener {
                    override fun onAdLoaded(p0: Ad?) {
                        pendingInterstitialReady = true
                        Log.d(TAG, "Interstitial loaded")
                    }

                    override fun onError(p0: Ad?, error: AdError?) {
                        pendingInterstitialReady = false
                        Log.w(TAG, "Interstitial load failed: ${error?.errorMessage}")
                    }

                    override fun onAdClicked(p0: Ad?) {}
                    override fun onLoggingImpression(p0: Ad?) {}
                    override fun onInterstitialDisplayed(p0: Ad?) {}
                    override fun onInterstitialDismissed(p0: Ad?) {
                        // After dismissal the ad is consumed — release it and
                        // start preloading the next one for the next show.
                        pendingInterstitial?.destroy()
                        pendingInterstitial = null
                        pendingInterstitialReady = false
                        preloadInterstitial()
                    }
                })
                .build()
        )
        pendingInterstitial = ad
    }

    /**
     * Tries to show the preloaded interstitial. Silently no-ops if:
     *  - ads are disabled (premium / opt-out)
     *  - within session grace period (first 30s)
     *  - within rate-limit window (last show < 90s ago)
     *  - no preloaded ad ready
     *
     * Returns true if it actually showed.
     */
    fun showInterstitialIfEligible(activity: Activity): Boolean {
        if (adsDisabledProvider()) return false
        val now = System.currentTimeMillis()
        if (now - sessionStart < SESSION_GRACE_MS) return false
        if (now - lastInterstitialShownAt < MIN_INTERSTITIAL_GAP_MS) return false
        val ad = pendingInterstitial ?: return false
        if (!pendingInterstitialReady || !ad.isAdLoaded || ad.isAdInvalidated) return false

        ad.show()
        lastInterstitialShownAt = now
        return true
    }
}

/** Composition-local hook so any composable can request ads without DI plumbing. */
val LocalAdController = compositionLocalOf<FanAdsController?> { null }
