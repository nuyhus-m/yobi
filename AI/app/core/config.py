# core/config.py
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    OPENAI_API_KEY: str
    INTERNAL_AI_URL: str = "http://localhost:8001/api"
    
    
    model_config = {
        "env_file": ".env",
        "extra": "allow"  # 추가 필드 허용
    }


settings = Settings()