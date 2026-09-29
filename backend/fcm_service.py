import os
import json
import logging
from typing import Dict, List, Optional, Any
from pydantic import BaseModel, Field

logger = logging.getLogger("fcm_service")

class DeviceTokenRegistration(BaseModel):
    token: str
    platform: str = "android"
    user_id: Optional[str] = None
    topics: List[str] = Field(default_factory=list)
    created_at: Optional[str] = None

class NotificationPayload(BaseModel):
    match_id: Optional[str] = None
    type: str # "goal", "red_card", "match_starting", "half_time", "full_time", "favorite_team"
    title: str
    body: str
    team_id: Optional[str] = None
    data: Dict[str, str] = Field(default_factory=dict)

class FCMService:
    def __init__(self):
        self.is_initialized = False
        self._tokens: Dict[str, DeviceTokenRegistration] = {}
        self._dispatched_notifications: List[Dict[str, Any]] = []

        credentials_path = os.getenv("FIREBASE_CREDENTIALS_PATH")
        service_account_json = os.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")

        if credentials_path and os.path.exists(credentials_path):
            try:
                import firebase_admin
                from firebase_admin import credentials
                if not firebase_admin._apps:
                    cred = credentials.Certificate(credentials_path)
                    firebase_admin.initialize_app(cred)
                self.is_initialized = True
                logger.info("[FCMService] Firebase Admin SDK successfully initialized via service account file.")
            except Exception as e:
                logger.warning(f"[FCMService] Firebase Admin initialization failed: {e}")
        elif service_account_json:
            try:
                import firebase_admin
                from firebase_admin import credentials
                if not firebase_admin._apps:
                    cert_dict = json.loads(service_account_json)
                    cred = credentials.Certificate(cert_dict)
                    firebase_admin.initialize_app(cred)
                self.is_initialized = True
                logger.info("[FCMService] Firebase Admin SDK successfully initialized via JSON credentials.")
            except Exception as e:
                logger.warning(f"[FCMService] Firebase Admin initialization failed: {e}")
        else:
            logger.info("[FCMService] Firebase server credentials not present. FCM architecture configured in standby mode.")

    def register_token(self, token: str, platform: str = "android", user_id: Optional[str] = None, topics: Optional[List[str]] = None) -> DeviceTokenRegistration:
        registration = DeviceTokenRegistration(
            token=token,
            platform=platform,
            user_id=user_id,
            topics=topics or []
        )
        self._tokens[token] = registration
        return registration

    def get_registered_tokens(self, user_id: Optional[str] = None) -> List[DeviceTokenRegistration]:
        if user_id:
            return [t for t in self._tokens.values() if t.user_id == user_id]
        return list(self._tokens.values())

    async def send_match_notification(
        self,
        match_id: str,
        type: str,
        title: str,
        body: str,
        team_id: Optional[str] = None,
        extra_data: Optional[Dict[str, str]] = None
    ) -> Dict[str, Any]:
        """
        Dispatches match notifications to registered devices.
        Supported types: 'goal', 'red_card', 'match_starting', 'half_time', 'full_time', 'favorite_team'.
        """
        payload_data = {
            "match_id": match_id,
            "type": type,
            "title": title,
            "body": body,
            **(extra_data or {})
        }
        if team_id:
            payload_data["team_id"] = team_id

        record = {
            "match_id": match_id,
            "type": type,
            "title": title,
            "body": body,
            "team_id": team_id,
            "data": payload_data,
            "recipient_count": len(self._tokens),
            "delivered_live": self.is_initialized
        }
        self._dispatched_notifications.append(record)

        if self.is_initialized:
            try:
                from firebase_admin import messaging
                messages = []
                for token in self._tokens.keys():
                    messages.append(
                        messaging.Message(
                            notification=messaging.Notification(title=title, body=body),
                            data=payload_data,
                            token=token
                        )
                    )
                if messages:
                    response = messaging.send_each(messages)
                    record["success_count"] = response.success_count
                    record["failure_count"] = response.failure_count
            except Exception as e:
                logger.error(f"[FCMService] Failed to send live FCM messages: {e}")
                record["error"] = str(e)

        return record

    def get_dispatched_notifications(self) -> List[Dict[str, Any]]:
        return self._dispatched_notifications

fcm_service = FCMService()
