package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val type: String, // "team", "player", "league", "match"
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val sport: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
