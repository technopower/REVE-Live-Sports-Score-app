package com.example.ui.screens.leagues

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeagueDto
import com.example.ui.components.SportSelector
import com.example.ui.theme.*
import com.example.ui.viewmodel.SportsViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaguesScreen(
    viewModel: SportsViewModel,
    onLeagueClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedSport by viewModel.selectedSport.collectAsState()
    val leaguesState by viewModel.leagues.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val favoriteIds = favorites.map { it.id }.toSet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "LEAGUES & CUPS",
                        color = SportsTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
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
            SportSelector(
                selectedSport = selectedSport,
                onSportSelected = { viewModel.setSport(it) }
            )

            when (val state = leaguesState) {
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
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.data) { league ->
                            LeagueCardItem(
                                league = league,
                                isFavorite = favoriteIds.contains(league.id),
                                onFavoriteClick = { viewModel.toggleFavoriteLeague(league) },
                                onClick = { onLeagueClick(league.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeagueCardItem(
    league: LeagueDto,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SportsCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(SportsCardSecondary, SportsBorder)),
            width = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SportsCardSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = league.name,
                        tint = SportsNeonGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = league.name,
                        color = SportsTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${league.country ?: "International"} • ${league.currentRound ?: league.sport}",
                        color = SportsTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) SportsLiveRed else SportsTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
