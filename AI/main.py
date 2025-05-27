import sys
import os
from pathlib import Path

# app 디렉토리를 Python 경로에 추가
app_dir = Path(__file__).parent / "app"
sys.path.append(str(app_dir))

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv
from core.database import engine, Base

# .env 파일 로드
load_dotenv()

# 환경 변수 설정
DATABASE_URL = os.getenv("DATABASE_URL")
# REDIS_HOST = os.getenv("REDIS_HOST")
# REDIS_PORT = os.getenv("REDIS_PORT")
# REDIS_DB = os.getenv("REDIS_DB")
# API_TITLE = os.getenv("API_TITLE")
# API_DESCRIPTION = os.getenv("API_DESCRIPTION")
# API_VERSION = os.getenv("API_VERSION")

# 데이터베이스 테이블 생성
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="여기가 찐 AI입니다 !! 아님",
    description="여기가 찐 AI입니다 !! 아님",
    version="1.0.0"
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
async def root():
    return {
        "message": "Health Data Collection API",
        "status": "active"
    }

@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "version": "1.0.0"
    } 