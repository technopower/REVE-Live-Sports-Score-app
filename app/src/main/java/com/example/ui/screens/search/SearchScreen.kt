package com.example.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MatchCard
import com.example.ui.components.TeamLogo
import com.example.ui.theme.*
import com.example.ui.viewmodel.SportsViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SportsViewModel,
    onMatchClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsState()
    val searchResultState by viewModel.searchResult.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val favoriteIds = favorites.map { it.id }.toSet()

    val popularTags = listOf("Arsenal", "Chelsea", "Premier League", "Dhoni", "Lakers", "Alcaraz", "Champions League")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search teams, leagues, matches...", color = SportsTextSecondary, fontSize = 14.sp) },
                        singleLine = true,
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SportsTextSecondary)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SportsCard,
                            unfocusedContainerColor = SportsCard,
                            focusedBorderColor = SportsNeonGreen,
                            unfocusedBorderColor = SportsBorder,
                            focusedTextColor = SportsTextPrimary,
                            unfocusedTextColor = SportsTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                            .testTag("global_search_input")
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("search_back")) {
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
        ) {
            // Popular searches suggestions
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "POPULAR SEARCHES",
                    color = SportsTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(popularTags) { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(SportsCard)
                                .clickable { viewModel.onSearchQueryChanged(tag) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = tag, color = SportsNeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            HorizontalDivider(color = SportsBorder, modifier = Modifier.padding(vertical = 4.dp))

            when (val state = searchResultState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SportsNeonGreen)
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = SportsTextSecondary)
                    }
                }
                is UiState.Success -> {
                    val res = state.data
                    if (query.isNotBlank() && res.teams.isEmpty() && res.leagues.isEmpty() && res.matches.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No results found for \"$query\"", color = SportsTextSecondary)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (res.teams.isNotEmpty()) {
                                item {
                                    Text("TEAMS", color = SportsNeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                items(res.teams) { team ->
                                    val isTeamFav = favoriteIds.contains(team.id)
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = SportsCard),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TeamLogo(team = team, size = 32)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(team.name, color = SportsTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                                if (!team.country.isNullOrEmpty()) {
                                                    Text(team.country, color = SportsTextSecondary, fontSize = 11.sp)
                                                }
                                            }
                                            IconButton(
                                                onClick = { viewModel.toggleFavoriteTeam(team) },
                                                modifier = Modifier.testTag("search_fav_team_${team.id}")
                                            ) {
                                                Icon(
                                                    imageVector = if (isTeamFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                    contentDescription = "Favorite Team",
                                                    tint = if (isTeamFav) SportsLiveRed else SportsTextSecondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (res.leagues.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("LEAGUES", color = SportsNeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                items(res.leagues) { league ->
                                    val isLeagueFav = favoriteIds.contains(league.id)
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = SportsCard),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(league.name, color = SportsTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                                Text(league.sport, color = SportsNeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                            }
                                            IconButton(
                                                onClick = { viewModel.toggleFavoriteLeague(league) },
                                                modifier = Modifier.testTag("search_fav_league_${league.id}")
                                            ) {
                                                Icon(
                                                    imageVector = if (isLeagueFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                    contentDescription = "Favorite League",
                                                    tint = if (isLeagueFav) SportsLiveRed else SportsTextSecondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (res.matches.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("MATCHES", color = SportsNeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                items(res.matches) { match ->
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
                }
            }
        }
    }
}
