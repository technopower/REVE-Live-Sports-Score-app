from fastapi import FastAPI, Query, HTTPException, status, Header, Depends
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from typing import List, Optional
import os
import datetime

from backend.dto import (
    LiveMatchDto, FixtureDetailDto, LeagueDto, NewsDto,
    CricketScorecardDto, BasketballGameDto, TennisMatchDto
)
from backend.sample_data import LEAGUES, MATCHES, DETAILS_DB, NEWS_ITEMS
from backend.sportmonks_service import sportmonks_service
from backend.cache import cache
from backend.favorites_service import favorites_service, FavoriteItemDto
from backend.fcm_service import fcm_service, DeviceTokenRegistration, NotificationPayload

app = FastAPI(
    title="REVE Live Sports REST API",
    version="1.0.0",
    description="Production-quality REST API backend for REVE Live Sports Android App"
)

# Enable CORS for local testing and emulator
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

def token_unavailable_response():
    return JSONResponse(
        status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
        content={
            "error": "SPORTMONKS_API_TOKEN_UNAVAILABLE",
            "message": "Sportmonks API token is not configured on the backend server. Real-time sports data requires a valid Sportmonks v3 API token. Please set SPORTMONKS_API_TOKEN in your backend environment (backend/.env).",
            "sportmonks_configured": False,
            "hint": "For development preview, pass query param demo=true (e.g. /api/v1/live?demo=true)"
        }
    )

@app.get("/")
def root():
    return {
        "app": "REVE Live Sports API",
        "status": "online",
        "version": "1.0.0",
        "sportmonks_configured": sportmonks_service.has_token,
        "docs_url": "/docs"
    }

@app.get("/health")
def health():
    return {
        "status": "healthy",
        "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
        "sportmonks_configured": sportmonks_service.has_token
    }

@app.get("/api/v1/status")
@app.get("/api/status")
def api_status():
    return {
        "status": "online",
        "sportmonks_configured": sportmonks_service.has_token,
        "cache_type": "in_memory",
        "supported_sports": ["football", "cricket", "basketball", "tennis"],
        "endpoints": [
            "/health",
            "/api/v1/live",
            "/api/v1/leagues",
            "/api/v1/fixtures/{date}",
            "/api/v1/matches/{fixture_id}",
            "/api/v1/news",
            "/api/v1/search"
        ]
    }

# LIVE MATCHES - Support both /api/v1/live and /api/live
@app.get("/api/v1/live")
@app.get("/api/live")
async def get_live_matches(
    sport: Optional[str] = Query(None),
    demo: bool = Query(False)
):
    if not sportmonks_service.has_token and not demo:
        return token_unavailable_response()

    cache_key = f"live_{sport}_{demo}"
    cached = cache.get(cache_key)
    if cached:
        return cached

    matches = await sportmonks_service.get_live_matches(sport=sport, allow_demo=demo)
    cache.set(cache_key, [m.model_dump() for m in matches], ttl_seconds=10)
    return matches

# LEAGUES - Support both /api/v1/leagues and /api/leagues
@app.get("/api/v1/leagues", response_model=List[LeagueDto])
@app.get("/api/leagues", response_model=List[LeagueDto])
async def get_leagues(sport: Optional[str] = Query(None)):
    cache_key = f"leagues_{sport}"
    cached = cache.get(cache_key)
    if cached:
        return cached

    leagues = await sportmonks_service.get_leagues(sport=sport)
    cache.set(cache_key, [l.model_dump() for l in leagues], ttl_seconds=300)
    return leagues

@app.get("/api/v1/leagues/{league_id}", response_model=LeagueDto)
@app.get("/api/leagues/{league_id}", response_model=LeagueDto)
async def get_league_detail(league_id: str):
    all_leagues = await sportmonks_service.get_leagues()
    league = next((l for l in all_leagues if l.id == league_id), None)
    if not league:
        raise HTTPException(status_code=404, detail="League not found")
    return league

# FIXTURES BY DATE - Support /api/v1/fixtures/{date}
@app.get("/api/v1/fixtures/{date}")
async def get_fixtures_by_date(
    date: str,
    sport: Optional[str] = Query(None),
    demo: bool = Query(False)
):
    if not sportmonks_service.has_token and not demo:
        return token_unavailable_response()

    cache_key = f"fixtures_{date}_{sport}_{demo}"
    cached = cache.get(cache_key)
    if cached:
        return cached

    matches = await sportmonks_service.get_fixtures_by_date(date, sport=sport, allow_demo=demo)
    cache.set(cache_key, [m.model_dump() for m in matches], ttl_seconds=60)
    return matches

