from app.utils.vision_utils import process_image
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

async def process_ocr_image(file, user_id):
    """이미지 파일을 처리하고 OCR 결과 반환"""
    try:
        # 파일 내용 읽기
        image_content = await file.read()
        
        # 이미지 처리
        result = process_image(image_content, user_id)
        
        return result
    except Exception as e:
        logger.error(f"Error in OCR service: {str(e)}")
        raise