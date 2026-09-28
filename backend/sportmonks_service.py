import os
from pathlib import Path
from dotenv import load_dotenv
import httpx
from typing import List, Optional, Dict, Any

# Load environment variables from backend/.env or root .env
_env_path = Path(__file__).resolve().parent / ".env"
if _env_path.exists():
    load_dotenv(dotenv_path=_env_path)
else:
    load_dotenv()

from backend.dto import (
    LiveMatchDto, TeamDto, LeagueDto, FixtureDetailDto, MatchEventDto,
    MatchStatsDto, StatItemDto, LineupsDto, PlayerDto, LineupPlayerDto,
    StandingItemDto, StandingRowDto
)
from backend.sample_data import MATCHES, DETAILS_DB, LEAGUES
from backend.cache import cache

SPORTMONKS_BASE_URL = "https://api.sportmonks.com/v3/football"

class SportmonksService:
    def __init__(self):
        self.api_token = os.getenv("SPORTMONKS_API_TOKEN", "").strip()

    @property
    def has_token(self) -> bool:
        return bool(self.api_token and not self.api_token.startswith("your_") and self.api_token != "YOUR_TOKEN_HERE")


    async def get_live_matches(self, sport: Optional[str] = None, allow_demo: bool = False) -> List[LiveMatchDto]:
        sport_lower = (sport or "").lower().strip()

        if self.has_token and (not sport_lower or sport_lower == "football"):
            try:
                async with httpx.AsyncClient(timeout=8.0) as client:
                    resp = await client.get(
                        f"{SPORTMONKS_BASE_URL}/livescores/inplay",
                        params={
                            "api_token": self.api_token,
                            "include": "participants;scores;state;league;events.type;venue"
                        }
                    )
                    if resp.status_code == 200:
                        data = resp.json().get("data", [])
                        normalized = self._normalize_sportmonks_matches(data)
                        if normalized:
                            return normalized
            except Exception as e:
                print(f"[SportmonksService] Live fetch error: {type(e).__name__}")

        if allow_demo:
            results = MATCHES
            if sport_lower and sport_lower != "all":
                results = [m for m in results if m.sport == sport_lower]
            return results

        return []

    async def get_fixtures_by_date(self, date_str: str, sport: Optional[str] = None, allow_demo: bool = False) -> List[LiveMatchDto]:
        sport_lower = (sport or "").lower().strip()

        if self.has_token and (not sport_lower or sport_lower == "football"):
            try:
                async with httpx.AsyncClient(timeout=8.0) as client:
                    resp = await client.get(
                        f"{SPORTMONKS_BASE_URL}/fixtures/date/{date_str}",
                        params={
                            "api_token": self.api_token,
                            "include": "participants;scores;state;league;venue"
                        }
                    )
                    if resp.status_code == 200:
                        data = resp.json().get("data", [])
                        normalized = self._normalize_sportmonks_matches(data)
                        if normalized:
                            return normalized
            except Exception as e:
                print(f"[SportmonksService] Fixtures date fetch error: {type(e).__name__}")

        if allow_demo:
            results = MATCHES
            if sport_lower and sport_lower != "all":
                results = [m for m in results if m.sport == sport_lower]
            return results

        return []

    async def get_fixture_details(self, fixture_id: str, allow_demo: bool = False) -> Optional[FixtureDetailDto]:
        # Support Sportmonks IDs with sm_, sm_fixture_, or direct numeric ID
        clean_id = fixture_id.replace("sm_fixture_", "").replace("sm_", "")
        if self.has_token and (fixture_id.startswith("sm_") or clean_id.isdigit()):
            try:
                async with httpx.AsyncClient(timeout=8.0) as client:
                    resp = await client.get(
                        f"{SPORTMONKS_BASE_URL}/fixtures/{clean_id}",
                        params={
                            "api_token": self.api_token,
                            "include": "participants;scores;state;league;events.type;lineups.player;statistics.type;venue;referees.referee"
                        }
                    )
                    if resp.status_code == 200:
                        data = resp.json().get("data")
                        if data:
                            return await self._normalize_single_fixture_details(data)
            except Exception as e:
                print(f"[SportmonksService] Fixture detail error: {type(e).__name__}")

        if fixture_id in DETAILS_DB:
            return DETAILS_DB[fixture_id]

        if allow_demo:
            for m in MATCHES:
                if m.id == fixture_id:
                    return FixtureDetailDto(
                        match=m,
                        events=[],
                        stats=MatchStatsDto(),
                        lineups=LineupsDto(),
                        standings=[]
                    )

        return None

    async def get_season_standings(self, season_id: int) -> List[StandingRowDto]:
        if not self.has_token or not season_id:
            return []
        cache_key = f"standings_season_{season_id}"
        cached = cache.get(cache_key)
        if cached:
            return [StandingRowDto(**row) for row in cached]

        try:
            async with httpx.AsyncClient(timeout=8.0) as client:
                resp = await client.get(
                    f"{SPORTMONKS_BASE_URL}/standings/seasons/{season_id}",
                    params={
                        "api_token": self.api_token,
                        "include": "participant;details.type"
                    }
                )
                if resp.status_code == 200:
                    sdata = resp.json().get("data", [])
                    standings = []
                    for s in sdata:
                        part = s.get("participant", {}) or {}
                        details = s.get("details", [])
                        dmap = {}
                        for d in details:
                            code = (d.get("type", {}) or {}).get("code")
                            if code:
                                dmap[code] = d.get("value", 0)

                        row = StandingRowDto(
                            position=s.get("position", 0),
                            team=TeamDto(
                                id=f"sm_tm_{part.get('id', '')}",
                                name=part.get("name", "Team"),
                                short_name=part.get("short_code"),
                                logo_url=part.get("image_path") or "https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=128",
                                country=part.get("country")
                            ),
                            played=dmap.get("overall-matches-played", 0),
                            won=dmap.get("overall-won", 0),
                            drawn=dmap.get("overall-draw", 0),
                            lost=dmap.get("overall-lost", 0),
                            goals_for=dmap.get("overall-goals-for", 0),
                            goals_against=dmap.get("overall-goals-against", 0),
                            goal_difference=dmap.get("goal-difference", 0),
                            points=s.get("points", 0),
                            team_id=f"sm_tm_{part.get('id', '')}",
                            team_name=part.get("name", "Team"),
                            team_logo=part.get("image_path")
                        )
                        standings.append(row)

                    cache.set(cache_key, [st.model_dump() for st in standings], ttl_seconds=600)
                    return standings
        except Exception as e:
            print(f"[SportmonksService] Standings fetch error: {type(e).__name__}")
        return []

    async def get_recent_fixtures(self, sport: Optional[str] = None, limit: int = 15) -> List[LiveMatchDto]:
        sport_lower = (sport or "").lower().strip()
        if self.has_token and (not sport_lower or sport_lower == "football"):
            cache_key = f"recent_fixtures_{limit}"
            cached = cache.get(cache_key)
            if cached:
                return [LiveMatchDto(**m) for m in cached]

            try:
                async with httpx.AsyncClient(timeout=8.0) as client:
                    resp = await client.get(
                        f"{SPORTMONKS_BASE_URL}/fixtures",
                        params={
                            "api_token": self.api_token,
                            "order": "desc",
                            "sort": "starting_at",
                            "per_page": limit,
                            "include": "participants;scores;state;league;venue"
                        }
                    )
                    if resp.status_code == 200:
                        data = resp.json().get("data", [])
                        normalized = self._normalize_sportmonks_matches(data)
                        if normalized:
                            cache.set(cache_key, [m.model_dump() for m in normalized], ttl_seconds=120)
                            return normalized
            except Exception as e:
                print(f"[SportmonksService] Recent fixtures fetch error: {type(e).__name__}")
        return []

    async def get_leagues(self, sport: Optional[str] = None) -> List[LeagueDto]:
        sport_lower = (sport or "").lower().strip()
        if self.has_token and (not sport_lower or sport_lower == "football"):
            try:
                async with httpx.AsyncClient(timeout=8.0) as client:
                    resp = await client.get(
                        f"{SPORTMONKS_BASE_URL}/leagues",
                        params={
                            "api_token": self.api_token,
                            "include": "country"
                        }
                    )
                    if resp.status_code == 200:
                        data = resp.json().get("data", [])
                        leagues = []
                        for l in data[:20]: # Top 20 leagues
                            country = l.get("country", {}) or {}
                            leagues.append(
                                LeagueDto(
                                    id=f"sm_lg_{l.get('id')}",
                                    name=l.get("name", "Football League"),
                                    sport="Football",
                                    country=country.get("name"),
                                    logo_url=l.get("image_path") or "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=128",
                                    is_popular=True
                                )
                            )
                        if leagues:
                            return leagues
            except Exception as e:
                print(f"[SportmonksService] Leagues fetch error: {type(e).__name__}")

        results = LEAGUES
        if sport_lower and sport_lower != "all":
            results = [l for l in LEAGUES if l.sport.lower() == sport_lower]
        return results

    def _normalize_sportmonks_matches(self, sm_items: List[Dict[str, Any]]) -> List[LiveMatchDto]:
        normalized = []
        for item in sm_items:
            try:
                participants = item.get("participants", [])
                home = next((p for p in participants if p.get("meta", {}).get("location") == "home"), None)
                away = next((p for p in participants if p.get("meta", {}).get("location") == "away"), None)

                if not home or not away:
                    continue

                scores = item.get("scores", [])
                # Get current score
                current_scores = [s for s in scores if s.get("description") == "CURRENT"]
                if not current_scores:
                    current_scores = scores

                home_score = next((s.get("score", {}).get("goals", 0) for s in current_scores if s.get("participant_id") == home.get("id")), 0)
                away_score = next((s.get("score", {}).get("goals", 0) for s in current_scores if s.get("participant_id") == away.get("id")), 0)

                league_data = item.get("league", {}) or {}
                state_data = item.get("state", {}) or {}
                venue_data = item.get("venue", {}) or {}

                is_live = state_data.get("state") == "inplay"
                status = "LIVE" if is_live else (state_data.get("name") or "UPCOMING")
                minute_val = item.get("minute")
                minute_str = f"{minute_val}'" if minute_val is not None else None

                normalized.append(
                    LiveMatchDto(
                        id=f"sm_{item.get('id')}",
                        sport="football",
                        league=LeagueDto(
                            id=f"sm_lg_{league_data.get('id', '0')}",
                            name=league_data.get("name", "Football League"),
                            logo_url=league_data.get("image_path") or "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=128",
                            sport="Football"
                        ),
                        home_team=TeamDto(
                            id=f"sm_tm_{home.get('id')}",
                            name=home.get("name", "Home Team"),
                            short_name=home.get("short_code"),
                            logo_url=home.get("image_path") or "https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=128",
                            score=home_score
                        ),
                        away_team=TeamDto(
                            id=f"sm_tm_{away.get('id')}",
                            name=away.get("name", "Away Team"),
                            short_name=away.get("short_code"),
                            logo_url=away.get("image_path") or "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=128",
                            score=away_score
                        ),
                        status=status,
                        minute=minute_str,
                        kickoff_time=item.get("starting_at", "Live"),
                        venue=venue_data.get("name"),
                        is_live=is_live
                    )
                )
            except Exception:
                continue
        return normalized

    async def _normalize_single_fixture_details(self, data: Dict[str, Any]) -> FixtureDetailDto:
        # Match dto
        match_list = self._normalize_sportmonks_matches([data])
        match_dto = match_list[0] if match_list else MATCHES[0]

        participants = data.get("participants", [])
        home = next((p for p in participants if p.get("meta", {}).get("location") == "home"), None)
        away = next((p for p in participants if p.get("meta", {}).get("location") == "away"), None)
        home_id = home.get("id") if home else None
        away_id = away.get("id") if away else None

        # Events
        events = []
        for ev in data.get("events", []):
            try:
                ev_type = (ev.get("type", {}) or {}).get("name", "").lower()
                ev_code = (ev.get("type", {}) or {}).get("code", "").lower()
                type_norm = "goal"
                if "yellow" in ev_type or "yellow" in ev_code:
                    type_norm = "yellow_card"
                elif "red" in ev_type or "red" in ev_code:
                    type_norm = "red_card"
                elif "sub" in ev_type or "sub" in ev_code:
                    type_norm = "sub"
                elif "pen" in ev_type or "pen" in ev_code:
                    type_norm = "penalty"
                elif "var" in ev_type or "var" in ev_code:
                    type_norm = "var"

                min_val = ev.get("minute", 0)
                extra = ev.get("extra_minute")
                min_str = f"{min_val}+{extra}'" if extra else f"{min_val}'"

                p_name = ev.get("player_name") or "Player"
                rel_name = ev.get("related_player_name")

                desc = None
                if type_norm == "goal" and rel_name:
                    desc = f"Assist: {rel_name}"
                elif type_norm == "sub" and rel_name:
                    desc = f"In: {p_name}, Out: {rel_name}"
                elif type_norm == "penalty":
                    desc = ev.get("addition") or "Penalty Goal"
                else:
                    desc = ev.get("addition") or ev.get("info")

                part_id = ev.get("participant_id", "")
                team_name = (home.get("name") if part_id == home_id else (away.get("name") if part_id == away_id else "Team")) if home and away else "Team"

                events.append(
                    MatchEventDto(
                        id=str(ev.get("id", len(events))),
                        minute=min_str,
                        extra_minute=extra,
                        type=type_norm,
                        team_id=f"sm_tm_{part_id}",
                        team_name=team_name,
                        player=p_name,
                        player_name=p_name,
                        assist_player_name=rel_name,
                        description=desc
                    )
                )
            except Exception:
                continue

        # Lineups
        position_map = {24: "GK", 25: "DF", 26: "MF", 27: "FW"}
        home_starting, home_subs = [], []
        away_starting, away_subs = [], []

        for lp in data.get("lineups", []):
            try:
                p_info = lp.get("player", {}) or {}
                pos_code = (lp.get("position", {}) or {}).get("code") or position_map.get(lp.get("position_id"), "MF")
                p_name = p_info.get("name") or lp.get("player_name") or "Player"
                type_id = lp.get("type_id") or lp.get("lineup_type_id")
                is_starter = (type_id == 11)

                p_dto = PlayerDto(
                    id=str(lp.get("player_id", "")),
                    name=p_name,
                    number=lp.get("jersey_number"),
                    position=pos_code,
                    is_captain=bool(lp.get("captain")),
                    is_starter=is_starter,
                    photo_url=p_info.get("image_path")
                )
                tid = lp.get("team_id")
                if tid == home_id:
                    if is_starter:
                        home_starting.append(p_dto)
                    else:
                        home_subs.append(p_dto)
                else:
                    if is_starter:
                        away_starting.append(p_dto)
                    else:
                        away_subs.append(p_dto)
            except Exception:
                continue

        lineups = LineupsDto(
            home_starting=home_starting,
            home_subs=home_subs,
            away_starting=away_starting,
            away_subs=away_subs,
            home_players=home_starting + home_subs,
            away_players=away_starting + away_subs
        )

        # Statistics
        stat_map = {}
        for st in data.get("statistics", []):
            try:
                code = (st.get("type", {}) or {}).get("code", "").lower()
                name = (st.get("type", {}) or {}).get("name", "Stat")
                loc = st.get("location")
                if not loc:
                    part_id = st.get("participant_id")
                    loc = "home" if part_id == home_id else "away"
                raw_val = (st.get("data", {}) or {}).get("value", 0)
                try:
                    val = int(raw_val)
                except (ValueError, TypeError):
                    val = 0
                key = code or name.lower()
                if key not in stat_map:
                    stat_map[key] = {"name": name, "home": 0, "away": 0, "is_percentage": "possession" in key}
                stat_map[key][loc] = val
            except Exception:
                continue

        stats_items = []
        for key, entry in stat_map.items():
            stats_items.append(
                StatItemDto(
                    name=entry["name"],
                    home_value=entry["home"],
                    away_value=entry["away"],
                    is_percentage=entry["is_percentage"]
                )
            )

        def get_stat(*aliases):
            for a in aliases:
                for k, v in stat_map.items():
                    if a in k:
                        return v["home"], v["away"]
            return 0, 0

        poss_h, poss_a = get_stat("possession")
        if poss_h == 0 and poss_a == 0 and not stat_map:
            # Default only if completely empty
            poss_h, poss_a = 50, 50

        shots_h, shots_a = get_stat("shots-total", "total shots", "shots")
        sot_h, sot_a = get_stat("shots-on-target", "target")
        corners_h, corners_a = get_stat("corners")
        fouls_h, fouls_a = get_stat("fouls")
        yc_h, yc_a = get_stat("yellowcards", "yellow-cards", "yellow")
        rc_h, rc_a = get_stat("redcards", "red-cards", "red")
        off_h, off_a = get_stat("offsides")

        stats = MatchStatsDto(
            home_possession=poss_h,
            away_possession=poss_a,
            home_shots=shots_h,
            away_shots=shots_a,
            home_shots_on_target=sot_h,
            away_shots_on_target=sot_a,
            home_corners=corners_h,
            away_corners=corners_a,
            home_fouls=fouls_h,
            away_fouls=fouls_a,
            home_yellow_cards=yc_h,
            away_yellow_cards=yc_a,
            home_red_cards=rc_h,
            away_red_cards=rc_a,
            home_offsides=off_h,
            away_offsides=off_a,
            stats=stats_items
        )

        # Standings (fetch real season standings if available)
        season_id = data.get("season_id")
        standings = await self.get_season_standings(season_id) if season_id else []

        return FixtureDetailDto(
            match=match_dto,
            events=events,
            stats=stats,
            lineups=lineups,
            standings=standings
        )

sportmonks_service = SportmonksService()
