package com.example.data.repository

import com.example.data.model.*

object DefaultSportsData {
    val leagues = listOf(
        LeagueDto(
            id = "league_pl",
            name = "Premier League",
            sport = "Football",
            country = "England",
            logoUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=120",
            currentRound = "Matchday 28"
        ),
        LeagueDto(
            id = "league_ucl",
            name = "UEFA Champions League",
            sport = "Football",
            country = "Europe",
            logoUrl = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=120",
            currentRound = "Quarter-Finals"
        ),
        LeagueDto(
            id = "league_ipl",
            name = "Indian Premier League",
            sport = "Cricket",
            country = "India",
            logoUrl = "https://images.unsplash.com/photo-1531415074868-036b107e775a?w=120",
            currentRound = "League Stage"
        ),
        LeagueDto(
            id = "league_nba",
            name = "NBA",
            sport = "Basketball",
            country = "USA",
            logoUrl = "https://images.unsplash.com/photo-1546519638-68e109498ffc?w=120",
            currentRound = "Regular Season"
        ),
        LeagueDto(
            id = "league_atp",
            name = "ATP Tour",
            sport = "Tennis",
            country = "International",
            logoUrl = "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=120",
            currentRound = "Semi-Finals"
        ),
        LeagueDto(
            id = "league_laliga",
            name = "La Liga",
            sport = "Football",
            country = "Spain",
            logoUrl = "https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=120",
            currentRound = "Matchday 29"
        ),
        LeagueDto(
            id = "league_bundesliga",
            name = "Bundesliga",
            sport = "Football",
            country = "Germany",
            logoUrl = "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=120",
            currentRound = "Matchday 27"
        ),
        LeagueDto(
            id = "league_seriea",
            name = "Serie A",
            sport = "Italy",
            country = "Italy",
            logoUrl = "https://images.unsplash.com/photo-1518091043644-c1d4457512c6?w=120",
            currentRound = "Matchday 28"
        )
    )

    val teams = listOf(
        TeamDto("team_ars", "Arsenal", "ARS", "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=120", "England"),
        TeamDto("team_che", "Chelsea", "CHE", "https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=120", "England"),
        TeamDto("team_liv", "Liverpool", "LIV", "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=120", "England"),
        TeamDto("team_mci", "Manchester City", "MCI", "https://images.unsplash.com/photo-1518091043644-c1d4457512c6?w=120", "England"),
        TeamDto("team_rm", "Real Madrid", "RMA", "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=120", "Spain"),
        TeamDto("team_bar", "FC Barcelona", "FCB", "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=120", "Spain"),
        TeamDto("team_csk", "Chennai Super Kings", "CSK", "https://images.unsplash.com/photo-1531415074868-036b107e775a?w=120", "India"),
        TeamDto("team_mi", "Mumbai Indians", "MI", "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?w=120", "India"),
        TeamDto("team_lal", "Los Angeles Lakers", "LAL", "https://images.unsplash.com/photo-1546519638-68e109498ffc?w=120", "USA"),
        TeamDto("team_gsw", "Golden State Warriors", "GSW", "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=120", "USA"),
        TeamDto("team_alcaraz", "Carlos Alcaraz", "ALC", "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=120", "Spain"),
        TeamDto("team_sinner", "Jannik Sinner", "SIN", "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=120", "Italy")
    )

