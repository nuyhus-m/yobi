from google.cloud import vision
import io
import os
import re
from dotenv import load_dotenv
from datetime import datetime
import logging
import numpy as np

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
    
    return response

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

def extract_schedules_position_based(text_blocks, year, month):
    """위치 기반 일정 추출 - 달력 구조를 명확하게 인식"""
    # 요일 헤더 식별 (일, 월, 화, 수, 목, 금, 토)
    weekday_headers = []
    for block in text_blocks:
        if block["text"] in ["일", "월", "화", "수", "목", "금", "토"]:
            weekday_headers.append(block)
    
    # 요일 헤더 정렬
    sorted_headers = sorted(weekday_headers, key=lambda x: x["center_x"])
    
    # 칸 너비 계산
    if len(sorted_headers) < 7:
        logger.warning(f"요일 헤더 감지 부족: {len(sorted_headers)}개")
        # 이미지 너비 추정
        img_width = max([block["max_x"] for block in text_blocks]) if text_blocks else 1800
        # 7개 칸으로 나누기
        column_widths = [img_width / 7] * 7
        column_centers = [i * img_width / 7 + img_width / 14 for i in range(7)]
    else:
        # 각 요일 헤더의 중심점으로 열 중심 설정
        column_centers = [header["center_x"] for header in sorted_headers]
        # 칸 너비 추정
        if len(column_centers) > 1:
            column_widths = [column_centers[i+1] - column_centers[i] for i in range(len(column_centers)-1)]
            column_widths.append(column_widths[-1])  # 마지막 열 너비는 이전과 동일하게
        else:
            column_widths = [200] * 7  # 기본값
    
    # 날짜 블록 찾기
    day_pattern = re.compile(r'^(\d{1,2})$')
    days = []
    
    for block in text_blocks:
        match = day_pattern.match(block["text"])
        if match:
            day = int(match.group(1))
            if 1 <= day <= 31:  # 유효한 날짜인지 확인
                days.append({
                    "day": day,
                    "center_x": block["center_x"],
                    "center_y": block["center_y"],
                    "min_y": block["min_y"],
                    "max_y": block["max_y"]
                })
    
    # 행 나누기 (날짜 기준)
    days_sorted_by_y = sorted(days, key=lambda x: x["center_y"])
    row_breaks = []
    
    if days_sorted_by_y:
        current_row_y = days_sorted_by_y[0]["center_y"]
        for i in range(1, len(days_sorted_by_y)):
            if days_sorted_by_y[i]["center_y"] - current_row_y > 50:  # 50픽셀 이상 차이면 새 행
                mid_y = (days_sorted_by_y[i]["center_y"] + current_row_y) / 2
                row_breaks.append(mid_y)
                current_row_y = days_sorted_by_y[i]["center_y"]
    
    # 날짜를 열에 할당
    for day in days:
        # 가장 가까운 열 찾기
        col_idx = min(range(len(column_centers)), 
                     key=lambda i: abs(day["center_x"] - column_centers[i]))
        day["col"] = col_idx
        
        # 행 번호 찾기
        row_idx = 0
        for break_y in row_breaks:
            if day["center_y"] > break_y:
                row_idx += 1
        day["row"] = row_idx
    
    # 로깅
    for day in days:
        logger.info(f"날짜 {day['day']}: 행 {day['row']}, 열 {day['col']}")
    
    # 시간+이름 패턴
    schedule_pattern = re.compile(r'(\d{1,2})\s*:\s*(\d{2})\s*[\~\-]\s*(\d{1,2})\s*:\s*(\d{2})\s+([가-힣]{2,})')
    
    # 일정 추출
    schedules = []
    
    # 일정 텍스트 블록 찾기
    for block in text_blocks:
        matches = schedule_pattern.finditer(block["text"])
        for match in matches:
            sh, sm, eh, em, name = match.groups()
            
            # 위치 기반으로 이 일정이 속한 날짜 찾기
            best_day = None
            min_distance = float('inf')
            
            for day in days:
                # 같은 열에 있고 날짜보다 아래에 있는지 확인
                if (day["col"] == find_column(block["center_x"], column_centers) and
                    block["min_y"] > day["max_y"]):
                    
                    # 다음 행의 날짜보다는 위에 있어야 함
                    next_row_days = [d for d in days if d["row"] == day["row"] + 1 and d["col"] == day["col"]]
                    if not next_row_days or block["max_y"] < min(d["min_y"] for d in next_row_days):
                        # 거리 계산 (y축 거리를 주요 기준으로)
                        x_dist = abs(block["center_x"] - day["center_x"])
                        y_dist = block["min_y"] - day["max_y"]
                        
                        # 거리 계산 (y축 거리에 가중치 부여)
                        distance = x_dist + y_dist * 0.5
                        
                        if distance < min_distance:
                            min_distance = distance
                            best_day = day
            
            # 날짜를 찾았으면 일정 추가
            if best_day and min_distance < 300:  # 거리 제한
                try:
                    if 0 <= int(sh) <= 23 and 0 <= int(eh) <= 23 and 0 <= int(sm) <= 59 and 0 <= int(em) <= 59:
                        schedule = {
                            "day": best_day["day"],
                            "startAt": f"{int(sh):02d}:{sm}",
                            "endAt": f"{int(eh):02d}:{em}",
                            "clientName": name.strip()
                        }
                        
                        # 중복 방지
                        if not any(s["day"] == schedule["day"] and 
                                  s["startAt"] == schedule["startAt"] and 
                                  s["endAt"] == schedule["endAt"] and 
                                  s["clientName"] == schedule["clientName"] 
                                  for s in schedules):
                            schedules.append(schedule)
                            logger.info(f"일정 추출: 날짜 {best_day['day']}일, {sh}:{sm}-{eh}:{em} {name}")
                except ValueError:
                    logger.warning(f"시간 변환 오류: {sh}:{sm}-{eh}:{em}")
                    continue
    
    return schedules

