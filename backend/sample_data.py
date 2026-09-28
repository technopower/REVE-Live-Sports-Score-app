from backend.dto import (
    LiveMatchDto, TeamDto, LeagueDto, FixtureDetailDto, MatchEventDto,
    MatchStatsDto, StatItemDto, LineupsDto, LineupPlayerDto, StandingItemDto,
    NewsDto, CricketScorecardDto, BasketballGameDto, TennisMatchDto
)

LEAGUES = [
    LeagueDto(
        id="league_pl",
        name="Premier League",
        sport="Football",
        country="England",
        logo_url="https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=128&q=80",
        current_round="Matchday 29",
        season="2025/2026",
        is_popular=True
    ),
    LeagueDto(
        id="league_ucl",
        name="UEFA Champions League",
        sport="Football",
        country="Europe",
        logo_url="https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=128&q=80",
        current_round="Quarter Finals",
        season="2025/2026",
        is_popular=True
    ),
    LeagueDto(
        id="league_laliga",
        name="La Liga",
        sport="Football",
        country="Spain",
        logo_url="https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=128&q=80",
        current_round="Matchday 30",
        season="2025/2026",
        is_popular=True
    ),
    LeagueDto(
        id="league_seriea",
        name="Serie A",
        sport="Football",
        country="Italy",
        logo_url="https://images.unsplash.com/photo-1518091043644-c1d4457512c6?w=128&q=80",
        current_round="Matchday 28",
        season="2025/2026",
        is_popular=True
    ),
    LeagueDto(
        id="league_ipl",
        name="Indian Premier League (IPL)",
        sport="Cricket",
        country="India",
        logo_url="https://images.unsplash.com/photo-1531415074868-036b107e775a?w=128&q=80",
        current_round="Group Stage",
        season="2026",
        is_popular=True
    ),
    LeagueDto(
        id="league_nba",
        name="NBA Basketball",
        sport="Basketball",
        country="USA",
        logo_url="https://images.unsplash.com/photo-1546519638-68e109498ffc?w=128&q=80",
        current_round="Regular Season",
        season="2025/2026",
        is_popular=True
    ),
    LeagueDto(
        id="league_wimbledon",
        name="Wimbledon Championship",
        sport="Tennis",
        country="United Kingdom",
        logo_url="https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=128&q=80",
        current_round="Quarter Finals",
        season="2026",
        is_popular=True
    )
]

