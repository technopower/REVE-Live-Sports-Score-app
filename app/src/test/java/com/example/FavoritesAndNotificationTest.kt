package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteDao
import com.example.data.local.FavoriteEntity
import com.example.data.repository.SportsRepository
import com.example.domain.notification.NotificationPreferences
import com.example.ui.viewmodel.SportsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FavoritesAndNotificationTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var dao: FavoriteDao

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.favoriteDao()
        NotificationPreferences.init(context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    // --- 1. FAVORITES & ROOM PERSISTENCE TESTS ---

    @Test
    fun testInsertAndRetrieveFavoriteMatch() = runBlocking {
        val matchFav = FavoriteEntity(
            id = "sm_19722821",
            type = "match",
            title = "Aberdeen vs Hearts",
            subtitle = "Premiership",
            sport = "Football"
        )
        dao.insertFavorite(matchFav)

        val favorites = dao.getAllFavorites().first()
        assertEquals(1, favorites.size)
        assertEquals("sm_19722821", favorites[0].id)
        assertEquals("match", favorites[0].type)
        assertEquals("Aberdeen vs Hearts", favorites[0].title)
    }

    @Test
    fun testInsertMultipleFavoriteTypes() = runBlocking {
        val matchFav = FavoriteEntity(
            id = "match_001",
            type = "match",
            title = "Arsenal vs Chelsea",
            sport = "Football"
        )
        val teamFav = FavoriteEntity(
            id = "team_001",
            type = "team",
            title = "Arsenal FC",
            subtitle = "England",
            sport = "Football"
        )
        val leagueFav = FavoriteEntity(
            id = "league_001",
            type = "league",
            title = "Premier League",
            sport = "Football"
        )

        dao.insertFavorite(matchFav)
        dao.insertFavorite(teamFav)
        dao.insertFavorite(leagueFav)

        val allFavs = dao.getAllFavorites().first()
        assertEquals(3, allFavs.size)

        val teamFavs = dao.getFavoritesByType("team").first()
        assertEquals(1, teamFavs.size)
        assertEquals("Arsenal FC", teamFavs[0].title)

        val leagueFavs = dao.getFavoritesByType("league").first()
        assertEquals(1, leagueFavs.size)
        assertEquals("Premier League", leagueFavs[0].title)

        val matchFavs = dao.getFavoritesByType("match").first()
        assertEquals(1, matchFavs.size)
        assertEquals("Arsenal vs Chelsea", matchFavs[0].title)
    }

    @Test
    fun testFavoriteIconStateAndDeletion() = runBlocking {
        val teamId = "sm_tm_273"
        val teamFav = FavoriteEntity(
            id = teamId,
            type = "team",
            title = "Aberdeen",
            sport = "Football"
        )

        assertFalse(dao.isFavoriteDirect(teamId))
        assertFalse(dao.isFavorite(teamId).first())

        dao.insertFavorite(teamFav)
        assertTrue(dao.isFavoriteDirect(teamId))
        assertTrue(dao.isFavorite(teamId).first())

        // Delete
        dao.deleteFavoriteById(teamId)
        assertFalse(dao.isFavoriteDirect(teamId))
        assertFalse(dao.isFavorite(teamId).first())
    }

    @Test
    fun testRepositoryToggleFavorite() = runBlocking {
        val repo = SportsRepository(dao)

        val testId = "test_item_123"
        assertFalse(dao.isFavoriteDirect(testId))

        // 1st toggle: adds favorite
        repo.toggleFavorite(id = testId, type = "team", title = "Test Team", sport = "Football")
        assertTrue(dao.isFavoriteDirect(testId))

        // 2nd toggle: removes favorite
        repo.toggleFavorite(id = testId, type = "team", title = "Test Team", sport = "Football")
        assertFalse(dao.isFavoriteDirect(testId))
    }

    // --- 2. NOTIFICATION PREFERENCES TESTS ---

    @Test
    fun testNotificationPreferencesDefaults() {
        val s = NotificationPreferences.settings.value
        assertTrue(s.goalAlerts)
        assertTrue(s.matchStartAlerts)
        assertTrue(s.matchResultAlerts)
        assertTrue(s.redCardAlerts)
        assertTrue(s.favoriteTeamAlerts)
        assertTrue(s.halfTimeAlerts)
    }

    @Test
    fun testNotificationPreferencesToggles() {
        // Toggle goal alerts off
        NotificationPreferences.toggleGoalAlerts(false)
        assertFalse(NotificationPreferences.settings.value.goalAlerts)
        assertFalse(NotificationPreferences.shouldShowAlert("goal"))

        // Other alerts still enabled
        assertTrue(NotificationPreferences.shouldShowAlert("red_card"))
        assertTrue(NotificationPreferences.shouldShowAlert("match_starting"))
        assertTrue(NotificationPreferences.shouldShowAlert("full_time"))
        assertTrue(NotificationPreferences.shouldShowAlert("favorite_team"))
        assertTrue(NotificationPreferences.shouldShowAlert("half_time"))

        // Toggle red card alerts off
        NotificationPreferences.toggleRedCardAlerts(false)
        assertFalse(NotificationPreferences.settings.value.redCardAlerts)
        assertFalse(NotificationPreferences.shouldShowAlert("red_card"))

        // Reset back to true
        NotificationPreferences.toggleGoalAlerts(true)
        NotificationPreferences.toggleRedCardAlerts(true)
        assertTrue(NotificationPreferences.shouldShowAlert("goal"))
        assertTrue(NotificationPreferences.shouldShowAlert("red_card"))
    }

    @Test
    fun testNotificationAlertTypesSupported() {
        // Supported types: Goal, Red card, Match starting, Half-time, Full-time, Favorite team
        assertTrue(NotificationPreferences.shouldShowAlert("goal"))
        assertTrue(NotificationPreferences.shouldShowAlert("red_card"))
        assertTrue(NotificationPreferences.shouldShowAlert("match_starting"))
        assertTrue(NotificationPreferences.shouldShowAlert("half_time"))
        assertTrue(NotificationPreferences.shouldShowAlert("full_time"))
        assertTrue(NotificationPreferences.shouldShowAlert("favorite_team"))
    }

    // --- 3. LIVE AUTO REFRESH LIFECYCLE TESTS ---

    @Test
    fun testLiveAutoRefreshForegroundBackgroundTransition() {
        val repo = SportsRepository(dao)
        val viewModel = SportsViewModel(repo)

        // App backgrounded: live polling stops
        viewModel.setAppForegroundState(false)
        // App returns to foreground: live polling resumes
        viewModel.setAppForegroundState(true)

        // Clean up
        viewModel.stopLivePolling()
    }
}
