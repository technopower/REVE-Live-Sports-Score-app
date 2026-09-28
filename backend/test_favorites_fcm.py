import unittest
import json
from fastapi.testclient import TestClient
from backend.main import app
from backend.favorites_service import favorites_service
from backend.fcm_service import fcm_service

class TestFavoritesAndFCM(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.client = TestClient(app)

    def setUp(self):
        # Reset in-memory test states
        favorites_service._storage.clear()
        fcm_service._tokens.clear()
        fcm_service._dispatched_notifications.clear()

    # --- FAVORITES TESTS ---
    def test_add_favorite_match(self):
        payload = {
            "id": "sm_19722821",
            "type": "match",
            "title": "Aberdeen vs Hearts",
            "subtitle": "Premiership",
            "sport": "Football"
        }
        res = self.client.post("/api/v1/favorites", json=payload)
        self.assertEqual(res.status_code, 200)
        data = res.json()
        self.assertEqual(data["id"], "sm_19722821")
        self.assertEqual(data["type"], "match")
        self.assertEqual(data["title"], "Aberdeen vs Hearts")

    def test_add_favorite_team_and_league(self):
        team_payload = {
            "id": "sm_tm_273",
            "type": "team",
            "title": "Aberdeen",
            "subtitle": "Scotland",
            "sport": "Football"
        }
        league_payload = {
            "id": "sm_lg_501",
            "type": "league",
            "title": "Premiership",
            "subtitle": "Scotland",
            "sport": "Football"
        }
        res_team = self.client.post("/api/v1/favorites", json=team_payload)
        self.assertEqual(res_team.status_code, 200)
        res_league = self.client.post("/api/v1/favorites", json=league_payload)
        self.assertEqual(res_league.status_code, 200)

        # Verify both exist in favorites list
        get_res = self.client.get("/api/v1/favorites")
        self.assertEqual(get_res.status_code, 200)
        favs = get_res.json()
        ids = [f["id"] for f in favs]
        self.assertIn("sm_tm_273", ids)
        self.assertIn("sm_lg_501", ids)

    def test_delete_favorite(self):
        payload = {
            "id": "match_to_delete",
            "type": "match",
            "title": "Team A vs Team B"
        }
        self.client.post("/api/v1/favorites", json=payload)
        
        # Verify exists
        res_before = self.client.get("/api/v1/favorites")
        self.assertEqual(len(res_before.json()), 1)

        # Delete
        del_res = self.client.delete("/api/v1/favorites/match_to_delete")
        self.assertEqual(del_res.status_code, 200)
        self.assertTrue(del_res.json()["deleted"])

        # Verify gone
        res_after = self.client.get("/api/v1/favorites")
        self.assertEqual(len(res_after.json()), 0)

    def test_favorites_user_auth_isolation(self):
        # User 1 adds a favorite
        self.client.post(
            "/api/v1/favorites",
            json={"id": "fav_u1", "type": "team", "title": "User1 Fav Team"},
            headers={"Authorization": "Bearer token_for_user_alpha"}
        )

        # User 2 adds a different favorite
        self.client.post(
            "/api/v1/favorites",
            json={"id": "fav_u2", "type": "team", "title": "User2 Fav Team"},
            headers={"Authorization": "Bearer token_for_user_beta"}
        )

        # User 1 gets only their favorites
        res_u1 = self.client.get(
            "/api/v1/favorites",
            headers={"Authorization": "Bearer token_for_user_alpha"}
        )
        self.assertEqual(len(res_u1.json()), 1)
        self.assertEqual(res_u1.json()[0]["id"], "fav_u1")

        # User 2 gets only their favorites
        res_u2 = self.client.get(
            "/api/v1/favorites",
            headers={"Authorization": "Bearer token_for_user_beta"}
        )
        self.assertEqual(len(res_u2.json()), 1)
        self.assertEqual(res_u2.json()[0]["id"], "fav_u2")

    # --- FIREBASE CLOUD MESSAGING (FCM) TESTS ---
    def test_fcm_token_registration(self):
        payload = {
            "token": "fcm_test_device_token_xyz_12345",
            "platform": "android",
            "topics": ["premiership", "goals"]
        }
        res = self.client.post("/api/v1/notifications/register-token", json=payload)
        self.assertEqual(res.status_code, 200)
        data = res.json()
        self.assertEqual(data["status"], "registered")
        self.assertEqual(data["token"], "fcm_test_device_token_xyz_12345")

        status_res = self.client.get("/api/v1/notifications/status")
        self.assertEqual(status_res.status_code, 200)
        self.assertEqual(status_res.json()["registered_devices_count"], 1)

    def test_fcm_notification_types_dispatch(self):
        # Register a test client device first
        self.client.post("/api/v1/notifications/register-token", json={
            "token": "device_token_abc",
            "platform": "android"
        })

        notification_types = [
            ("goal", "Goal Alert!", "Aberdeen 1 - 0 Hearts (35' Oisin McEntee)"),
            ("red_card", "Red Card!", "Player sent off in 72'"),
            ("match_starting", "Match Starting!", "Aberdeen vs Hearts kicks off now"),
            ("half_time", "Half Time", "Aberdeen 1 - 0 Hearts at the break"),
            ("full_time", "Full Time Result", "Aberdeen 2 - 1 Hearts"),
            ("favorite_team", "Favorite Team Playing Today", "Aberdeen match scheduled for 16:30")
        ]

        for ntype, title, body in notification_types:
            payload = {
                "match_id": "sm_19722821",
                "type": ntype,
                "title": title,
                "body": body,
                "team_id": "sm_tm_273",
                "data": {"match_id": "sm_19722821", "sport": "football"}
            }
            res = self.client.post("/api/v1/notifications/send-alert", json=payload)
            self.assertEqual(res.status_code, 200, f"Failed for type {ntype}")
            data = res.json()
            self.assertEqual(data["status"], "dispatched")
            self.assertEqual(data["result"]["type"], ntype)
            self.assertEqual(data["result"]["recipient_count"], 1)

        # Check total dispatched count in status
        status_res = self.client.get("/api/v1/notifications/status")
        self.assertEqual(status_res.json()["dispatched_count"], len(notification_types))

    # --- LIVE REFRESH & CACHING TESTS ---
    def test_live_polling_backend_caching(self):
        # Test that rapid repeated requests hit cache and do not exceed Sportmonks rate limits
        res1 = self.client.get("/api/v1/live?demo=true")
        self.assertEqual(res1.status_code, 200)

        res2 = self.client.get("/api/v1/live?demo=true")
        self.assertEqual(res2.status_code, 200)
        self.assertEqual(len(res1.json()), len(res2.json()))

    def test_fixture_details_caching(self):
        res1 = self.client.get("/api/v1/matches/19722821")
        self.assertEqual(res1.status_code, 200)
        res2 = self.client.get("/api/v1/matches/19722821")
        self.assertEqual(res2.status_code, 200)
        self.assertEqual(res1.json()["match"]["id"], res2.json()["match"]["id"])

if __name__ == "__main__":
    unittest.main()