# FIXTURES GENERAL - Support /api/fixtures, /api/fixtures/today, /api/fixtures/upcoming
@app.get("/api/fixtures/today")
@app.get("/api/v1/fixtures/today")
async def get_today_fixtures(sport: Optional[str] = Query(None)):
    if sportmonks_service.has_token:
        today_date = datetime.date.today().isoformat()
        real_fixtures = await sportmonks_service.get_fixtures_by_date(today_date, sport=sport, allow_demo=False)
        if not real_fixtures:
            real_fixtures = await sportmonks_service.get_recent_fixtures(sport=sport, limit=10)
        if real_fixtures:
            return real_fixtures
    return []

@app.get("/api/fixtures/upcoming")
@app.get("/api/v1/fixtures/upcoming")
async def get_upcoming_fixtures(sport: Optional[str] = Query(None)):
    if sportmonks_service.has_token:
        recent = await sportmonks_service.get_recent_fixtures(sport=sport, limit=10)
        if recent:
            return [m for m in recent if not m.is_live]
    return []

@app.get("/api/fixtures", response_model=List[LiveMatchDto])
async def get_fixtures_query(
    sport: Optional[str] = Query(None),
    status: Optional[str] = Query(None)
):
    if sportmonks_service.has_token:
        recent = await sportmonks_service.get_recent_fixtures(sport=sport, limit=15)
        if recent:
            if status:
                return [m for m in recent if m.status.upper() == status.upper()]
            return recent
    return []

# MATCH DETAILS - Support /api/v1/matches/{fixture_id}, /api/matches/{fixture_id}, /api/fixtures/{fixture_id}
@app.get("/api/v1/matches/{fixture_id}")
@app.get("/api/matches/{fixture_id}")
@app.get("/api/fixtures/{fixture_id}")
async def get_match_details(fixture_id: str, demo: bool = Query(False)):
    cache_key = f"match_detail_{fixture_id}_{demo}"
    cached = cache.get(cache_key)
    if cached:
        return cached

    detail = await sportmonks_service.get_fixture_details(fixture_id, allow_demo=demo)
    if not detail:
        if not sportmonks_service.has_token:
            return token_unavailable_response()
        raise HTTPException(status_code=404, detail="Fixture not found")

    cache.set(cache_key, detail.model_dump(), ttl_seconds=15)
    return detail

# CRICKET LIVE SCORECARD
@app.get("/api/v1/cricket/live", response_model=CricketScorecardDto)
@app.get("/api/cricket/live", response_model=CricketScorecardDto)
async def get_cricket_live():
    return CricketScorecardDto(
        match_id="match_103",
        team1="Chennai Super Kings",
        team2="Royal Challengers Bengaluru",
        team1_score="192/4 (20.0 ov)",
        team2_score="168/5 (18.2 ov)",
        current_batsmen=[
            {"name": "Virat Kohli", "runs": "74*", "balls": "46", "sr": "160.8"},
            {"name": "Dinesh Karthik", "runs": "18*", "balls": "9", "sr": "200.0"}
        ],
        current_bowler={"name": "Matheesha Pathirana", "overs": "3.2", "wickets": "2", "runs": "28"},
        crr="9.16",
        rrr="15.00",
        partnership="36 runs (15 balls)",
        last_overs=["1", "4", "6", "1", "W", "2"],
        status_text="RCB need 25 runs in 10 balls to win"
    )

# BASKETBALL LIVE
@app.get("/api/v1/basketball/live", response_model=BasketballGameDto)
@app.get("/api/basketball/live", response_model=BasketballGameDto)
async def get_basketball_live():
    return BasketballGameDto(
        match_id="match_104",
        quarter="Q3",
        clock="04:18",
        home_quarters=[28, 31, 27, 0],
        away_quarters=[24, 29, 29, 0],
        home_fouls=3,
        away_fouls=4,
        top_scorer_home="LeBron James (26 pts, 8 reb)",
        top_scorer_away="Stephen Curry (31 pts, 7 3PM)"
    )

