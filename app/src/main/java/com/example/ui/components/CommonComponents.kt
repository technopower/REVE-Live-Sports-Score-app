package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MatchDto
import com.example.data.model.TeamDto
import com.example.ui.theme.*

@Composable
fun LiveBadge(
    modifier: Modifier = Modifier,
    text: String = "LIVE"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SportsLiveRed.copy(alpha = 0.18f))
            .border(1.dp, SportsLiveRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(SportsLiveRed.copy(alpha = alpha))
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            color = SportsLiveRed,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun TeamLogo(
    team: TeamDto,
    modifier: Modifier = Modifier,
    size: Int = 36
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(SportsCardSecondary)
            .border(1.dp, SportsBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!team.logoUrl.isNullOrEmpty()) {
            AsyncImage(
                model = team.logoUrl,
                contentDescription = team.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            val initial = team.name.take(1).uppercase()
            Text(
                text = initial,
                color = SportsNeonGreen,
                fontWeight = FontWeight.Bold,
                fontSize = (size * 0.45).sp
            )
        }
    }
}

@Composable
fun MatchCard(
    match: MatchDto,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("match_card_${match.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SportsCard
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                colors = listOf(SportsBorder, SportsCardSecondary)
            ),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: League & Status/Minute & Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = match.league.name,
                        color = SportsTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = " • ${match.sport}",
                        color = SportsNeonGreen.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (match.status.equals("LIVE", ignoreCase = true)) {
                        LiveBadge(modifier = Modifier.padding(end = 6.dp))
                        Text(
                            text = match.minute ?: "",
                            color = SportsLiveRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (match.status.equals("FINISHED", ignoreCase = true)) {
                        Text(
                            text = "FT",
                            color = SportsTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = match.startTime,
                            color = SportsTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(start = 4.dp)
                            .testTag("favorite_btn_${match.id}")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) SportsLiveRed else SportsTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scoreboard Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamLogo(team = match.homeTeam, size = 32)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.homeTeam.name,
                        color = SportsTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Scores or VS
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SportsCardSecondary)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (match.status.equals("UPCOMING", ignoreCase = true)) {
                        Text(
                            text = "VS",
                            color = SportsNeonGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "${match.score.home} - ${match.score.away}",
                            color = if (match.status.equals("LIVE", ignoreCase = true)) SportsNeonGreen else SportsTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Away Team
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = match.awayTeam.name,
                        color = SportsTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TeamLogo(team = match.awayTeam, size = 32)
                }
            }

            // Cricket extra details row if present
            if (match.cricketDetails != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SportsCardSecondary.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = match.cricketDetails.currentBatsman,
                        color = SportsNeonGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "RR: ${match.cricketDetails.runRate}",
                        color = SportsTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SportSelector(
    selectedSport: String,
    onSportSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sports = listOf("All", "Football", "Cricket", "Basketball", "Tennis")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sports.forEach { sport ->
            val isSelected = selectedSport.equals(sport, ignoreCase = true)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) SportsNeonGreen else SportsCard)
                    .border(
                        1.dp,
                        if (isSelected) SportsNeonGreen else SportsBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onSportSelected(sport) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("sport_tab_$sport"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sport,
                    color = if (isSelected) Color(0xFF06101B) else SportsTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatBar(
    title: String,
    homeValue: Int,
    awayValue: Int,
    isPercentage: Boolean = false,
    modifier: Modifier = Modifier
) {
    val total = (homeValue + awayValue).coerceAtLeast(1)
    val homeRatio = homeValue.toFloat() / total.toFloat()

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isPercentage) "$homeValue%" else "$homeValue",
                color = SportsNeonGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = SportsTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (isPercentage) "$awayValue%" else "$awayValue",
                color = SportsLiveRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(SportsCardSecondary)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(homeRatio.coerceAtLeast(0.02f))
                    .background(SportsNeonGreen)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight((1f - homeRatio).coerceAtLeast(0.02f))
                    .background(SportsLiveRed)
            )
        }
    }
}

@Composable
fun BannerAdPlaceholder(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SportsCardSecondary.copy(alpha = 0.7f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(SportsBorder, SportsCard)),
            width = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SportsBorder)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AD",
                        color = SportsTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REVE Sports Pro • Go Ad-Free & Unlock Deep Analytics",
                    color = SportsTextPrimary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "UPGRADE",
                color = SportsNeonGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