    val liveMatches = listOf(
        MatchDto(
            id = "match_101",
            sport = "Football",
            league = leagues[0],
            homeTeam = teams[0],
            awayTeam = teams[1],
            status = "LIVE",
            minute = "74'",
            score = ScoreDto(home = 2, away = 1, period = "2nd Half"),
            startTime = "Today, 19:45",
            venue = "Emirates Stadium, London",
            referee = "Michael Oliver"
        ),
        MatchDto(
            id = "match_102",
            sport = "Cricket",
            league = leagues[2],
            homeTeam = teams[6],
            awayTeam = teams[7],
            status = "LIVE",
            minute = "17.4 ov",
            score = ScoreDto(home = 176, away = 162, period = "Innings 2", details = "CSK need 15 runs in 14 balls"),
            startTime = "Today, 14:00",
            venue = "M. A. Chidambaram Stadium, Chennai",
            referee = "Nitin Menon",
            cricketDetails = CricketDetailsDto(
                runs = 162,
                wickets = 4,
                overs = 17.4,
                currentBatsman = "MS Dhoni (28 off 14)",
                currentBowler = "Jasprit Bumrah (3.4-0-28-2)",
                runRate = 9.17,
                reqRunRate = 6.42,
                partnership = "44 (21b)",
                target = 177,
                balls = listOf("1", "4", "2", "6", "1", "W")
            )
        ),
        MatchDto(
            id = "match_103",
            sport = "Basketball",
            league = leagues[3],
            homeTeam = teams[8],
            awayTeam = teams[9],
            status = "LIVE",
            minute = "Q4 03:45",
            score = ScoreDto(home = 104, away = 101, period = "Quarter 4"),
            startTime = "Today, 20:30",
            venue = "Crypto.com Arena, Los Angeles"
        ),
        MatchDto(
            id = "match_104",
            sport = "Tennis",
            league = leagues[4],
            homeTeam = teams[10],
            awayTeam = teams[11],
            status = "LIVE",
            minute = "Set 3 (4-3)",
            score = ScoreDto(home = 1, away = 1, period = "Set 3", details = "Game score: 30-40 Break Point"),
            startTime = "Today, 16:00",
            venue = "Centre Court"
        )
    )

    val todayMatches = listOf(
        MatchDto(
            id = "match_105",
            sport = "Football",
            league = leagues[1],
            homeTeam = teams[4],
            awayTeam = teams[5],
            status = "UPCOMING",
            minute = "21:00",
            score = ScoreDto(home = 0, away = 0),
            startTime = "Tonight, 21:00",
            venue = "Santiago Bernabéu, Madrid",
            referee = "Szymon Marciniak"
        ),
        MatchDto(
            id = "match_106",
            sport = "Football",
            league = leagues[0],
            homeTeam = teams[2],
            awayTeam = teams[3],
            status = "FINISHED",
            minute = "FT",
            score = ScoreDto(home = 3, away = 2, period = "Full Time"),
            startTime = "Today, 16:30",
            venue = "Anfield, Liverpool",
            referee = "Anthony Taylor"
        )
    )

    val news = listOf(
        NewsItemDto(
            id = "news_1",
            title = "Arsenal strike late to take decisive edge in North London showdown",
            summary = "Bukayo Saka starred in a thrilling encounter that keeps Arsenal at the top of the Premier League table.",
            source = "REVE Sports Desk",
            publishedAt = "15 mins ago",
            sport = "Football",
            imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=600",
            badge = "BREAKING"
        ),
        NewsItemDto(
            id = "news_2",
            title = "IPL 2026: MS Dhoni thriller lights up Chennai as playoff race heats up",
            summary = "A vintage finish leaves fans mesmerized in an electric encounter against Mumbai Indians.",
            source = "CricPulse",
            publishedAt = "45 mins ago",
            sport = "Cricket",
            imageUrl = "https://images.unsplash.com/photo-1531415074868-036b107e775a?w=600",
            badge = "TRENDING"
        ),
        NewsItemDto(
            id = "news_3",
            title = "Lakers edge Warriors with clutch three-pointer in final minute",
            summary = "LeBron James and Anthony Davis deliver masterclass defense to seal wire-to-wire victory.",
            source = "CourtSide News",
            publishedAt = "2 hours ago",
            sport = "Basketball",
            imageUrl = "https://images.unsplash.com/photo-1546519638-68e109498ffc?w=600"
        ),
        NewsItemDto(
            id = "news_4",
            title = "Alcaraz and Sinner wage five-set masterpiece in semifinal duel",
            summary = "The next generation rivalry treats tennis world to unbelievable shotmaking and drama.",
            source = "Tennis Central",
            publishedAt = "3 hours ago",
            sport = "Tennis",
            imageUrl = "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=600"
        )
    )

    val standings = listOf(
        StandingRowDto(1, teams[0], 28, 20, 5, 3, 62, 22, 65),
        StandingRowDto(2, teams[3], 28, 19, 6, 3, 60, 25, 63),
        StandingRowDto(3, teams[2], 28, 18, 7, 3, 64, 28, 61),
        StandingRowDto(4, teams[1], 28, 14, 8, 6, 48, 32, 50),
        StandingRowDto(5, TeamDto("team_tot", "Tottenham", "TOT", "https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=120", "England"), 28, 14, 5, 9, 52, 40, 47)
    )

