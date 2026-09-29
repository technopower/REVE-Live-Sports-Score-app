import time
from typing import Any, Optional, Dict

class SimpleMemoryCache:
    def __init__(self):
        self._cache: Dict[str, tuple[float, Any]] = {}

    def get(self, key: str) -> Optional[Any]:
        if key in self._cache:
            expire_at, value = self._cache[key]
            if time.time() < expire_at:
                return value
            else:
                del self._cache[key]
        return None

    def set(self, key: str, value: Any, ttl_seconds: int = 60) -> None:
        self._cache[key] = (time.time() + ttl_seconds, value)

    def clear(self) -> None:
        self._cache.clear()

cache = SimpleMemoryCache()