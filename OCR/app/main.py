from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import ocr_router
import uvicorn
import logging
from dotenv import load_dotenv
import os

# 환경 변수 로드
load_dotenv()

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Google Cloud 자격 증명 확인
credentials_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
if not os.path.exists(credentials_path):
    logger.warning(f"Google Cloud credentials file not found at: {credentials_path}")

# FastAPI 앱 생성
app = FastAPI(
    title="OCR Schedule Service",
    description="Service for OCR processing of schedule images",
    version="1.0.0"
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 실제 운영 환경에서는 구체적인 출처 지정 필요
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 라우터 등록
app.include_router(ocr_router.router)

@app.get("/")
async def root():
    return {"message": "Welcome to OCR Schedule Service"}

# 서버 실행 (직접 실행하는 경우)
if __name__ == "__main__":
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)