    fun getMatchDetails(matchId: String): MatchDetailsResponseDto {
        val match = liveMatches.find { it.id == matchId }
            ?: todayMatches.find { it.id == matchId }
            ?: liveMatches.first()

        val events = listOf(
            MatchEventDto("14'", "goal", "Bukayo Saka", "team_ars", "Assisted by Martin Ødegaard with a precision through ball"),
            MatchEventDto("32'", "yellow_card", "Enzo Fernández", "team_che", "Tactical foul breaking counter-attack"),
            MatchEventDto("45+2'", "goal", "Cole Palmer", "team_che", "Stunning curled effort into the top right corner"),
            MatchEventDto("62'", "sub", "Gabriel Martinelli", "team_ars", "Replaced Leandro Trossard"),
            MatchEventDto("71'", "goal", "Kai Havertz", "team_ars", "Header from Bukayo Saka's corner kick"),
            MatchEventDto("73'", "var", "VAR Check Over", "team_ars", "Goal confirmed after offside review")
        )

        val stats = MatchStatsDto(
            homePossession = 58,
            awayPossession = 42,
            homeShots = 14,
            awayShots = 8,
            homeShotsOnTarget = 7,
            awayShotsOnTarget = 3,
            homeCorners = 6,
            awayCorners = 3,
            homeFouls = 9,
            awayFouls = 12,
            homeYellowCards = 1,
            awayYellowCards = 2,
            homeRedCards = 0,
            awayRedCards = 0
        )

        val lineups = LineupsDto(
            homeStarting = listOf(
                PlayerDto("p1", "David Raya", 22, "Goalkeeper", null, "Spain", 28, "Arsenal"),
                PlayerDto("p2", "Ben White", 4, "Defender", null, "England", 26, "Arsenal"),
                PlayerDto("p3", "William Saliba", 2, "Defender", null, "France", 23, "Arsenal"),
                PlayerDto("p4", "Gabriel Magalhães", 6, "Defender", null, "Brazil", 26, "Arsenal"),
                PlayerDto("p5", "Jurriën Timber", 12, "Defender", null, "Netherlands", 23, "Arsenal"),
                PlayerDto("p6", "Declan Rice", 41, "Midfielder", null, "England", 25, "Arsenal"),
                PlayerDto("p7", "Thomas Partey", 5, "Midfielder", null, "Ghana", 30, "Arsenal"),
                PlayerDto("p8", "Martin Ødegaard", 8, "Midfielder", null, "Norway", 25, "Arsenal"),
                PlayerDto("p9", "Bukayo Saka", 7, "Forward", null, "England", 23, "Arsenal"),
                PlayerDto("p10", "Kai Havertz", 29, "Forward", null, "Germany", 25, "Arsenal"),
                PlayerDto("p11", "Gabriel Martinelli", 11, "Forward", null, "Brazil", 23, "Arsenal")
            ),
            awayStarting = listOf(
                PlayerDto("p12", "Robert Sánchez", 1, "Goalkeeper", null, "Spain", 26, "Chelsea"),
                PlayerDto("p13", "Malo Gusto", 27, "Defender", null, "France", 21, "Chelsea"),
                PlayerDto("p14", "Wesley Fofana", 29, "Defender", null, "France", 23, "Chelsea"),
                PlayerDto("p15", "Levi Colwill", 6, "Defender", null, "England", 21, "Chelsea"),
                PlayerDto("p16", "Marc Cucurella", 3, "Defender", null, "Spain", 26, "Chelsea"),
                PlayerDto("p17", "Moisés Caicedo", 25, "Midfielder", null, "Ecuador", 22, "Chelsea"),
                PlayerDto("p18", "Enzo Fernández", 8, "Midfielder", null, "Argentina", 23, "Chelsea"),
                PlayerDto("p19", "Cole Palmer", 20, "Midfielder", null, "England", 22, "Chelsea"),
                PlayerDto("p20", "Noni Madueke", 11, "Forward", null, "England", 22, "Chelsea"),
                PlayerDto("p21", "Nicolas Jackson", 15, "Forward", null, "Senegal", 23, "Chelsea"),
                PlayerDto("p22", "Pedro Neto", 7, "Forward", null, "Portugal", 24, "Chelsea")
            )
        )

        return MatchDetailsResponseDto(match, events, stats, lineups, standings)
    }
}
