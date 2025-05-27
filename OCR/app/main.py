# from fastapi import FastAPI
# from fastapi.middleware.cors import CORSMiddleware
# from dotenv import load_dotenv
# import os

# # .env 파일 로드
# load_dotenv()

# # 환경 변수 설정
# DATABASE_URL = os.getenv("DATABASE_URL")
# REDIS_HOST = os.getenv("REDIS_HOST")
# REDIS_PORT = os.getenv("REDIS_PORT")
# REDIS_DB = os.getenv("REDIS_DB")
# API_TITLE = os.getenv("API_TITLE")
# API_DESCRIPTION = os.getenv("API_DESCRIPTION")
# API_VERSION = os.getenv("API_VERSION")

# app = FastAPI(
#     title="Health Data Collection API",
#     description="API for collecting and processing health data for AI analysis",
#     version="1.0.0"
# )

# # CORS 설정
# app.add_middleware(
#     CORSMiddleware,
#     allow_origins=["*"],
#     allow_credentials=True,
#     allow_methods=["*"],
#     allow_headers=["*"],
# )

# @app.get("/")
# async def root():
#     return {
#         "message": "Health Data Collection API",
#         "status": "active"
#     }

# @app.get("/health")
# async def health_check():
#     return {
#         "status": "healthy",
#         "version": "1.0.0"
#     }



from fastapi import FastAPI, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import logging
import os
from dotenv import load_dotenv

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# .env 파일 로드
load_dotenv()

app = FastAPI(
    title="OCR API",
    description="OCR 처리를 위한 FastAPI 서버",
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
        "message": "OCR API",
        "status": "active"
    }

@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "version": "1.0.0"
    }

@app.post("/api/schedules/ocr")
async def process_image(image: UploadFile):
    """이미지를 업로드하고 OCR 처리 결과 반환"""
    logger.info("Received image upload request")
    
    # 이미지 형식 확인
    if not image.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="Invalid file format. Please upload an image file.")
    
    try:
        # 이미지 데이터를 bytes로 읽기
        image_content = await image.read()
        
        # 임시 OCR 결과 (실제 구현은 별도로 필요)
        result = {
            "schedules": [
                {
                    "day": 15,
                    "clientName": "홍길동",
                    "startAt": "09:00",
                    "endAt": "11:00"
                },
                {
                    "day": 16,
                    "clientName": "김영희",
                    "startAt": "14:00",
                    "endAt": "16:00"
                }
            ]
        }
        
        logger.info("OCR processing completed")
        return result
        
    except Exception as e:
        logger.error(f"Error processing image: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Error processing image: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=7000)