MATCHES: list[LiveMatchDto] = [
    LiveMatchDto(
        id="match_101",
        sport="football",
        league=LEAGUES[0],
        home_team=TeamDto(
            id="team_arsenal",
            name="Arsenal",
            short_name="ARS",
            logo_url="https://images.unsplash.com/photo-1517466787929-bc90951d0974?w=128&q=80",
            country="England",
            score=2,
            half_time_score=1,
            form=["W", "W", "W", "D", "W"]
        ),
        away_team=TeamDto(
            id="team_mancity",
            name="Manchester City",
            short_name="MCI",
            logo_url="https://images.unsplash.com/photo-1489944440615-453fc2b6a9a9?w=128&q=80",
            country="England",
            score=1,
            half_time_score=1,
            form=["W", "D", "W", "W", "L"]
        ),
        status="LIVE",
        minute="74'",
        kickoff_time="20:00",
        venue="Emirates Stadium, London",
        referee="Michael Oliver",
        is_live=True,
        events_count=4
    ),
    LiveMatchDto(
        id="match_102",
        sport="football",
        league=LEAGUES[1],
        home_team=TeamDto(
            id="team_realmadrid",
            name="Real Madrid",
            short_name="RMA",
            logo_url="https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=128&q=80",
            country="Spain",
            score=3,
            half_time_score=1,
            form=["W", "W", "L", "W", "W"]
        ),
        away_team=TeamDto(
            id="team_bayern",
            name="Bayern Munich",
            short_name="BAY",
            logo_url="https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=128&q=80",
            country="Germany",
            score=2,
            half_time_score=2,
            form=["W", "W", "W", "D", "W"]
        ),
        status="LIVE",
        minute="62'",
        kickoff_time="21:00",
        venue="Santiago Bernabéu, Madrid",
        referee="Szymon Marciniak",
        is_live=True,
        events_count=6
    ),
    LiveMatchDto(
        id="match_103",
        sport="cricket",
        league=LEAGUES[4],
        home_team=TeamDto(
            id="team_csk",
            name="Chennai Super Kings",
            short_name="CSK",
            logo_url="https://images.unsplash.com/photo-1531415074868-036b107e775a?w=128&q=80",
            country="India",
            score=192,
            form=["W", "L", "W", "W", "W"]
        ),
        away_team=TeamDto(
            id="team_rcb",
            name="Royal Challengers Bengaluru",
            short_name="RCB",
            logo_url="https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?w=128&q=80",
            country="India",
            score=168,
            form=["L", "W", "W", "L", "W"]
        ),
        status="LIVE",
        minute="18.2 ov",
        kickoff_time="19:30",
        venue="M. A. Chidambaram Stadium, Chennai",
        referee="Nitin Menon",
        is_live=True,
        events_count=8,
        extra_info={"runs": "168/5", "target": "193", "crr": "9.16", "rrr": "15.00"}
    ),
    LiveMatchDto(
        id="match_104",
        sport="basketball",
        league=LEAGUES[5],
        home_team=TeamDto(
            id="team_lakers",
            name="Los Angeles Lakers",
            short_name="LAL",
            logo_url="https://images.unsplash.com/photo-1546519638-68e109498ffc?w=128&q=80",
            country="USA",
            score=86,
            form=["W", "W", "L", "W", "L"]
        ),
        away_team=TeamDto(
            id="team_warriors",
            name="Golden State Warriors",
            short_name="GSW",
            logo_url="https://images.unsplash.com/photo-1519766304817-4f37bda74a29?w=128&q=80",
            country="USA",
            score=82,
            form=["W", "L", "W", "W", "W"]
        ),
        status="LIVE",
        minute="Q3 04:18",
        kickoff_time="22:30",
        venue="Crypto.com Arena, Los Angeles",
        referee="Scott Foster",
        is_live=True,
        events_count=12
    ),
    LiveMatchDto(
        id="match_105",
        sport="tennis",
        league=LEAGUES[6],
        home_team=TeamDto(
            id="team_alcaraz",
            name="Carlos Alcaraz",
            short_name="ALC",
            logo_url="https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=128&q=80",
            country="Spain",
            score=2,
            form=["W", "W", "W", "W", "W"]
        ),
        away_team=TeamDto(
            id="team_sinner",
            name="Jannik Sinner",
            short_name="SIN",
            logo_url="https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=128&q=80",
            country="Italy",
            score=1,
            form=["W", "W", "W", "W", "L"]
        ),
        status="LIVE",
        minute="Set 4 (4-3)",
        kickoff_time="14:00",
        venue="Centre Court, London",
        referee="Eva Asderaki",
        is_live=True,
        events_count=5
    ),
    LiveMatchDto(
        id="match_106",
        sport="football",
        league=LEAGUES[0],
        home_team=TeamDto(
            id="team_liverpool",
            name="Liverpool",
            short_name="LIV",
            logo_url="https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=128&q=80",
            country="England",
            form=["W", "W", "D", "W", "W"]
        ),
        away_team=TeamDto(
            id="team_chelsea",
            name="Chelsea",
            short_name="CHE",
            logo_url="https://images.unsplash.com/photo-1517466787929-bc90951d0974?w=128&q=80",
            country="England",
            form=["L", "W", "W", "D", "W"]
        ),
        status="UPCOMING",
        minute=None,
        kickoff_time="Tomorrow, 17:30",
        venue="Anfield, Liverpool",
        referee="Anthony Taylor",
        is_live=False,
        events_count=0
    ),
    LiveMatchDto(
        id="match_107",
        sport="football",
        league=LEAGUES[2],
        home_team=TeamDto(
            id="team_barcelona",
            name="Barcelona",
            short_name="BAR",
            logo_url="https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=128&q=80",
            country="Spain",
            score=3,
            half_time_score=1,
            form=["W", "W", "W", "W", "D"]
        ),
        away_team=TeamDto(
            id="team_atletico",
            name="Atletico Madrid",
            short_name="ATM",
            logo_url="https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=128&q=80",
            country="Spain",
            score=1,
            half_time_score=0,
            form=["W", "L", "W", "D", "W"]
        ),
        status="FT",
        minute="Full-time",
        kickoff_time="Yesterday",
        venue="Spotify Camp Nou, Barcelona",
        referee="Jesus Gil Manzano",
        is_live=False,
        events_count=5
    )
]

