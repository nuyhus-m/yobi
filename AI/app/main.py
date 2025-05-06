from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv
import os

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

app = FastAPI(
    title="여기가 찐 AI입니다 !! OCR 아님",
    description="여기가 찐 AI입니다 !! OCR 아님",
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
