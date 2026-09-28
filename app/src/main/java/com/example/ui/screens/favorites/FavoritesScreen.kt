package com.example.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FavoriteEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.SportsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: SportsViewModel,
    onMatchClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favorites.collectAsState()
    var selectedTypeIndex by remember { mutableIntStateOf(0) }
    val types = listOf("All", "match", "team", "league")
    val typeLabels = listOf("All", "Matches", "Teams", "Leagues")

    val filteredList = remember(favorites, selectedTypeIndex) {
        val selectedType = types[selectedTypeIndex]
        if (selectedType == "All") favorites else favorites.filter { it.type.equals(selectedType, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorites",
                            tint = SportsLiveRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MY FAVORITES",
                            color = SportsTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
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
            // Type filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                typeLabels.forEachIndexed { index, label ->
                    val isSelected = selectedTypeIndex == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) SportsNeonGreen else SportsCard)
                            .clickable { selectedTypeIndex = index }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("filter_chip_$label")
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) SportsBackground else SportsTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = SportsCardSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No saved favorites in this category",
                            color = SportsTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        FavoriteItemRow(
                            item = item,
                            onClick = {
                                if (item.type.equals("match", ignoreCase = true)) {
                                    onMatchClick(item.id)
                                }
                            },
                            onRemoveClick = {
                                viewModel.removeFavorite(item.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteItemRow(
    item: FavoriteEntity,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("favorite_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SportsCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                listOf(SportsCardSecondary, SportsBorder)
            ),
            width = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = SportsTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!item.subtitle.isNullOrEmpty()) {
                    Text(
                        text = "${item.subtitle} • ${item.type.uppercase()}",
                        color = SportsTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SportsCardSecondary)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.sport ?: "SPORTS",
                    color = SportsNeonGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("remove_favorite_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove favorite",
                    tint = SportsLiveRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