DETAILS_DB: dict[str, FixtureDetailDto] = {
    "match_101": FixtureDetailDto(
        match=MATCHES[0],
        events=[
            MatchEventDto(
                id="ev_1",
                minute=23,
                type="goal",
                team_id="team_arsenal",
                team_name="Arsenal",
                player_name="Bukayo Saka",
                assist_player_name="Martin Ødegaard",
                description="Superb curled shot into top corner"
            ),
            MatchEventDto(
                id="ev_2",
                minute=39,
                type="yellow_card",
                team_id="team_mancity",
                team_name="Manchester City",
                player_name="Rodri",
                description="Tactical foul in midfield"
            ),
            MatchEventDto(
                id="ev_3",
                minute=44,
                type="goal",
                team_id="team_mancity",
                team_name="Manchester City",
                player_name="Erling Haaland",
                assist_player_name="Kevin De Bruyne",
                description="Header from close range"
            ),
            MatchEventDto(
                id="ev_4",
                minute=68,
                type="goal",
                team_id="team_arsenal",
                team_name="Arsenal",
                player_name="Kai Havertz",
                assist_player_name="Gabriel Martinelli",
                description="Tap in following rebound"
            ),
            MatchEventDto(
                id="ev_5",
                minute=71,
                type="substitution",
                team_id="team_mancity",
                team_name="Manchester City",
                player_name="Phil Foden",
                description="Subbed on for Bernardo Silva"
            )
        ],
        stats=MatchStatsDto(
            stats=[
                StatItemDto(name="Possession", home_value=54, away_value=46, is_percentage=True),
                StatItemDto(name="Total Shots", home_value=14, away_value=11),
                StatItemDto(name="Shots on Target", home_value=6, away_value=4),
                StatItemDto(name="Corners", home_value=7, away_value=5),
                StatItemDto(name="Fouls", home_value=10, away_value=13),
                StatItemDto(name="Offsides", home_value=2, away_value=3),
                StatItemDto(name="Yellow Cards", home_value=1, away_value=2),
                StatItemDto(name="Red Cards", home_value=0, away_value=0)
            ]
        ),
        lineups=LineupsDto(
            formation_home="4-3-3",
            formation_away="4-1-4-1",
            home_players=[
                LineupPlayerDto(id="p_1", name="David Raya", number=22, position="GK"),
                LineupPlayerDto(id="p_2", name="Ben White", number=4, position="DF"),
                LineupPlayerDto(id="p_3", name="William Saliba", number=2, position="DF"),
                LineupPlayerDto(id="p_4", name="Gabriel Magalhães", number=6, position="DF"),
                LineupPlayerDto(id="p_5", name="Jurrien Timber", number=12, position="DF"),
                LineupPlayerDto(id="p_6", name="Declan Rice", number=41, position="MF"),
                LineupPlayerDto(id="p_7", name="Thomas Partey", number=5, position="MF"),
                LineupPlayerDto(id="p_8", name="Martin Ødegaard", number=8, position="MF", is_captain=True),
                LineupPlayerDto(id="p_9", name="Bukayo Saka", number=7, position="FW"),
                LineupPlayerDto(id="p_10", name="Kai Havertz", number=29, position="FW"),
                LineupPlayerDto(id="p_11", name="Gabriel Martinelli", number=11, position="FW"),
            ],
            away_players=[
                LineupPlayerDto(id="p_21", name="Ederson", number=31, position="GK"),
                LineupPlayerDto(id="p_22", name="Kyle Walker", number=2, position="DF", is_captain=True),
                LineupPlayerDto(id="p_23", name="Ruben Dias", number=3, position="DF"),
                LineupPlayerDto(id="p_24", name="Manuel Akanji", number=25, position="DF"),
                LineupPlayerDto(id="p_25", name="Josko Gvardiol", number=24, position="DF"),
                LineupPlayerDto(id="p_26", name="Rodri", number=16, position="MF"),
                LineupPlayerDto(id="p_27", name="Kevin De Bruyne", number=17, position="MF"),
                LineupPlayerDto(id="p_28", name="Ilkay Gündogan", number=19, position="MF"),
                LineupPlayerDto(id="p_29", name="Savinho", number=26, position="FW"),
                LineupPlayerDto(id="p_30", name="Erling Haaland", number=9, position="FW"),
                LineupPlayerDto(id="p_31", name="Jeremy Doku", number=11, position="FW"),
            ]
        ),
        standings=[
            StandingItemDto(position=1, team_id="team_arsenal", team_name="Arsenal", team_logo="https://images.unsplash.com/photo-1517466787929-bc90951d0974?w=128&q=80", played=29, won=21, drawn=5, lost=3, goals_for=67, goals_against=24, goal_difference=43, points=68, form=["W", "W", "W", "D", "W"]),
            StandingItemDto(position=2, team_id="team_mancity", team_name="Manchester City", team_logo="https://images.unsplash.com/photo-1489944440615-453fc2b6a9a9?w=128&q=80", played=29, won=20, drawn=6, lost=3, goals_for=65, goals_against=28, goal_difference=37, points=66, form=["W", "D", "W", "W", "L"]),
            StandingItemDto(position=3, team_id="team_liverpool", team_name="Liverpool", team_logo="https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=128&q=80", played=29, won=20, drawn=4, lost=5, goals_for=64, goals_against=30, goal_difference=34, points=64, form=["W", "W", "D", "W", "W"]),
            StandingItemDto(position=4, team_id="team_chelsea", team_name="Chelsea", team_logo="https://images.unsplash.com/photo-1517466787929-bc90951d0974?w=128&q=80", played=29, won=16, drawn=6, lost=7, goals_for=54, goals_against=38, goal_difference=16, points=54, form=["L", "W", "W", "D", "W"])
        ]
    )
}

