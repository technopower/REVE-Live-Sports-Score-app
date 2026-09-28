from typing import Dict, List, Optional
from pydantic import BaseModel, Field

class FavoriteItemDto(BaseModel):
    id: str
    type: str # "team", "league", "match", "player"
    title: str
    subtitle: Optional[str] = None
    imageUrl: Optional[str] = Field(default=None, alias="imageUrl")
    image_url: Optional[str] = None
    sport: Optional[str] = "Football"
    created_at: Optional[str] = None

    def model_post_init(self, __context) -> None:
        if not self.image_url and self.imageUrl:
            self.image_url = self.imageUrl
        elif not self.imageUrl and self.image_url:
            self.imageUrl = self.image_url

class FavoritesService:
    def __init__(self):
        # Maps user_id (or 'default') to a dict of {id: FavoriteItemDto}
        self._storage: Dict[str, Dict[str, FavoriteItemDto]] = {}

    def _get_user_key(self, user_id: Optional[str]) -> str:
        return user_id if user_id else "global_scope"

    def add_favorite(self, item: FavoriteItemDto, user_id: Optional[str] = None) -> FavoriteItemDto:
        key = self._get_user_key(user_id)
        if key not in self._storage:
            self._storage[key] = {}
        self._storage[key][item.id] = item
        return item

    def delete_favorite(self, item_id: str, user_id: Optional[str] = None) -> bool:
        key = self._get_user_key(user_id)
        if key in self._storage and item_id in self._storage[key]:
            del self._storage[key][item_id]
            return True
        return False

    def get_favorites(self, user_id: Optional[str] = None) -> List[FavoriteItemDto]:
        key = self._get_user_key(user_id)
        return list(self._storage.get(key, {}).values())

favorites_service = FavoritesService()
