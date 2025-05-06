from fastapi import FastAPI
from app.routers import ocr_router

app = FastAPI(
    title="OCR API",
    description="OCR 처리를 위한 FastAPI 서버",
    version="1.0.0"
)

app.include_router(ocr_router.router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
