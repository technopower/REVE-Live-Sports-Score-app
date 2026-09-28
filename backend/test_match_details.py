import unittest
from fastapi.testclient import TestClient
from backend.main import app

class TestMatchDetails(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.client = TestClient(app)

    def test_health(self):
        response = self.client.get("/health")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["status"], "healthy")
        self.assertTrue(data["sportmonks_configured"])

    def test_match_details_numeric_id(self):
        # Real Sportmonks fixture ID: 19722821 (Aberdeen vs Hearts)
        response = self.client.get("/api/v1/matches/19722821")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertIn("match", data)
        match = data["match"]
        self.assertIn(match["id"], ["sm_19722821", "19722821"])
        self.assertEqual(match["home_team"]["name"], "Aberdeen")
        self.assertEqual(match["away_team"]["name"], "Hearts")
        self.assertEqual(match["score"]["home"], 2)
        self.assertEqual(match["score"]["away"], 1)
        self.assertEqual(match["status"], "Full Time")
        self.assertIsNotNone(match["venue"])

    def test_match_details_prefixed_id(self):
        response = self.client.get("/api/matches/sm_19722821")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["match"]["home_team"]["name"], "Aberdeen")

    def test_match_details_events(self):
        response = self.client.get("/api/v1/matches/19722821")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        events = data["events"]
        self.assertGreater(len(events), 0)

        # Verify goals
        goal_event = next((e for e in events if e["type"] in ["goal", "penalty"]), None)
        self.assertIsNotNone(goal_event)
        self.assertTrue(bool(goal_event["player"]))
        self.assertIn("'", goal_event["minute"])

        # Verify substitutions and cards exist
        has_sub = any(e["type"] == "sub" for e in events)
        has_card = any(e["type"] in ["yellow_card", "red_card"] for e in events)
        self.assertTrue(has_sub)
        self.assertTrue(has_card)

    def test_match_details_statistics(self):
        response = self.client.get("/api/v1/matches/19722821")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        stats = data["stats"]
        self.assertEqual(stats["home_corners"], 4)
        self.assertEqual(stats["away_corners"], 6)
        self.assertEqual(stats["home_possession"], 51)
        self.assertEqual(stats["away_possession"], 49)
        self.assertGreater(len(stats["stats"]), 0)

    def test_match_details_lineups(self):
        response = self.client.get("/api/v1/matches/19722821")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        lineups = data["lineups"]
        # Starting XI
        self.assertEqual(len(lineups["home_starting"]), 11)
        self.assertEqual(len(lineups["away_starting"]), 11)
        # Substitutes
        self.assertGreater(len(lineups["home_subs"]), 0)
        self.assertGreater(len(lineups["away_subs"]), 0)

        first_player = lineups["home_starting"][0]
        self.assertTrue(bool(first_player["name"]))
        self.assertTrue(bool(first_player["position"]))

    def test_match_details_standings(self):
        response = self.client.get("/api/v1/matches/19722821")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        standings = data["standings"]
        self.assertEqual(len(standings), 12)
        first_row = standings[0]
        self.assertEqual(first_row["position"], 1)
        self.assertTrue(bool(first_row["team"]["name"]))
        self.assertGreater(first_row["points"], 0)
        self.assertGreater(first_row["played"], 0)

    def test_match_details_invalid_fixture(self):
        response = self.client.get("/api/v1/matches/non_existent_999999999")
        self.assertIn(response.status_code, [404, 503])

if __name__ == "__main__":
    unittest.main()
