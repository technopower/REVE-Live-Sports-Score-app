package com.example.domain.ad

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AdManager provides clean abstraction for Banner, Native, Interstitial, and Rewarded Ads.
 * Allows ads to be safely toggled, disabled for Pro subscribers, and monitored without
 * breaking or cluttering the core sports UI.
 */
object AdManager {
    private val _adsEnabled = MutableStateFlow(true)
    val adsEnabled: StateFlow<Boolean> = _adsEnabled.asStateFlow()

    private val _bannerAdUnitId = MutableStateFlow("ca-app-pub-3940256099942544/6300978111") // Google test banner ID
    val bannerAdUnitId: StateFlow<String> = _bannerAdUnitId.asStateFlow()

    fun setAdsEnabled(enabled: Boolean) {
        _adsEnabled.value = enabled
    }

    fun showInterstitial(onDismiss: () -> Unit = {}) {
        // Safe mock/production hook without disrupting live matches
        if (_adsEnabled.value) {
            // Trigger interstitial listener
            onDismiss()
        } else {
            onDismiss()
        }
    }
}
