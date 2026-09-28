package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TeamDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "short_name") val shortName: String? = null,
    @Json(name = "logo_url") val logoUrl: String? = null,
    @Json(name = "country") val country: String? = null
)

@JsonClass(generateAdapter = true)
data class LeagueDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "sport") val sport: String = "Football",
    @Json(name = "country") val country: String? = null,
    @Json(name = "logo_url") val logoUrl: String? = null,
    @Json(name = "current_round") val currentRound: String? = null
)

@JsonClass(generateAdapter = true)
data class ScoreDto(
    @Json(name = "home") val home: Int = 0,
    @Json(name = "away") val away: Int = 0,
    @Json(name = "period") val period: String? = null,
    @Json(name = "details") val details: String? = null
)

@JsonClass(generateAdapter = true)
data class CricketDetailsDto(
    @Json(name = "runs") val runs: Int = 0,
    @Json(name = "wickets") val wickets: Int = 0,
    @Json(name = "overs") val overs: Double = 0.0,
    @Json(name = "current_batsman") val currentBatsman: String = "",
    @Json(name = "current_bowler") val currentBowler: String = "",
    @Json(name = "run_rate") val runRate: Double = 0.0,
    @Json(name = "req_run_rate") val reqRunRate: Double? = null,
    @Json(name = "partnership") val partnership: String? = null,
    @Json(name = "target") val target: Int? = null,
    @Json(name = "balls") val balls: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MatchDto(
    @Json(name = "id") val id: String,
    @Json(name = "sport") val sport: String = "football",
    @Json(name = "league") val league: LeagueDto = LeagueDto(),
    @Json(name = "home_team") val homeTeam: TeamDto = TeamDto(),
    @Json(name = "away_team") val awayTeam: TeamDto = TeamDto(),
    @Json(name = "status") val status: String = "LIVE", // LIVE, UPCOMING, FINISHED
    @Json(name = "minute") val minute: String? = null,
    @Json(name = "score") val score: ScoreDto = ScoreDto(),
    @Json(name = "start_time") val startTime: String = "",
    @Json(name = "venue") val venue: String? = null,
    @Json(name = "referee") val referee: String? = null,
    @Json(name = "cricket_details") val cricketDetails: CricketDetailsDto? = null
)

@JsonClass(generateAdapter = true)
data class MatchEventDto(
    @Json(name = "minute") val minute: String = "",
    @Json(name = "type") val type: String = "goal", // goal, yellow_card, red_card, sub, var, penalty, wicket, six, four
    @Json(name = "player") val player: String = "",
    @Json(name = "team_id") val teamId: String = "",
    @Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class MatchStatsDto(
    @Json(name = "home_possession") val homePossession: Int = 50,
    @Json(name = "away_possession") val awayPossession: Int = 50,
    @Json(name = "home_shots") val homeShots: Int = 0,
    @Json(name = "away_shots") val awayShots: Int = 0,
    @Json(name = "home_shots_on_target") val homeShotsOnTarget: Int = 0,
    @Json(name = "away_shots_on_target") val awayShotsOnTarget: Int = 0,
    @Json(name = "home_corners") val homeCorners: Int = 0,
    @Json(name = "away_corners") val awayCorners: Int = 0,
    @Json(name = "home_fouls") val homeFouls: Int = 0,
    @Json(name = "away_fouls") val awayFouls: Int = 0,
    @Json(name = "home_yellow_cards") val homeYellowCards: Int = 0,
    @Json(name = "away_yellow_cards") val awayYellowCards: Int = 0,
    @Json(name = "home_red_cards") val homeRedCards: Int = 0,
    @Json(name = "away_red_cards") val awayRedCards: Int = 0,
    @Json(name = "home_offsides") val homeOffsides: Int = 0,
    @Json(name = "away_offsides") val awayOffsides: Int = 0
)

@JsonClass(generateAdapter = true)
data class PlayerDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "number") val number: Int? = null,
    @Json(name = "position") val position: String = "-",
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "nationality") val nationality: String? = null,
    @Json(name = "age") val age: Int? = null,
    @Json(name = "team_name") val teamName: String? = null,
    @Json(name = "stats") val stats: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class LineupsDto(
    @Json(name = "home_starting") val homeStarting: List<PlayerDto> = emptyList(),
    @Json(name = "home_subs") val homeSubs: List<PlayerDto> = emptyList(),
    @Json(name = "away_starting") val awayStarting: List<PlayerDto> = emptyList(),
    @Json(name = "away_subs") val awaySubs: List<PlayerDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class StandingRowDto(
    @Json(name = "position") val position: Int = 0,
    @Json(name = "team") val team: TeamDto = TeamDto(id = "0", name = "Team"),
    @Json(name = "played") val played: Int = 0,
    @Json(name = "won") val won: Int = 0,
    @Json(name = "drawn") val drawn: Int = 0,
    @Json(name = "lost") val lost: Int = 0,
    @Json(name = "goals_for") val goalsFor: Int = 0,
    @Json(name = "goals_against") val goalsAgainst: Int = 0,
    @Json(name = "points") val points: Int = 0
)

@JsonClass(generateAdapter = true)
data class MatchDetailsResponseDto(
    @Json(name = "match") val match: MatchDto,
    @Json(name = "events") val events: List<MatchEventDto> = emptyList(),
    @Json(name = "stats") val stats: MatchStatsDto = MatchStatsDto(),
    @Json(name = "lineups") val lineups: LineupsDto = LineupsDto(),
    @Json(name = "standings") val standings: List<StandingRowDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NewsItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "summary") val summary: String,
    @Json(name = "source") val source: String,
    @Json(name = "published_at") val publishedAt: String,
    @Json(name = "sport") val sport: String,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "badge") val badge: String? = null
)

@JsonClass(generateAdapter = true)
data class SearchResultDto(
    @Json(name = "teams") val teams: List<TeamDto> = emptyList(),
    @Json(name = "players") val players: List<PlayerDto> = emptyList(),
    @Json(name = "leagues") val leagues: List<LeagueDto> = emptyList(),
    @Json(name = "matches") val matches: List<MatchDto> = emptyList()
)
