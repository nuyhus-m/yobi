from google.cloud import vision
import io
import os
import re
from dotenv import load_dotenv
from datetime import datetime
import logging

# 환경 변수 로드
load_dotenv()

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def detect_table_from_image(image_content):
    """Google Cloud Vision API를 사용하여 이미지에서 표와 텍스트 감지"""
    client = vision.ImageAnnotatorClient()
    image = vision.Image(content=image_content)
    
    # 텍스트 감지 요청
    response = client.document_text_detection(image=image)
    
    # 에러 확인
    if response.error.message:
        logger.error(f"Error: {response.error.message}")
        raise Exception(f"Vision API Error: {response.error.message}")
    
    # 전체 텍스트 로깅
    full_text = response.full_text_annotation.text
    logger.info(f"Detected text: {full_text[:100]}...")  # 처음 100자만 로깅
    
    return response.full_text_annotation

def extract_year_month(text):
    """텍스트에서 연도와 월 추출 (표 상단에 일반적으로 표시됨)"""
    # 표 제목에서 연도와 월 찾기
    year_pattern = r'(20\d{2})년'  # 2000년대 연도
    month_pattern = r'(\d{1,2})월'  # 1~12월
    
    year_match = re.search(year_pattern, text)
    month_match = re.search(month_pattern, text)
    
    # 패턴 매칭 실패시 현재 연도와 월 사용
    current_year = datetime.now().year
    current_month = datetime.now().month
    
    year = int(year_match.group(1)) if year_match else current_year
    month = int(month_match.group(1)) if month_match else current_month
    
    logger.info(f"Extracted year: {year}, month: {month}")
    return year, month

def extract_daily_schedules(annotation, year, month):
    schedules = set()
    full_text = annotation.text
    logger.info(f"Processing full text: {full_text}")
    
    lines = [line.strip() for line in full_text.splitlines() if line.strip()]
    logger.info(f"Total lines: {len(lines)}")
    
    # 날짜+시간+이름이 모두 있는 경우: ex) 13 10:00~13:00 김철수
    date_time_name_pattern = r'(\d{1,2})\s*(\d{2})[:~](\d{2})[~\-](\d{2}):?(\d{2})\s+([가-힣]{2,4})'
    # 시간+이름만 있는 경우: ex) 10:00~13:00 김철수
    time_name_pattern = r'(\d{2})[:~](\d{2})[~\-](\d{2}):?(\d{2})\s+([가-힣]{2,4})'
    date_pattern = r'^(\d{1,2})$'
    
    current_day = None
    for line in lines:
        # 1. 한 줄에 날짜+시간+이름이 여러 개 있을 수도 있음
        for match in re.finditer(date_time_name_pattern, line):
            day, sh, sm, eh, em, name = match.groups()
            if all(0 <= int(x) <= 23 for x in [sh, eh]) and all(0 <= int(x) <= 59 for x in [sm, em]):
                date_str = f"{year}-{month:02d}-{int(day):02d}"
                start_time = f"{int(sh):02d}:{int(sm):02d}"
                end_time = f"{int(eh):02d}:{int(em):02d}"
                schedules.add((date_str, start_time, end_time, name))
                logger.info(f"Extracted schedule (inline): {date_str}, {start_time}, {end_time}, {name}")
        # 2. 날짜만 있는 줄이면 current_day 갱신
        date_match = re.match(date_pattern, line)
        if date_match:
            current_day = int(date_match.group(1))
            logger.info(f"Current day set to: {current_day}")
            continue
        # 3. 날짜가 이미 정해져 있고, 시간+이름만 있는 줄이면 해당 날짜로 스케줄 추가
        if current_day is not None:
            for match in re.finditer(time_name_pattern, line):
                sh, sm, eh, em, name = match.groups()
                if all(0 <= int(x) <= 23 for x in [sh, eh]) and all(0 <= int(x) <= 59 for x in [sm, em]):
                    date_str = f"{year}-{month:02d}-{current_day:02d}"
                    start_time = f"{int(sh):02d}:{int(sm):02d}"
                    end_time = f"{int(eh):02d}:{int(em):02d}"
                    schedules.add((date_str, start_time, end_time, name))
                    logger.info(f"Extracted schedule (date context): {date_str}, {start_time}, {end_time}, {name}")
    # set -> list of dicts
    result = [
        {"date": d, "startAt": s, "endAt": e, "clientName": n}
        for (d, s, e, n) in sorted(schedules)
    ]
    logger.info(f"Total extracted schedules: {len(result)}")
    for idx, schedule in enumerate(result, 1):
        logger.info(f"Schedule {idx}: {schedule}")
    return result

def process_image(image_content, user_id):
    """이미지 처리 및 OCR 결과 반환"""
    try:
        # 텍스트 감지
        annotation = detect_table_from_image(image_content)
        
        # 연도와 월 추출
        year, month = extract_year_month(annotation.text)
        
        # 일정 항목 추출
        schedules = extract_daily_schedules(annotation, year, month)
        
        # 응답 생성
        response = {
            "userId": user_id,
            "year": year,
            "month": month,
            "schedules": schedules
        }
        
        logger.info(f"Final response: {response}")
        return response
    except Exception as e:
        logger.error(f"Error processing image: {str(e)}")
        raise