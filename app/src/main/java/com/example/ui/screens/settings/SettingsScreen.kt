package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.NetworkClient
import com.example.domain.billing.SubscriptionManager
import com.example.domain.billing.SubscriptionTier
import com.example.domain.notification.NotificationPreferences
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notificationSettings by NotificationPreferences.settings.collectAsState()
    val tier by SubscriptionManager.tier.collectAsState()
    var apiUrlInput by remember { mutableStateOf(NetworkClient.baseUrl) }
    var urlSavedMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SETTINGS & PRO",
                        color = SportsTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("settings_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SportsTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SportsBackground)
            )
        },
        containerColor = SportsBackground,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SUBSCRIPTION PRO CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SportsCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(SportsNeonGreen, SportsCardSecondary)
                    ),
                    width = 1.5.dp
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Pro",
                                tint = SportsNeonGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REVE PRO PASS",
                                color = SportsNeonGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (tier != SubscriptionTier.FREE) SportsNeonGreen else SportsCardSecondary)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (tier != SubscriptionTier.FREE) "ACTIVE" else "FREE",
                                color = if (tier != SubscriptionTier.FREE) SportsBackground else SportsTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Ad-free experience across all live matches\n• Advanced analytics and live match heatmaps\n• Instant push notifications for favorite teams",
                        color = SportsTextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (tier == SubscriptionTier.FREE) {
                                SubscriptionManager.upgradeToPro()
                            } else {
                                SubscriptionManager.downgradeToFree()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tier == SubscriptionTier.FREE) SportsNeonGreen else SportsCardSecondary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subscription_toggle_btn")
                    ) {
                        Text(
                            text = if (tier == SubscriptionTier.FREE) "UPGRADE TO PRO (TEST)" else "SWITCH TO FREE TIER",
                            color = if (tier == SubscriptionTier.FREE) SportsBackground else SportsTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // NOTIFICATION PREFERENCES
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SportsCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NOTIFICATION PREFERENCES",
                        color = SportsNeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    NotificationToggleRow(
                        title = "Goal & Score Alerts",
                        checked = notificationSettings.goalAlerts,
                        onCheckedChange = { NotificationPreferences.toggleGoalAlerts(it) },
                        testTag = "toggle_goal_alerts"
                    )
                    NotificationToggleRow(
                        title = "Red Card Alerts",
                        checked = notificationSettings.redCardAlerts,
                        onCheckedChange = { NotificationPreferences.toggleRedCardAlerts(it) },
                        testTag = "toggle_red_card_alerts"
                    )
                    NotificationToggleRow(
                        title = "Match Start Alerts",
                        checked = notificationSettings.matchStartAlerts,
                        onCheckedChange = { NotificationPreferences.toggleMatchStartAlerts(it) },
                        testTag = "toggle_match_start_alerts"
                    )
                    NotificationToggleRow(
                        title = "Match Result Alerts (FT)",
                        checked = notificationSettings.matchResultAlerts,
                        onCheckedChange = { NotificationPreferences.toggleMatchResultAlerts(it) },
                        testTag = "toggle_match_result_alerts"
                    )
                    NotificationToggleRow(
                        title = "Favorite Team Alerts",
                        checked = notificationSettings.favoriteTeamAlerts,
                        onCheckedChange = { NotificationPreferences.toggleFavoriteTeamAlerts(it) },
                        testTag = "toggle_favorite_team_alerts"
                    )
                }
            }

            // BACKEND API SERVER CONFIGURATION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SportsCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FASTAPI BACKEND CONFIGURATION",
                        color = SportsNeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Configure the REST API endpoint used by the applet:",
                        color = SportsTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = apiUrlInput,
                        onValueChange = {
                            apiUrlInput = it
                            urlSavedMessage = false
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SportsCardSecondary,
                            unfocusedContainerColor = SportsCardSecondary,
                            focusedBorderColor = SportsNeonGreen,
                            unfocusedBorderColor = SportsBorder,
                            focusedTextColor = SportsTextPrimary,
                            unfocusedTextColor = SportsTextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            NetworkClient.updateBaseUrl(apiUrlInput)
                            urlSavedMessage = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SportsCardSecondary),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Apply & Reconnect", color = SportsNeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (urlSavedMessage) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SportsNeonGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("API Base URL updated successfully", color = SportsNeonGreen, fontSize = 12.sp)
                        }
                    }
                }
            }

            // PRIVACY & LEGAL
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SportsCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PRIVACY & ABOUT",
                        color = SportsNeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "REVE Live Sports v1.0.0\nLicensed Sports Data API Integration.\nLive scores and statistics are provided for information purposes.",
                        color = SportsTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = SportsTextPrimary, fontSize = 13.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SportsNeonGreen,
                checkedTrackColor = SportsCardSecondary,
                uncheckedTrackColor = SportsCardSecondary,
                uncheckedThumbColor = SportsTextSecondary
            )
        )
    }
}
