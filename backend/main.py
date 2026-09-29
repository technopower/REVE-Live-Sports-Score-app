from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from datetime import datetime, timezone

app = FastAPI(title="REVE Live Sports API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

LIVE_MATCHES = [
    {
        "id": "football-001",
        "sport": "Football",
        "league": "Premier League",
        "status": "LIVE",
        "minute": 72,
        "home": {"name": "Man United", "short": "MUN", "score": 1},
        "away": {"name": "Liverpool", "short": "LIV", "score": 2},
    },
    {
        "id": "cricket-001",
        "sport": "Cricket",
        "league": "Asia Cup - ODI",
        "status": "LIVE",
        "minute": 24,
        "home": {"name": "Bangladesh", "short": "BAN", "score": "132/4"},
        "away": {"name": "Sri Lanka", "short": "SL", "score": ""},
    },
    {
        "id": "basketball-001",
        "sport": "Basketball",
        "league": "NBA",
        "status": "LIVE",
        "minute": 0,
        "home": {"name": "Lakers", "short": "LAL", "score": 78},
        "away": {"name": "Celtics", "short": "BOS", "score": 65},
    },
]

@app.get("/health")
def health():
    return {
        "status": "ok",
        "service": "REVE Live Sports API",
        "time": datetime.now(timezone.utc).isoformat(),
    }

@app.get("/api/v1/live")
def live_matches():
    return {"success": True, "matches": LIVE_MATCHES}

@app.get("/api/v1/matches")
def matches():
    return {"success": True, "matches": LIVE_MATCHES}

@app.get("/api/v1/matches/{match_id}")
def match_details(match_id: str):
    match = next((m for m in LIVE_MATCHES if m["id"] == match_id), None)
    if not match:
        return {"success": False, "error": "Match not found"}
    return {
        "success": True,
        "match": match,
        "events": [
            {"minute": 72, "type": "goal", "player": "Mohamed Salah", "team": "Liverpool"},
            {"minute": 64, "type": "card", "player": "Bruno Fernandes", "team": "Man United"},
            {"minute": 58, "type": "goal", "player": "Darwin Nunez", "team": "Liverpool"},
            {"minute": 45, "type": "substitution", "player": "R. Hojlund", "team": "Man United"},
        ],
        "stats": {
            "possession": [47, 53],
            "shots": [12, 16],
        },
    }

@app.get("/api/v1/leagues")
def leagues():
    return {
        "success": True,
        "leagues": [
            {"name": "Premier League", "country": "England"},
            {"name": "La Liga", "country": "Spain"},
            {"name": "Serie A", "country": "Italy"},
            {"name": "Bundesliga", "country": "Germany"},
            {"name": "Ligue 1", "country": "France"},
            {"name": "UEFA Champions League", "country": "Europe"},
            {"name": "UEFA Europa League", "country": "Europe"},
        ],
    }
