package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeagueDto
import com.example.data.model.MatchDto
import com.example.data.model.TeamDto
import com.example.domain.ad.AdManager
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SportsViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SportsViewModel,
    onMatchClick: (String) -> Unit,
    onLeagueClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedSport by viewModel.selectedSport.collectAsState()
    val liveMatchesState by viewModel.liveMatches.collectAsState()
    val todayMatchesState by viewModel.todayMatches.collectAsState()
    val upcomingMatchesState by viewModel.upcomingMatches.collectAsState()
    val leaguesState by viewModel.leagues.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val adsEnabled by AdManager.adsEnabled.collectAsState()

    val favoriteIds = favorites.map { it.id }.toSet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "REVE",
                            color = SportsNeonGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE SPORTS",
                            color = SportsTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.testTag("home_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = SportsTextPrimary
                        )
                    }
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("home_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = SportsTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SportsBackground
                )
            )
        },
        containerColor = SportsBackground,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Sport Category Chips
            item {
                SportSelector(
                    selectedSport = selectedSport,
                    onSportSelected = { viewModel.setSport(it) }
                )
            }

            // Banner Ad if enabled
            if (adsEnabled) {
                item {
                    BannerAdPlaceholder()
                }
            }

            // SECTION: LIVE NOW
            item {
                SectionHeader(
                    title = "LIVE NOW",
                    trailingText = "View All",
                    onTrailingClick = { /* Handled via bottom bar live tab */ }
                )
            }

            item {
                when (val state = liveMatchesState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = SportsNeonGreen, modifier = Modifier.size(28.dp))
                        }
                    }
                    is UiState.Error -> {
                        Text(
                            text = "Failed to load live scores",
                            color = SportsTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    is UiState.Success -> {
                        if (state.data.isEmpty()) {
                            Text(
                                text = "No live matches right now in $selectedSport",
                                color = SportsTextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        } else {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.data) { match ->
                                    LiveMatchCarouselCard(
                                        match = match,
                                        isFavorite = favoriteIds.contains(match.id),
                                        onFavoriteClick = { viewModel.toggleFavoriteMatch(match) },
                                        onClick = { onMatchClick(match.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION: POPULAR LEAGUES
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "POPULAR LEAGUES")
            }

            item {
                when (val state = leaguesState) {
                    is UiState.Success -> {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.data) { league ->
                                LeagueChip(
                                    league = league,
                                    onClick = { onLeagueClick(league.id) }
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }

            // SECTION: TODAY'S MATCHES
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "TODAY'S MATCHES")
            }

            when (val state = todayMatchesState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = SportsNeonGreen, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        Text(
                            text = "Could not refresh today's fixtures",
                            color = SportsTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                is UiState.Success -> {
                    items(state.data) { match ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                            MatchCard(
                                match = match,
                                isFavorite = favoriteIds.contains(match.id),
                                onFavoriteClick = { viewModel.toggleFavoriteMatch(match) },
                                onClick = { onMatchClick(match.id) }
                            )
                        }
                    }
                }
            }

            // SECTION: UPCOMING
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "UPCOMING FIXTURES")
            }

            when (val state = upcomingMatchesState) {
                is UiState.Success -> {
                    items(state.data) { match ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                            MatchCard(
                                match = match,
                                isFavorite = favoriteIds.contains(match.id),
                                onFavoriteClick = { viewModel.toggleFavoriteMatch(match) },
                                onClick = { onMatchClick(match.id) }
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    trailingText: String? = null,
    onTrailingClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = SportsTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                color = SportsNeonGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onTrailingClick() }
            )
        }
    }
}

@Composable
private fun LiveMatchCarouselCard(
    match: MatchDto,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable { onClick() }
            .testTag("live_carousel_card_${match.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SportsCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(listOf(SportsCardSecondary, SportsBorder)),
            width = 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiveBadge()
                Text(
                    text = match.minute ?: "LIVE",
                    color = SportsLiveRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Home Team Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TeamLogo(team = match.homeTeam, size = 26)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.homeTeam.name,
                        color = SportsTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${match.score.home}",
                    color = SportsNeonGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Away Team Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TeamLogo(team = match.awayTeam, size = 26)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.awayTeam.name,
                        color = SportsTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${match.score.away}",
                    color = SportsNeonGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = match.league.name,
                color = SportsTextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LeagueChip(
    league: LeagueDto,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SportsCard)
            .border(1.dp, SportsBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = league.name,
                color = SportsTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = league.sport,
                color = SportsNeonGreen,
                fontSize = 10.sp
            )
        }
    }
}
