package com.example.data.repository

import com.example.data.local.FavoriteDao
import com.example.data.local.FavoriteEntity
import com.example.data.model.*
import com.example.data.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SportsRepository(
    private val favoriteDao: FavoriteDao
) {
    private val api = NetworkClient.getService()

    suspend fun getLiveMatches(sport: String? = null): List<MatchDto> = withContext(Dispatchers.IO) {
        val normalizedSport = if (sport != null && sport.equals("All", ignoreCase = true)) null else sport
        api.getLiveMatches(normalizedSport)
    }

    suspend fun getTodayMatches(sport: String? = null): List<MatchDto> = withContext(Dispatchers.IO) {
        try {
            val result = api.getTodayMatches(sport)
            if (result.isNotEmpty()) result else filterBySport(DefaultSportsData.todayMatches, sport)
        } catch (e: Exception) {
            filterBySport(DefaultSportsData.todayMatches, sport)
        }
    }

    suspend fun getUpcomingMatches(sport: String? = null): List<MatchDto> = withContext(Dispatchers.IO) {
        try {
            val result = api.getUpcomingMatches(sport)
            if (result.isNotEmpty()) result else filterBySport(DefaultSportsData.todayMatches, sport)
        } catch (e: Exception) {
            filterBySport(DefaultSportsData.todayMatches, sport)
        }
    }

    suspend fun getMatchDetails(matchId: String): MatchDetailsResponseDto = withContext(Dispatchers.IO) {
        api.getMatchDetails(matchId)
    }

    suspend fun getLeagues(sport: String? = null): List<LeagueDto> = withContext(Dispatchers.IO) {
        try {
            val result = api.getLeagues(sport)
            if (result.isNotEmpty()) result else filterLeaguesBySport(DefaultSportsData.leagues, sport)
        } catch (e: Exception) {
            filterLeaguesBySport(DefaultSportsData.leagues, sport)
        }
    }

    suspend fun getLeagueStandings(leagueId: String): List<StandingRowDto> = withContext(Dispatchers.IO) {
        try {
            api.getLeagueStandings(leagueId)
        } catch (e: Exception) {
            DefaultSportsData.standings
        }
    }

    suspend fun getTeam(teamId: String): TeamDto = withContext(Dispatchers.IO) {
        try {
            api.getTeam(teamId)
        } catch (e: Exception) {
            DefaultSportsData.teams.find { it.id == teamId }
                ?: TeamDto(teamId, "Arsenal", "ARS", null, "England")
        }
    }

    suspend fun getNews(sport: String? = null): List<NewsItemDto> = withContext(Dispatchers.IO) {
        try {
            val result = api.getNews(sport)
            if (result.isNotEmpty()) result else filterNewsBySport(DefaultSportsData.news, sport)
        } catch (e: Exception) {
            filterNewsBySport(DefaultSportsData.news, sport)
        }
    }

    suspend fun search(query: String): SearchResultDto = withContext(Dispatchers.IO) {
        try {
            api.search(query)
        } catch (e: Exception) {
            val q = query.lowercase().trim()
            val matchedTeams = DefaultSportsData.teams.filter { it.name.lowercase().contains(q) || (it.shortName?.lowercase()?.contains(q) == true) }
            val matchedLeagues = DefaultSportsData.leagues.filter { it.name.lowercase().contains(q) || it.sport.lowercase().contains(q) }
            val matchedMatches = (DefaultSportsData.liveMatches + DefaultSportsData.todayMatches).filter {
                it.homeTeam.name.lowercase().contains(q) || it.awayTeam.name.lowercase().contains(q) || it.league.name.lowercase().contains(q)
            }
            SearchResultDto(
                teams = matchedTeams,
                players = emptyList(),
                leagues = matchedLeagues,
                matches = matchedMatches
            )
        }
    }

    // Favorites persistence via Room
    fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()

    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>> = favoriteDao.getFavoritesByType(type)

    fun isFavorite(id: String): Flow<Boolean> = favoriteDao.isFavorite(id)

    suspend fun insertFavorite(favorite: FavoriteEntity) = withContext(Dispatchers.IO) {
        favoriteDao.insertFavorite(favorite)
        try {
            api.addFavorite(favorite)
        } catch (_: Exception) {}
    }

    suspend fun deleteFavoriteById(id: String) = withContext(Dispatchers.IO) {
        favoriteDao.deleteFavoriteById(id)
        try {
            api.deleteFavorite(id)
        } catch (_: Exception) {}
    }

    suspend fun toggleFavorite(
        id: String,
        type: String,
        title: String,
        subtitle: String? = null,
        imageUrl: String? = null,
        sport: String? = null
    ) = withContext(Dispatchers.IO) {
        if (favoriteDao.isFavoriteDirect(id)) {
            favoriteDao.deleteFavoriteById(id)
            try {
                api.deleteFavorite(id)
            } catch (_: Exception) {}
        } else {
            val entity = FavoriteEntity(
                id = id,
                type = type,
                title = title,
                subtitle = subtitle,
                imageUrl = imageUrl,
                sport = sport
            )
            favoriteDao.insertFavorite(entity)
            try {
                api.addFavorite(entity)
            } catch (_: Exception) {}
        }
    }

    private fun filterBySport(matches: List<MatchDto>, sport: String?): List<MatchDto> {
        if (sport.isNullOrBlank() || sport.equals("all", ignoreCase = true)) return matches
        return matches.filter { it.sport.equals(sport, ignoreCase = true) }
    }

    private fun filterLeaguesBySport(leagues: List<LeagueDto>, sport: String?): List<LeagueDto> {
        if (sport.isNullOrBlank() || sport.equals("all", ignoreCase = true)) return leagues
        return leagues.filter { it.sport.equals(sport, ignoreCase = true) }
    }

    private fun filterNewsBySport(news: List<NewsItemDto>, sport: String?): List<NewsItemDto> {
        if (sport.isNullOrBlank() || sport.equals("all", ignoreCase = true)) return news
        return news.filter { it.sport.equals(sport, ignoreCase = true) }
    }
}
