package com.example.data.network

import com.example.BuildConfig
import com.example.data.local.FavoriteEntity
import com.example.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface SportsApiService {
    @GET("api/v1/live")
    suspend fun getLiveMatches(
        @Query("sport") sport: String? = null
    ): List<MatchDto>

    @GET("api/fixtures/today")
    suspend fun getTodayMatches(
        @Query("sport") sport: String? = null
    ): List<MatchDto>

    @GET("api/fixtures/upcoming")
    suspend fun getUpcomingMatches(
        @Query("sport") sport: String? = null
    ): List<MatchDto>

    @GET("api/matches/{id}")
    suspend fun getMatchDetails(
        @Path("id") matchId: String
    ): MatchDetailsResponseDto

    @GET("api/v1/leagues")
    suspend fun getLeagues(
        @Query("sport") sport: String? = null
    ): List<LeagueDto>

    @GET("api/leagues/{id}/standings")
    suspend fun getLeagueStandings(
        @Path("id") leagueId: String
    ): List<StandingRowDto>

    @GET("api/teams/{id}")
    suspend fun getTeam(
        @Path("id") teamId: String
    ): TeamDto

    @GET("api/players/{id}")
    suspend fun getPlayer(
        @Path("id") playerId: String
    ): PlayerDto

    @GET("api/v1/news")
    suspend fun getNews(
        @Query("sport") sport: String? = null
    ): List<NewsItemDto>

    @GET("api/v1/search")
    suspend fun search(
        @Query("q") query: String
    ): SearchResultDto

    @POST("api/v1/notifications/register-token")
    suspend fun registerDeviceToken(
        @Body body: Map<String, String>
    ): Map<String, Any>

    @GET("api/v1/favorites")
    suspend fun getFavorites(): List<FavoriteEntity>

    @POST("api/v1/favorites")
    suspend fun addFavorite(
        @Body favorite: FavoriteEntity
    ): FavoriteEntity

    @DELETE("api/v1/favorites/{id}")
    suspend fun deleteFavorite(
        @Path("id") id: String
    ): Map<String, Any>
}

object NetworkClient {
    var baseUrl: String = BuildConfig.BACKEND_BASE_URL
        private set

    private var cachedService: SportsApiService? = null

    val api: SportsApiService
        get() = getService()

    fun updateBaseUrl(newUrl: String) {
        val formatted = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        baseUrl = formatted
        cachedService = null
    }

    fun getService(): SportsApiService {
        cachedService?.let { return it }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val service = retrofit.create(SportsApiService::class.java)
        cachedService = service
        return service
    }
}
