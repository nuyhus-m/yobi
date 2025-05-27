# core/config.py
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    OPENAI_API_KEY: str
    INTERNAL_AI_URL: str = "http://localhost:8001/api"

    DATABASE_URL: str 
    # PostgreSQL 설정
    POSTGRES_USER: str
    POSTGRES_PASSWORD: str
    POSTGRES_DB: str
    
    # DATABASE_URL 계산 메서드 추가
    @property
    def DATABASE_URL(self) -> str:
        return f"postgresql://{self.POSTGRES_USER}:{self.POSTGRES_PASSWORD}@k12s209.p.ssafy.io:5432/{self.POSTGRES_DB}"
    
    
    model_config = {
        "env_file": ".env",
        "extra": "allow"  # 추가 필드 허용
    }


settings = Settings()