from fastapi import APIRouter, UploadFile, Form, HTTPException
from app.services.ocr_service import process_ocr_image
from app.models.schemas import OcrResponse
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

router = APIRouter(
    prefix="/api/schedules",
    tags=["OCR"],
    responses={404: {"description": "Not found"}},
)

@router.post("/ocr", response_model=OcrResponse)
async def process_image(
    image: UploadFile
):
    """이미지를 업로드하고 OCR 처리 결과 반환"""
    logger.info("Received image upload request")
    
    # 이미지 형식 확인
    if not image.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="Invalid file format. Please upload an image file.")
    
    try:
        # 이미지 데이터를 bytes로 읽기
        image_content = await image.read()
        
        # OCR 서비스 호출
        result = await process_ocr_image(image_content)
        
        if result is None:
            raise HTTPException(status_code=500, detail="OCR processing failed")
            
        logger.info("OCR processing completed")
        return result
        
    except Exception as e:
        logger.error(f"Error processing image: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Error processing image: {str(e)}")