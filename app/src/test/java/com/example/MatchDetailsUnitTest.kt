package com.example

import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test

class MatchDetailsUnitTest {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `test match details JSON deserialization with real structure`() {
        val json = """
        {
            "match": {
                "id": "sm_19722821",
                "sport": "football",
                "league": {
                    "id": "sm_lg_501",
                    "name": "Premiership",
                    "sport": "Football",
                    "logo_url": "https://cdn.sportmonks.com/images/soccer/leagues/501.png"
                },
                "home_team": {
                    "id": "sm_tm_273",
                    "name": "Aberdeen",
                    "short_name": "ABE",
                    "logo_url": "https://cdn.sportmonks.com/images/soccer/teams/17/273.png",
                    "score": 2
                },
                "away_team": {
                    "id": "sm_tm_314",
                    "name": "Hearts",
                    "short_name": "HEA",
                    "logo_url": "https://cdn.sportmonks.com/images/soccer/teams/26/314.png",
                    "score": 1
                },
                "status": "Full Time",
                "minute": "90'",
                "kickoff_time": "2026-08-01 16:30:00",
                "start_time": "2026-08-01 16:30:00",
                "venue": "Pittodrie Stadium",
                "is_live": false,
                "score": {
                    "home": 2,
                    "away": 1
                }
            },
            "events": [
                {
                    "id": "157371697",
                    "minute": "35'",
                    "type": "goal",
                    "team_id": "sm_tm_314",
                    "player": "Oisin McEntee",
                    "description": "1st Goal"
                },
                {
                    "id": "157371698",
                    "minute": "60'",
                    "type": "sub",
                    "team_id": "sm_tm_314",
                    "player": "Pierre Landry Kaboré",
                    "description": "In: Pierre Landry Kaboré, Out: Amadou Ba-Sy"
                },
                {
                    "id": "157371699",
                    "minute": "34'",
                    "type": "yellow_card",
                    "team_id": "sm_tm_273",
                    "player": "Brad Lyons"
                }
            ],
            "stats": {
                "home_possession": 51,
                "away_possession": 49,
                "home_shots": 14,
                "away_shots": 9,
                "home_shots_on_target": 6,
                "away_shots_on_target": 4,
                "home_corners": 4,
                "away_corners": 6,
                "home_fouls": 11,
                "away_fouls": 13,
                "home_yellow_cards": 3,
                "away_yellow_cards": 2,
                "home_red_cards": 0,
                "away_red_cards": 1,
                "home_offsides": 2,
                "away_offsides": 1
            },
            "lineups": {
                "home_starting": [
                    {
                        "id": "37614769",
                        "name": "Tony Yogane",
                        "number": 7,
                        "position": "MF"
                    }
                ],
                "home_subs": [
                    {
                        "id": "6007",
                        "name": "Dimitar Mitov",
                        "number": 1,
                        "position": "GK"
                    }
                ],
                "away_starting": [
                    {
                        "id": "27440420",
                        "name": "Oisin McEntee",
                        "number": 6,
                        "position": "DF"
                    }
                ],
                "away_subs": [
                    {
                        "id": "30446926",
                        "name": "Pierre Landry Kabore",
                        "number": 11,
                        "position": "FW"
                    }
                ]
            },
            "standings": [
                {
                    "position": 1,
                    "team": {
                        "id": "sm_tm_53",
                        "name": "Celtic",
                        "logo_url": "https://cdn.sportmonks.com/images/soccer/teams/21/53.png"
                    },
                    "played": 7,
                    "won": 6,
                    "drawn": 0,
                    "lost": 1,
                    "goals_for": 14,
                    "goals_against": 4,
                    "points": 18
                }
            ]
        }
        """.trimIndent()

        val adapter = moshi.adapter(MatchDetailsResponseDto::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        result!!

        // Match Overview verification
        assertEquals("sm_19722821", result.match.id)
        assertEquals("Aberdeen", result.match.homeTeam.name)
        assertEquals("Hearts", result.match.awayTeam.name)
        assertEquals(2, result.match.score.home)
        assertEquals(1, result.match.score.away)
        assertEquals("Full Time", result.match.status)
        assertEquals("Pittodrie Stadium", result.match.venue)
        assertEquals("Premiership", result.match.league.name)

        // Events verification
        assertEquals(3, result.events.size)
        val goalEvent = result.events[0]
        assertEquals("35'", goalEvent.minute)
        assertEquals("goal", goalEvent.type)
        assertEquals("Oisin McEntee", goalEvent.player)
        assertEquals("1st Goal", goalEvent.description)

        val subEvent = result.events[1]
        assertEquals("sub", subEvent.type)
        assertEquals("Pierre Landry Kaboré", subEvent.player)

        // Statistics verification
        assertEquals(51, result.stats.homePossession)
        assertEquals(49, result.stats.awayPossession)
        assertEquals(14, result.stats.homeShots)
        assertEquals(9, result.stats.awayShots)
        assertEquals(6, result.stats.homeShotsOnTarget)
        assertEquals(4, result.stats.awayShotsOnTarget)
        assertEquals(4, result.stats.homeCorners)
        assertEquals(6, result.stats.awayCorners)
        assertEquals(11, result.stats.homeFouls)
        assertEquals(13, result.stats.awayFouls)
        assertEquals(2, result.stats.homeOffsides)
        assertEquals(1, result.stats.awayOffsides)

        // Lineups verification
        assertEquals(1, result.lineups.homeStarting.size)
        assertEquals("Tony Yogane", result.lineups.homeStarting[0].name)
        assertEquals(7, result.lineups.homeStarting[0].number)
        assertEquals("MF", result.lineups.homeStarting[0].position)

        assertEquals(1, result.lineups.homeSubs.size)
        assertEquals("Dimitar Mitov", result.lineups.homeSubs[0].name)
        assertEquals("GK", result.lineups.homeSubs[0].position)

        assertEquals(1, result.lineups.awayStarting.size)
        assertEquals("Oisin McEntee", result.lineups.awayStarting[0].name)

        assertEquals(1, result.lineups.awaySubs.size)
        assertEquals("Pierre Landry Kabore", result.lineups.awaySubs[0].name)

        // Standings verification
        assertEquals(1, result.standings.size)
        val standing = result.standings[0]
        assertEquals(1, standing.position)
        assertEquals("Celtic", standing.team.name)
        assertEquals(7, standing.played)
        assertEquals(6, standing.won)
        assertEquals(0, standing.drawn)
        assertEquals(1, standing.lost)
        assertEquals(18, standing.points)
    }

    @Test
    fun `test match stats default values when empty`() {
        val stats = MatchStatsDto()
        assertEquals(50, stats.homePossession)
        assertEquals(50, stats.awayPossession)
        assertEquals(0, stats.homeShots)
        assertEquals(0, stats.awayShots)
        assertEquals(0, stats.homeCorners)
        assertEquals(0, stats.awayCorners)
        assertEquals(0, stats.homeOffsides)
        assertEquals(0, stats.awayOffsides)
    }

    @Test
    fun `test lineups empty defaults`() {
        val lineups = LineupsDto()
        assertTrue(lineups.homeStarting.isEmpty())
        assertTrue(lineups.homeSubs.isEmpty())
        assertTrue(lineups.awayStarting.isEmpty())
        assertTrue(lineups.awaySubs.isEmpty())
    }
}
