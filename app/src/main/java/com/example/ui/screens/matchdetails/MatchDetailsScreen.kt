package com.example.ui.screens.matchdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.LiveBadge
import com.example.ui.components.StatBar
import com.example.ui.components.TeamLogo
import com.example.ui.theme.*
import com.example.ui.viewmodel.SportsViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    matchId: String,
    viewModel: SportsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(matchId) {
        viewModel.loadMatchDetails(matchId)
    }

    val matchDetailsState by viewModel.matchDetails.collectAsState()
    val isRefreshing by viewModel.isDetailsRefreshing.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val isFavorite = favorites.any { it.id == matchId }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Events", "Statistics", "Lineups", "Table")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Match Center",
                        color = SportsTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("match_details_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SportsTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val state = matchDetailsState
                            if (state is UiState.Success) {
                                viewModel.toggleFavoriteMatch(state.data.match)
                            }
                        },
                        modifier = Modifier.testTag("match_details_fav")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) SportsLiveRed else SportsTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SportsBackground)
            )
        },
        containerColor = SportsBackground,
        modifier = modifier
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshMatchDetails(matchId) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = matchDetailsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = SportsNeonGreen,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading match center...",
                                color = SportsTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SportsCard),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = SportsLiveRed,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Failed to load match details",
                                    color = SportsTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    color = SportsTextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.loadMatchDetails(matchId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SportsNeonGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("match_details_retry")
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = SportsBackground,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Retry",
                                        color = SportsBackground,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val data = state.data
                    val match = data.match

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 60.dp)
                    ) {
                        // HERO SCOREBOARD CARD
                        item {
                            ScoreboardHeroCard(
                                match = match,
                                isHomeFavorite = favorites.any { it.id == match.homeTeam.id },
                                isAwayFavorite = favorites.any { it.id == match.awayTeam.id },
                                onToggleHomeFavorite = { viewModel.toggleFavoriteTeam(match.homeTeam, match.sport) },
                                onToggleAwayFavorite = { viewModel.toggleFavoriteTeam(match.awayTeam, match.sport) }
                            )
                        }

                        // TABS ROW
                        item {
                            ScrollableTabRow(
                                selectedTabIndex = selectedTabIndex,
                                containerColor = SportsBackground,
                                contentColor = SportsNeonGreen,
                                edgePadding = 16.dp,
                                divider = { HorizontalDivider(color = SportsBorder, thickness = 1.dp) }
                            ) {
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTabIndex == index,
                                        onClick = { selectedTabIndex = index },
                                        text = {
                                            Text(
                                                text = title,
                                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                                color = if (selectedTabIndex == index) SportsNeonGreen else SportsTextSecondary,
                                                fontSize = 14.sp
                                            )
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // TAB CONTENT
                        when (selectedTabIndex) {
                            0 -> { // OVERVIEW
                                item {
                                    OverviewTabContent(match = match)
                                }
                            }
                            1 -> { // EVENTS
                                if (data.events.isEmpty()) {
                                    item {
                                        EmptyTabNotice("No match events recorded yet.")
                                    }
                                } else {
                                    items(data.events) { event ->
                                        EventTimelineRow(event = event)
                                    }
                                }
                            }
                            2 -> { // STATISTICS
                                item {
                                    StatisticsTabContent(stats = data.stats)
                                }
                            }
                            3 -> { // LINEUPS
                                item {
                                    LineupsTabContent(
                                        lineups = data.lineups,
                                        homeName = match.homeTeam.name,
                                        awayName = match.awayTeam.name
                                    )
                                }
                            }
                            4 -> { // TABLE
                                item {
                                    StandingsTabContent(standings = data.standings)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreboardHeroCard(
    match: MatchDto,
    isHomeFavorite: Boolean = false,
    isAwayFavorite: Boolean = false,
    onToggleHomeFavorite: () -> Unit = {},
    onToggleAwayFavorite: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SportsCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(listOf(SportsCardSecondary, SportsBorder)),
            width = 1.5.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // League and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.league.name,
                    color = SportsTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                if (match.status.equals("LIVE", ignoreCase = true)) {
                    LiveBadge(text = match.minute ?: "LIVE")
                } else {
                    Text(
                        text = match.status,
                        color = SportsNeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Teams and Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TeamLogo(team = match.homeTeam, size = 52)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = match.homeTeam.name,
                        color = SportsTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SportsCardSecondary)
                            .clickable { onToggleHomeFavorite() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("fav_home_team_${match.homeTeam.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isHomeFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite Home Team",
                            tint = if (isHomeFavorite) SportsLiveRed else SportsTextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHomeFavorite) "Favorited" else "Follow",
                            color = if (isHomeFavorite) SportsLiveRed else SportsTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Center Score
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (match.status.equals("UPCOMING", ignoreCase = true)) {
                        Text(
                            text = "VS",
                            color = SportsNeonGreen,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    } else {
                        Text(
                            text = "${match.score.home} - ${match.score.away}",
                            color = SportsNeonGreen,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    if (match.score.period != null) {
                        Text(
                            text = match.score.period,
                            color = SportsTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Away Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TeamLogo(team = match.awayTeam, size = 52)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = match.awayTeam.name,
                        color = SportsTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SportsCardSecondary)
                            .clickable { onToggleAwayFavorite() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("fav_away_team_${match.awayTeam.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isAwayFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite Away Team",
                            tint = if (isAwayFavorite) SportsLiveRed else SportsTextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAwayFavorite) "Favorited" else "Follow",
                            color = if (isAwayFavorite) SportsLiveRed else SportsTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewTabContent(match: MatchDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SportsCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "MATCH INFORMATION",
                color = SportsNeonGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Sport", value = match.sport.replaceFirstChar { it.uppercase() })
            InfoRow(label = "Competition", value = match.league.name)
            InfoRow(label = "Kickoff Time", value = match.startTime)
            match.venue?.let { InfoRow(label = "Venue", value = it) }
            match.referee?.let { InfoRow(label = "Referee", value = it) }

            if (match.cricketDetails != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = SportsBorder)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "CRICKET SCORECARD",
                    color = SportsNeonGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(label = "Current Batsman", value = match.cricketDetails.currentBatsman)
                InfoRow(label = "Current Bowler", value = match.cricketDetails.currentBowler)
                InfoRow(label = "Current Run Rate", value = "${match.cricketDetails.runRate}")
                match.cricketDetails.reqRunRate?.let {
                    InfoRow(label = "Req. Run Rate", value = "$it")
                }
                match.cricketDetails.partnership?.let {
                    InfoRow(label = "Partnership", value = it)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = SportsTextSecondary, fontSize = 13.sp)
        Text(
            text = value,
            color = SportsTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EventTimelineRow(event: MatchEventDto) {
    val icon = when (event.type.lowercase()) {
        "goal" -> "⚽"
        "yellow_card", "yellowcard" -> "🟨"
        "red_card", "redcard" -> "🟥"
        "sub", "substitution" -> "🔄"
        "var" -> "🖥️"
        "penalty" -> "🎯"
        "wicket" -> "🏏"
        else -> "⏱️"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = event.minute,
            color = SportsNeonGreen,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(44.dp)
        )
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = event.player,
                color = SportsTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (!event.description.isNullOrEmpty()) {
                Text(
                    text = event.description,
                    color = SportsTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun StatisticsTabContent(stats: MatchStatsDto) {
    val hasStats = stats.homeShots > 0 || stats.awayShots > 0 ||
            stats.homeCorners > 0 || stats.awayCorners > 0 ||
            stats.homeFouls > 0 || stats.awayFouls > 0 ||
            stats.homeYellowCards > 0 || stats.awayYellowCards > 0 ||
            stats.homeRedCards > 0 || stats.awayRedCards > 0 ||
            stats.homeOffsides > 0 || stats.awayOffsides > 0 ||
            (stats.homePossession != 50 && stats.homePossession != 0)

    if (!hasStats) {
        EmptyTabNotice("No match statistics available yet.")
        return
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SportsCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatBar(title = "Ball Possession", homeValue = stats.homePossession, awayValue = stats.awayPossession, isPercentage = true)
            if (stats.homeShots > 0 || stats.awayShots > 0) {
                StatBar(title = "Total Shots", homeValue = stats.homeShots, awayValue = stats.awayShots)
            }
            if (stats.homeShotsOnTarget > 0 || stats.awayShotsOnTarget > 0) {
                StatBar(title = "Shots on Target", homeValue = stats.homeShotsOnTarget, awayValue = stats.awayShotsOnTarget)
            }
            if (stats.homeCorners > 0 || stats.awayCorners > 0) {
                StatBar(title = "Corners", homeValue = stats.homeCorners, awayValue = stats.awayCorners)
            }
            if (stats.homeFouls > 0 || stats.awayFouls > 0) {
                StatBar(title = "Fouls", homeValue = stats.homeFouls, awayValue = stats.awayFouls)
            }
            if (stats.homeOffsides > 0 || stats.awayOffsides > 0) {
                StatBar(title = "Offsides", homeValue = stats.homeOffsides, awayValue = stats.awayOffsides)
            }
            if (stats.homeYellowCards > 0 || stats.awayYellowCards > 0) {
                StatBar(title = "Yellow Cards", homeValue = stats.homeYellowCards, awayValue = stats.awayYellowCards)
            }
            if (stats.homeRedCards > 0 || stats.awayRedCards > 0) {
                StatBar(title = "Red Cards", homeValue = stats.homeRedCards, awayValue = stats.awayRedCards)
            }
        }
    }
}

@Composable
private fun LineupsTabContent(
    lineups: LineupsDto,
    homeName: String,
    awayName: String
) {
    val hasLineups = lineups.homeStarting.isNotEmpty() || lineups.awayStarting.isNotEmpty()
    if (!hasLineups) {
        EmptyTabNotice("Lineups have not been confirmed yet for this match.")
        return
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // HOME STARTING XI
        Text(
            text = "$homeName • Starting XI",
            color = SportsNeonGreen,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        lineups.homeStarting.forEach { player ->
            PlayerLineupRow(player = player)
        }

        // HOME SUBS
        if (lineups.homeSubs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "$homeName • Substitutes",
                color = SportsTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            lineups.homeSubs.forEach { player ->
                PlayerLineupRow(player = player)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = SportsBorder)
        Spacer(modifier = Modifier.height(14.dp))

        // AWAY STARTING XI
        Text(
            text = "$awayName • Starting XI",
            color = SportsNeonGreen,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        lineups.awayStarting.forEach { player ->
            PlayerLineupRow(player = player)
        }

        // AWAY SUBS
        if (lineups.awaySubs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "$awayName • Substitutes",
                color = SportsTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            lineups.awaySubs.forEach { player ->
                PlayerLineupRow(player = player)
            }
        }
    }
}

@Composable
private fun PlayerLineupRow(player: PlayerDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(SportsCardSecondary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${player.number ?: "-"}",
                color = SportsNeonGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = player.name,
            color = SportsTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = player.position,
            color = SportsTextSecondary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun StandingsTabContent(standings: List<StandingRowDto>) {
    if (standings.isEmpty()) {
        EmptyTabNotice("No league standings available for this match.")
        return
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SportsCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "#", color = SportsTextSecondary, fontSize = 11.sp, modifier = Modifier.width(20.dp))
                Text(text = "Club", color = SportsTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text(text = "P", color = SportsTextSecondary, fontSize = 11.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                Text(text = "W", color = SportsTextSecondary, fontSize = 11.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                Text(text = "D", color = SportsTextSecondary, fontSize = 11.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                Text(text = "L", color = SportsTextSecondary, fontSize = 11.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                Text(text = "Pts", color = SportsNeonGreen, fontSize = 11.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SportsBorder)

            standings.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "${row.position}", color = SportsTextSecondary, fontSize = 12.sp, modifier = Modifier.width(20.dp), fontWeight = FontWeight.Bold)
                    Text(text = row.team.name, color = SportsTextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "${row.played}", color = SportsTextSecondary, fontSize = 12.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                    Text(text = "${row.won}", color = SportsTextSecondary, fontSize = 12.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                    Text(text = "${row.drawn}", color = SportsTextSecondary, fontSize = 12.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                    Text(text = "${row.lost}", color = SportsTextSecondary, fontSize = 12.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                    Text(text = "${row.points}", color = SportsNeonGreen, fontSize = 13.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun EmptyTabNotice(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = SportsTextSecondary, fontSize = 13.sp)
    }
}