NEWS_ITEMS = [
    NewsDto(
        id="news_1",
        title="Champions League Quarter-Final Draw: Blockbuster Clashes Confirmed",
        summary="Europe's elite discover their fate as European giants clash in thrilling knock-out stages.",
        source="REVE Sports Desk",
        category="Football",
        image_url="https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=600&q=80",
        published_at="15 mins ago"
    ),
    NewsDto(
        id="news_2",
        title="IPL Thriller: Last-Ball Boundary Decides Classic at Chepauk",
        summary="Electrifying atmosphere as Chennai Super Kings edge past Bangalore in nail-biting finish.",
        source="CricStats Insider",
        category="Cricket",
        image_url="https://images.unsplash.com/photo-1531415074868-036b107e775a?w=600&q=80",
        published_at="42 mins ago"
    ),
    NewsDto(
        id="news_3",
        title="NBA Playoffs Race: Conference Seeds Heat Up with Triple-Double Night",
        summary="Superstars deliver masterclass performances down the stretch as postseason spots hang in balance.",
        source="Hoops Live",
        category="Basketball",
        image_url="https://images.unsplash.com/photo-1546519638-68e109498ffc?w=600&q=80",
        published_at="1 hour ago"
    ),
    NewsDto(
        id="news_4",
        title="Grand Slam Countdown: Top Seeds Gear Up on Grass Courts",
        summary="Intense baseline rallies and booming serves signal a classic fortnight of tennis ahead.",
        source="Tennis Central",
        category="Tennis",
        image_url="https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=600&q=80",
        published_at="2 hours ago"
    ),
    NewsDto(
        id="news_5",
        title="BREAKING: Official Medical Update Ahead of Weekend Derby",
        summary="Key playmaker declared fit to start following late fitness test in dramatic boost.",
        source="REVE Breaking",
        category="Breaking",
        image_url="https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=600&q=80",
        published_at="Just now"
    )
]