def find_column(x, column_centers):
    """x 좌표에 가장 가까운 열 인덱스 찾기"""
    return min(range(len(column_centers)), key=lambda i: abs(x - column_centers[i]))

def process_image(image_content, user_id):
    """이미지 처리 및 OCR 결과 반환"""
    try:
        # 텍스트 감지
        response = detect_table_from_image(image_content)
        
        # 연도와 월 추출
        year, month = extract_year_month(response.full_text_annotation.text)
        
        # 모든 텍스트 블록 추출
        text_blocks = []
        for page in response.full_text_annotation.pages:
            for block in page.blocks:
                # 텍스트 추출
                block_text = ""
                for paragraph in block.paragraphs:
                    for word in paragraph.words:
                        word_text = ''.join([symbol.text for symbol in word.symbols])
                        block_text += word_text + " "
                
                block_text = block_text.strip()
                if not block_text:
                    continue
                
                # 바운딩 박스 위치 계산
                vertices = block.bounding_box.vertices
                min_x = min(vertex.x for vertex in vertices)
                min_y = min(vertex.y for vertex in vertices)
                max_x = max(vertex.x for vertex in vertices)
                max_y = max(vertex.y for vertex in vertices)
                
                # 블록 정보 저장
                text_blocks.append({
                    "text": block_text,
                    "min_x": min_x,
                    "min_y": min_y,
                    "max_x": max_x,
                    "max_y": max_y,
                    "center_x": (min_x + max_x) / 2,
                    "center_y": (min_y + max_y) / 2
                })
        
        # 디버깅: 모든 텍스트 블록 로깅
        for i, block in enumerate(text_blocks):
            logger.info(f"블록 {i}: '{block['text']}' 위치: ({block['min_x']}, {block['min_y']}) ~ ({block['max_x']}, {block['max_y']})")
        
        # 위치 기반 일정 추출 - 더 정확한 열 인식
        schedules = extract_schedules_position_based(text_blocks, year, month)
        
        # 최종 형식으로 변환
        formatted_schedules = []
        for item in schedules:
            formatted_schedules.append({
                "date": f"{year}-{month:02d}-{item['day']:02d}",
                "startAt": item["startAt"],
                "endAt": item["endAt"],
                "clientName": item["clientName"]
            })
        
        # 응답 생성
        response_data = {
            "userId": user_id,
            "year": year,
            "month": month,
            "schedules": formatted_schedules
        }
        
        logger.info(f"Final response: {response_data}")
        return response_data
        
    except Exception as e:
        logger.error(f"Error processing image: {str(e)}", exc_info=True)
        raise