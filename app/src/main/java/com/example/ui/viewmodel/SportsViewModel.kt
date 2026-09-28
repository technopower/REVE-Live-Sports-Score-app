package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FavoriteEntity
import com.example.data.model.*
import com.example.data.repository.SportsRepository
import com.example.domain.billing.SubscriptionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class SportsViewModel(
    private val repository: SportsRepository
) : ViewModel() {

    private val _selectedSport = MutableStateFlow("All")
    val selectedSport: StateFlow<String> = _selectedSport.asStateFlow()

    private val _liveMatches = MutableStateFlow<UiState<List<MatchDto>>>(UiState.Loading)
    val liveMatches: StateFlow<UiState<List<MatchDto>>> = _liveMatches.asStateFlow()

    private val _todayMatches = MutableStateFlow<UiState<List<MatchDto>>>(UiState.Loading)
    val todayMatches: StateFlow<UiState<List<MatchDto>>> = _todayMatches.asStateFlow()

    private val _upcomingMatches = MutableStateFlow<UiState<List<MatchDto>>>(UiState.Loading)
    val upcomingMatches: StateFlow<UiState<List<MatchDto>>> = _upcomingMatches.asStateFlow()

    private val _leagues = MutableStateFlow<UiState<List<LeagueDto>>>(UiState.Loading)
    val leagues: StateFlow<UiState<List<LeagueDto>>> = _leagues.asStateFlow()

    private val _news = MutableStateFlow<UiState<List<NewsItemDto>>>(UiState.Loading)
    val news: StateFlow<UiState<List<NewsItemDto>>> = _news.asStateFlow()

    private val _searchResult = MutableStateFlow<UiState<SearchResultDto>>(UiState.Success(SearchResultDto()))
    val searchResult: StateFlow<UiState<SearchResultDto>> = _searchResult.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _matchDetails = MutableStateFlow<UiState<MatchDetailsResponseDto>>(UiState.Loading)
    val matchDetails: StateFlow<UiState<MatchDetailsResponseDto>> = _matchDetails.asStateFlow()

    // Room Favorites
    val favorites: StateFlow<List<FavoriteEntity>> = repository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var pollingJob: Job? = null
    private var searchDebounceJob: Job? = null

    init {
        loadAllData()
        startLivePolling()
    }

    fun setSport(sport: String) {
        _selectedSport.value = sport
        loadAllData(sport)
    }

    fun loadAllData(sport: String = _selectedSport.value) {
        viewModelScope.launch {
            loadLiveMatches(sport)
            loadTodayMatches(sport)
            loadUpcomingMatches(sport)
            loadLeagues(sport)
            loadNews(sport)
        }
    }

    private val _isLiveRefreshing = MutableStateFlow(false)
    val isLiveRefreshing: StateFlow<Boolean> = _isLiveRefreshing.asStateFlow()

    fun loadLiveMatches(sport: String = _selectedSport.value, isUserRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isUserRefresh) {
                _isLiveRefreshing.value = true
            } else if (_liveMatches.value !is UiState.Success) {
                _liveMatches.value = UiState.Loading
            }
            try {
                val matches = repository.getLiveMatches(sport)
                _liveMatches.value = UiState.Success(matches)
            } catch (e: Exception) {
                // If it's a silent periodic poll and we already have data, keep existing data or show error
                if (_liveMatches.value !is UiState.Success) {
                    _liveMatches.value = UiState.Error(e.message ?: "Failed to load live matches. Please check backend connection.")
                }
            } finally {
                _isLiveRefreshing.value = false
            }
        }
    }

    fun refreshLiveMatches() {
        loadLiveMatches(_selectedSport.value, isUserRefresh = true)
    }

    private fun loadTodayMatches(sport: String = _selectedSport.value) {
        viewModelScope.launch {
            try {
                val matches = repository.getTodayMatches(sport)
                _todayMatches.value = UiState.Success(matches)
            } catch (e: Exception) {
                _todayMatches.value = UiState.Error(e.message ?: "Failed to load today's matches")
            }
        }
    }

    private fun loadUpcomingMatches(sport: String = _selectedSport.value) {
        viewModelScope.launch {
            try {
                val matches = repository.getUpcomingMatches(sport)
                _upcomingMatches.value = UiState.Success(matches)
            } catch (e: Exception) {
                _upcomingMatches.value = UiState.Error(e.message ?: "Failed to load upcoming matches")
            }
        }
    }

    fun loadLeagues(sport: String = _selectedSport.value) {
        viewModelScope.launch {
            try {
                val data = repository.getLeagues(sport)
                _leagues.value = UiState.Success(data)
            } catch (e: Exception) {
                _leagues.value = UiState.Error(e.message ?: "Failed to load leagues")
            }
        }
    }

    fun loadNews(sport: String = _selectedSport.value) {
        viewModelScope.launch {
            try {
                val data = repository.getNews(sport)
                _news.value = UiState.Success(data)
            } catch (e: Exception) {
                _news.value = UiState.Error(e.message ?: "Failed to load news")
            }
        }
    }

    private val _isDetailsRefreshing = MutableStateFlow(false)
    val isDetailsRefreshing: StateFlow<Boolean> = _isDetailsRefreshing.asStateFlow()

    fun loadMatchDetails(matchId: String, isUserRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isUserRefresh) {
                _isDetailsRefreshing.value = true
            } else if (_matchDetails.value !is UiState.Success) {
                _matchDetails.value = UiState.Loading
            }
            try {
                val details = repository.getMatchDetails(matchId)
                _matchDetails.value = UiState.Success(details)
            } catch (e: Exception) {
                if (!isUserRefresh || _matchDetails.value !is UiState.Success) {
                    _matchDetails.value = UiState.Error(e.message ?: "Failed to load match details")
                }
            } finally {
                if (isUserRefresh) {
                    _isDetailsRefreshing.value = false
                }
            }
        }
    }

    fun refreshMatchDetails(matchId: String) {
        loadMatchDetails(matchId, isUserRefresh = true)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchDebounceJob?.cancel()
        if (query.isBlank()) {
            _searchResult.value = UiState.Success(SearchResultDto())
            return
        }
        searchDebounceJob = viewModelScope.launch {
            delay(350) // Debounce
            _searchResult.value = UiState.Loading
            try {
                val result = repository.search(query)
                _searchResult.value = UiState.Success(result)
            } catch (e: Exception) {
                _searchResult.value = UiState.Error(e.message ?: "Search failed")
            }
        }
    }

    fun toggleFavoriteMatch(match: MatchDto) {
        viewModelScope.launch {
            repository.toggleFavorite(
                id = match.id,
                type = "match",
                title = "${match.homeTeam.name} vs ${match.awayTeam.name}",
                subtitle = match.league.name,
                sport = match.sport
            )
        }
    }

    fun toggleFavoriteTeam(team: TeamDto, sport: String = "Football") {
        viewModelScope.launch {
            repository.toggleFavorite(
                id = team.id,
                type = "team",
                title = team.name,
                subtitle = team.country,
                imageUrl = team.logoUrl,
                sport = sport
            )
        }
    }

    fun toggleFavoriteLeague(league: LeagueDto) {
        viewModelScope.launch {
            repository.toggleFavorite(
                id = league.id,
                type = "league",
                title = league.name,
                subtitle = league.country,
                imageUrl = league.logoUrl,
                sport = league.sport
            )
        }
    }

    fun removeFavorite(id: String) {
        viewModelScope.launch {
            repository.deleteFavoriteById(id)
        }
    }

    private var isAppInForeground = true

    fun setAppForegroundState(inForeground: Boolean) {
        if (isAppInForeground == inForeground) return
        isAppInForeground = inForeground
        if (inForeground) {
            startLivePolling()
            loadLiveMatches(_selectedSport.value, isUserRefresh = false)
        } else {
            stopLivePolling()
        }
    }

    fun startLivePolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive && isAppInForeground) {
                delay(20000) // Poll every 20 seconds respecting API rate limits
                loadLiveMatches(_selectedSport.value, isUserRefresh = false)
            }
        }
    }

    fun stopLivePolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopLivePolling()
        searchDebounceJob?.cancel()
    }
}
