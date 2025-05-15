# core/config.py
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    OPENAI_API_KEY: str
    INTERNAL_AI_URL: str = "http://localhost:8001/api"
    
    class Config:
        env_file = ".env"

settings = Settings()