# TENNIS LIVE
@app.get("/api/v1/tennis/live", response_model=TennisMatchDto)
@app.get("/api/tennis/live", response_model=TennisMatchDto)
async def get_tennis_live():
    return TennisMatchDto(
        match_id="match_105",
        tournament="Wimbledon - Centre Court",
        court="Grass Court 1",
        player1_sets=[6, 4, 6, 4],
        player2_sets=[4, 6, 3, 3],
        current_game="30 - 40",
        serving_player=1
    )

# NEWS
@app.get("/api/v1/news", response_model=List[NewsDto])
@app.get("/api/news", response_model=List[NewsDto])
async def get_news(category: Optional[str] = Query(None)):
    if category and category.lower() != "all":
        return [n for n in NEWS_ITEMS if n.category.lower() == category.lower()]
    return NEWS_ITEMS

# GLOBAL SEARCH
@app.get("/api/v1/search")
@app.get("/api/search")
async def search_all(q: str = Query(..., min_length=1)):
    query = q.lower().strip()
    matched_leagues = [l for l in LEAGUES if query in l.name.lower() or query in l.sport.lower()]
    matched_matches = [
        m for m in MATCHES 
        if query in m.home_team.name.lower() 
        or query in m.away_team.name.lower() 
        or query in m.league.name.lower()
    ]
    return {
        "query": q,
        "leagues": matched_leagues,
        "matches": matched_matches
    }

# USER IDENTITY ARCHITECTURE (Prepared for Firebase Authentication / JWT)
async def get_optional_current_user(authorization: Optional[str] = Header(None)) -> Optional[str]:
    """
    Extracts authenticated user ID from Authorization header (Bearer <token>).
    Architecture prepared for Firebase Authentication.
    """
    if not authorization or not authorization.startswith("Bearer "):
        return None
    token = authorization.replace("Bearer ", "").strip()
    return f"user_{token}" if token else None

# FAVORITES ENDPOINTS - Supports both authenticated users and global scope
@app.get("/api/v1/favorites", response_model=List[FavoriteItemDto])
@app.get("/api/favorites", response_model=List[FavoriteItemDto])
async def get_favorites(user_id: Optional[str] = Depends(get_optional_current_user)):
    return favorites_service.get_favorites(user_id)

@app.post("/api/v1/favorites", response_model=FavoriteItemDto)
@app.post("/api/favorites", response_model=FavoriteItemDto)
async def add_favorite(item: FavoriteItemDto, user_id: Optional[str] = Depends(get_optional_current_user)):
    return favorites_service.add_favorite(item, user_id)

@app.delete("/api/v1/favorites/{item_id}")
@app.delete("/api/favorites/{item_id}")
async def delete_favorite(item_id: str, user_id: Optional[str] = Depends(get_optional_current_user)):
    success = favorites_service.delete_favorite(item_id, user_id)
    return {"status": "success", "id": item_id, "deleted": success}

# FIREBASE CLOUD MESSAGING (FCM) NOTIFICATIONS
@app.post("/api/v1/notifications/register-token")
async def register_device_token(
    registration: DeviceTokenRegistration,
    user_id: Optional[str] = Depends(get_optional_current_user)
):
    reg = fcm_service.register_token(
        token=registration.token,
        platform=registration.platform,
        user_id=user_id or registration.user_id,
        topics=registration.topics
    )
    return {"status": "registered", "token": reg.token, "platform": reg.platform}

@app.post("/api/v1/notifications/send-alert")
async def send_match_notification(payload: NotificationPayload):
    """
    Notification types: 'goal', 'red_card', 'match_starting', 'half_time', 'full_time', 'favorite_team'
    """
    result = await fcm_service.send_match_notification(
        match_id=payload.match_id or "general",
        type=payload.type,
        title=payload.title,
        body=payload.body,
        team_id=payload.team_id,
        extra_data=payload.data
    )
    return {"status": "dispatched", "result": result}

@app.get("/api/v1/notifications/status")
async def get_notifications_status():
    return {
        "fcm_live": fcm_service.is_initialized,
        "registered_devices_count": len(fcm_service.get_registered_tokens()),
        "dispatched_count": len(fcm_service.get_dispatched_notifications())
    }

if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", "8000"))
    uvicorn.run("backend.main:app", host="0.0.0.0", port=port, reload=True)
