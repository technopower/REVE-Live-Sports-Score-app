package com.example.domain.notification

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationSettings(
    val goalAlerts: Boolean = true,
    val matchStartAlerts: Boolean = true,
    val matchResultAlerts: Boolean = true,
    val redCardAlerts: Boolean = true,
    val favoriteTeamAlerts: Boolean = true,
    val halfTimeAlerts: Boolean = true
) {
    // Convenience backward compatibility property
    val matchStarting: Boolean get() = matchStartAlerts
    val fullTimeAlerts: Boolean get() = matchResultAlerts
}

object NotificationPreferences {
    private const val PREFS_NAME = "reve_notification_prefs"
    private const val KEY_GOAL_ALERTS = "key_goal_alerts"
    private const val KEY_MATCH_START_ALERTS = "key_match_start_alerts"
    private const val KEY_MATCH_RESULT_ALERTS = "key_match_result_alerts"
    private const val KEY_RED_CARD_ALERTS = "key_red_card_alerts"
    private const val KEY_FAVORITE_TEAM_ALERTS = "key_favorite_team_alerts"
    private const val KEY_HALF_TIME_ALERTS = "key_half_time_alerts"

    private var sharedPreferences: SharedPreferences? = null

    private val _settings = MutableStateFlow(NotificationSettings())
    val settings: StateFlow<NotificationSettings> = _settings.asStateFlow()

    fun init(context: Context) {
        if (sharedPreferences == null) {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPreferences = prefs
            loadFromPreferences(prefs)
        }
    }

    private fun loadFromPreferences(prefs: SharedPreferences) {
        val loaded = NotificationSettings(
            goalAlerts = prefs.getBoolean(KEY_GOAL_ALERTS, true),
            matchStartAlerts = prefs.getBoolean(KEY_MATCH_START_ALERTS, true),
            matchResultAlerts = prefs.getBoolean(KEY_MATCH_RESULT_ALERTS, true),
            redCardAlerts = prefs.getBoolean(KEY_RED_CARD_ALERTS, true),
            favoriteTeamAlerts = prefs.getBoolean(KEY_FAVORITE_TEAM_ALERTS, true),
            halfTimeAlerts = prefs.getBoolean(KEY_HALF_TIME_ALERTS, true)
        )
        _settings.value = loaded
    }

    fun toggleGoalAlerts(enabled: Boolean) {
        _settings.value = _settings.value.copy(goalAlerts = enabled)
        sharedPreferences?.edit()?.putBoolean(KEY_GOAL_ALERTS, enabled)?.apply()
    }

    fun toggleMatchStartAlerts(enabled: Boolean) {
        _settings.value = _settings.value.copy(matchStartAlerts = enabled)
        sharedPreferences?.edit()?.putBoolean(KEY_MATCH_START_ALERTS, enabled)?.apply()
    }

    // Convenience alias for backward compatibility
    fun toggleMatchStarting(enabled: Boolean) = toggleMatchStartAlerts(enabled)

    fun toggleMatchResultAlerts(enabled: Boolean) {
        _settings.value = _settings.value.copy(matchResultAlerts = enabled)
        sharedPreferences?.edit()?.putBoolean(KEY_MATCH_RESULT_ALERTS, enabled)?.apply()
    }

    fun toggleRedCardAlerts(enabled: Boolean) {
        _settings.value = _settings.value.copy(redCardAlerts = enabled)
        sharedPreferences?.edit()?.putBoolean(KEY_RED_CARD_ALERTS, enabled)?.apply()
    }

    fun toggleFavoriteTeamAlerts(enabled: Boolean) {
        _settings.value = _settings.value.copy(favoriteTeamAlerts = enabled)
        sharedPreferences?.edit()?.putBoolean(KEY_FAVORITE_TEAM_ALERTS, enabled)?.apply()
    }

    fun toggleHalfTimeAlerts(enabled: Boolean) {
        _settings.value = _settings.value.copy(halfTimeAlerts = enabled)
        sharedPreferences?.edit()?.putBoolean(KEY_HALF_TIME_ALERTS, enabled)?.apply()
    }

    fun shouldShowAlert(type: String): Boolean {
        val s = _settings.value
        return when (type.lowercase()) {
            "goal" -> s.goalAlerts
            "red_card", "redcard" -> s.redCardAlerts
            "match_starting", "start", "kickoff" -> s.matchStartAlerts
            "half_time", "ht" -> s.halfTimeAlerts
            "full_time", "ft", "result" -> s.matchResultAlerts
            "favorite_team", "team" -> s.favoriteTeamAlerts
            else -> true
        }
    }
}
