import redis.asyncio as redis
import os

# 환경 변수에서 Redis 설정 읽기
redis_client = redis.Redis(
    host=os.getenv("REDIS_HOST"),
    port=os.getenv("REDIS_PORT"),
    db=os.getenv("REDIS_DB"),
    decode_responses=True
)
