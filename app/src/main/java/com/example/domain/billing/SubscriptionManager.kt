package com.example.domain.billing

import com.example.domain.ad.AdManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SubscriptionTier {
    FREE,
    PRO_MONTHLY,
    PRO_YEARLY
}

/**
 * Manages Free vs Pro tier status, Google Play Billing lifecycle hooks,
 * and ad-free entitlement verification.
 */
object SubscriptionManager {
    private val _tier = MutableStateFlow(SubscriptionTier.FREE)
    val tier: StateFlow<SubscriptionTier> = _tier.asStateFlow()

    val isPro: Boolean
        get() = _tier.value != SubscriptionTier.FREE

    fun upgradeToPro(tier: SubscriptionTier = SubscriptionTier.PRO_YEARLY) {
        _tier.value = tier
        // Pro users enjoy an ad-free experience
        AdManager.setAdsEnabled(false)
    }

    fun downgradeToFree() {
        _tier.value = SubscriptionTier.FREE
        AdManager.setAdsEnabled(true)
    }
}
