from typing import List, Optional, Dict, Any, Union
from pydantic import BaseModel, Field, field_validator

class TeamDto(BaseModel):
    id: str
    name: str
    short_name: Optional[str] = None
    logo_url: str
    country: Optional[str] = None
    score: Optional[int] = None
    half_time_score: Optional[int] = None
    form: Optional[List[str]] = None # e.g. ["W", "W", "D", "L", "W"]

class LeagueDto(BaseModel):
    id: str
    name: str
    sport: str = "Football"
    country: Optional[str] = None
    logo_url: str
    current_round: Optional[str] = None
    season: Optional[str] = "2025/2026"
    is_popular: bool = False

class MatchEventDto(BaseModel):
    id: str = ""
    minute: Union[str, int] = "" # e.g. "35'", "90+2'", or 35
    extra_minute: Optional[int] = None
    type: str = "goal" # "goal", "yellow_card", "red_card", "sub", "var", "penalty"
    team_id: str = ""
    team_name: str = ""
    player: str = ""
    player_name: Optional[str] = None
    assist_player_name: Optional[str] = None
    description: Optional[str] = None

    @field_validator("minute", mode="before")
    @classmethod
    def format_minute(cls, v: Any) -> str:
        if isinstance(v, int):
            return f"{v}'"
        return str(v)

    def model_post_init(self, __context: Any) -> None:
        if not self.player and self.player_name:
            self.player = self.player_name
        elif not self.player_name and self.player:
            self.player_name = self.player

class StatItemDto(BaseModel):
    name: str # e.g. "Possession", "Shots", "Shots on Target", "Corners", "Fouls", "Offsides", "Yellow Cards", "Red Cards"
    home_value: int = 0
    away_value: int = 0
    is_percentage: bool = False

class MatchStatsDto(BaseModel):
    home_possession: int = 50
    away_possession: int = 50
    home_shots: int = 0
    away_shots: int = 0
    home_shots_on_target: int = 0
    away_shots_on_target: int = 0
    home_corners: int = 0
    away_corners: int = 0
    home_fouls: int = 0
    away_fouls: int = 0
    home_yellow_cards: int = 0
    away_yellow_cards: int = 0
    home_red_cards: int = 0
    away_red_cards: int = 0
    home_offsides: int = 0
    away_offsides: int = 0
    stats: List[StatItemDto] = []

class PlayerDto(BaseModel):
    id: str = ""
    name: str = ""
    number: Optional[int] = None
    position: str = "-" # "GK", "DF", "MF", "FW"
    photo_url: Optional[str] = None
    nationality: Optional[str] = None
    age: Optional[int] = None
    team_name: Optional[str] = None
    is_captain: bool = False
    is_starter: bool = True
    stats: Optional[Dict[str, str]] = None

# Backward compatibility alias
LineupPlayerDto = PlayerDto

class LineupsDto(BaseModel):
    formation_home: Optional[str] = None
    formation_away: Optional[str] = None
    home_starting: List[PlayerDto] = []
    home_subs: List[PlayerDto] = []
    away_starting: List[PlayerDto] = []
    away_subs: List[PlayerDto] = []
    home_players: List[PlayerDto] = []
    away_players: List[PlayerDto] = []

class StandingRowDto(BaseModel):
    position: int = 0
    team: Optional[TeamDto] = None
    played: int = 0
    won: int = 0
    drawn: int = 0
    lost: int = 0
    goals_for: int = 0
    goals_against: int = 0
    goal_difference: Optional[int] = 0
    points: int = 0
    team_id: Optional[str] = None
    team_name: Optional[str] = None
    team_logo: Optional[str] = None
    form: Optional[List[str]] = None

    def model_post_init(self, __context: Any) -> None:
        if self.team is None:
            self.team = TeamDto(
                id=self.team_id or f"team_{self.position}",
                name=self.team_name or "Team",
                logo_url=self.team_logo or "https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=128"
            )

# Backward compatibility alias
StandingItemDto = StandingRowDto

class ScoreDto(BaseModel):
    home: int = 0
    away: int = 0
    period: Optional[str] = None
    details: Optional[str] = None

class LiveMatchDto(BaseModel):
    id: str
    sport: str # "football", "cricket", "basketball", "tennis"
    league: LeagueDto
    home_team: TeamDto
    away_team: TeamDto
    status: str # "LIVE", "HT", "FT", "UPCOMING", "POSTPONED"
    minute: Optional[str] = None # e.g. "72'", "45+2'", "Q3 04:12", "Set 2"
    kickoff_time: str
    start_time: Optional[str] = None
    venue: Optional[str] = None
    referee: Optional[str] = None
    is_live: bool = False
    events_count: int = 0
    extra_info: Optional[Dict[str, Any]] = None
    score: Optional[ScoreDto] = None

    def model_post_init(self, __context: Any) -> None:
        if self.start_time is None:
            self.start_time = self.kickoff_time
        if self.score is None:
            h_score = self.home_team.score or 0
            a_score = self.away_team.score or 0
            self.score = ScoreDto(home=h_score, away=a_score)

class FixtureDetailDto(BaseModel):
    match: LiveMatchDto
    events: List[MatchEventDto] = []
    stats: MatchStatsDto = Field(default_factory=MatchStatsDto)
    lineups: LineupsDto = Field(default_factory=LineupsDto)
    standings: List[StandingRowDto] = []

class CricketScorecardDto(BaseModel):
    match_id: str
    team1: str
    team2: str
    team1_score: str # "182/4 (20.0 ov)"
    team2_score: str # "145/6 (17.2 ov)"
    current_batsmen: List[Dict[str, str]] = [] # [{"name": "Virat Kohli", "runs": "64", "balls": "42", "sr": "152.3"}]
    current_bowler: Optional[Dict[str, str]] = None # {"name": "Jasprit Bumrah", "overs": "3.2", "wickets": "2", "runs": "21"}
    crr: str = "8.42"
    rrr: Optional[str] = "11.20"
    partnership: str = "45 runs (28 balls)"
    last_overs: List[str] = ["1", "4", "W", "0", "6", "1"]
    status_text: str = "Team 2 needs 38 runs in 16 balls"

class BasketballGameDto(BaseModel):
    match_id: str
    quarter: str # "Q3"
    clock: str # "06:45"
    home_quarters: List[int] = [28, 31, 22, 0]
    away_quarters: List[int] = [24, 29, 25, 0]
    home_fouls: int = 3
    away_fouls: int = 2
    top_scorer_home: str = "LeBron James (24 pts)"
    top_scorer_away: str = "Stephen Curry (28 pts)"

class TennisMatchDto(BaseModel):
    match_id: str
    tournament: str = "Wimbledon - Semi-final"
    court: str = "Centre Court"
    player1_sets: List[int] = [6, 4, 3]
    player2_sets: List[int] = [4, 6, 2]
    current_game: str = "30 - 40"
    serving_player: int = 1

class NewsDto(BaseModel):
    id: str
    title: str
    summary: str
    content: Optional[str] = None
    source: str
    category: str # "Football", "Cricket", "Basketball", "Tennis", "Breaking", "Trending"
    image_url: str
    published_at: str
    url: Optional[str] = None
