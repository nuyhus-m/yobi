from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .routers import health_data

app = FastAPI(
    title="Health Data Collection API",
    description="API for collecting and processing health data for AI analysis",
    version="1.0.0"
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 이 부분은 추가로 확인하고 기능 넣을지 소통 필요요
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 라우터 등록
# 라우터 안에 기능들이 들어있습니다. 라우터로 기능을 몰아서 사용하지 않고 라우터 안에있는 형식을 보고 응용하여서 main.py에 사용해도 무관합니다.
app.include_router(health_data.router)

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