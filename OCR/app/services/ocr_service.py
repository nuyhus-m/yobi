import logging
import os
import tempfile
from fastapi import HTTPException
from app.utils.vision_utils import process_image

# 로거 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

async def process_ocr_image(image_data):
    """이미지 데이터를 처리하고 OCR 결과 반환"""
    try:
        # 이미지 데이터가 bytes인지 확인
        if not isinstance(image_data, bytes):
            raise ValueError("이미지 데이터가 bytes 형식이 아님")
            
        # 이미지 처리 (bytes 데이터 직접 전달)
        result = process_image(image_data)
        
        if result is None:
            raise HTTPException(status_code=500, detail="OCR 처리 실패")
            
        return result
        
    except Exception as e:
        logger.error(f"Error in OCR service: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Error processing image: {str(e)}")