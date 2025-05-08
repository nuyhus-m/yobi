import os
import json
import logging
import requests
from datetime import datetime
from dotenv import load_dotenv
import re

# 환경 변수 로드
load_dotenv()

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def analyze_image_with_clova_ocr(image_data):
    """
    네이버 클로바 OCR API를 사용하여 이미지에서 텍스트 감지
    
    Args:
        image_data (bytes): 이미지 바이너리 데이터
        
    Returns:
        dict: OCR 결과
    """
    api_url = "https://y2c2kflg03.apigw.ntruss.com/custom/v1/41749/5d217b95d9b5c264c3972dc752f7bf27a9755a818c42b6d58d6b50695f53dc20/general"
    secret_key = os.getenv("CLOVA_OCR_SECRET_KEY")
    
    if not secret_key:
        logger.error("CLOVA_OCR_SECRET_KEY 환경 변수가 설정되지 않았습니다.")
        raise ValueError("CLOVA_OCR_SECRET_KEY 환경 변수가 설정되지 않았습니다.")
    
    # API 요청 헤더
    headers = {
        "X-OCR-SECRET": secret_key
    }
    
    # API 요청 본문
    request_json = {
        "images": [
            {
                "format": "png",
                "name": "demo"
            }
        ],
        "requestId": str(datetime.now().timestamp()),
        "version": "V2",
        "timestamp": int(datetime.now().timestamp() * 1000),
        "lang": "ko",
        "enableTableDetection": True  # 표 분석 활성화
    }
    
    # multipart/form-data 요청 생성
    files = {
        'message': (None, json.dumps(request_json), 'application/json'),
        'file': ('image.png', image_data, 'image/png')
    }
    
    # API 요청
    try:
        logger.info("클로바 OCR API 요청 시작")
        response = requests.post(api_url, headers=headers, files=files)
        
        # 응답 로깅
        logger.info(f"OCR API 응답 상태 코드: {response.status_code}")
        
        if response.status_code != 200:
            logger.error(f"OCR API 오류 응답: {response.text}")
            return None
            
        # 응답 결과
        result = response.json()
        
        # 응답 구조 로깅
        logger.info(f"응답 키: {list(result.keys())}")
        if "images" in result and len(result["images"]) > 0:
            image_result = result["images"][0]
            logger.info(f"이미지 결과 키: {list(image_result.keys())}")
            
            # 필드 정보 로깅
            if "fields" in image_result:
                fields = image_result.get("fields", [])
                logger.info(f"감지된 텍스트 필드 수: {len(fields)}")
            
            # 테이블 정보 로깅
            if "tables" in image_result:
                tables = image_result.get("tables", [])
                logger.info(f"감지된 테이블 수: {len(tables)}")
            else:
                logger.info("감지된 테이블 없음")
        
        return result
    except Exception as e:
        logger.error(f"CLOVA OCR API 요청 오류: {str(e)}", exc_info=True)
        return None

def process_image(image_data):
    """
    이미지 처리 메인 함수 - FastAPI에서 호출
    
    Args:
        image_data (bytes): 이미지 바이너리 데이터
        
    Returns:
        dict: 처리 결과
    """
    try:
        # CLOVA OCR로 이미지 분석
        ocr_result = analyze_image_with_clova_ocr(image_data)
        
        if not ocr_result:
            logger.error("OCR 분석 실패")
            return None
            
        # OCR 결과 로깅
        logger.info("=== OCR 분석 결과 ===")
        
        # 현재 날짜 정보
        current_date = datetime.now()
        current_year = current_date.year
        current_month = current_date.month
        
        # 셀 정보와 스케줄을 저장할 리스트
        cells = []
        schedules = []
        
        # 표 분석 결과 출력 및 처리
        if "images" in ocr_result and len(ocr_result["images"]) > 0:
            image_result = ocr_result["images"][0]
            
            # 표 정보가 있는 경우
            if "tables" in image_result:
                tables = image_result["tables"]
                logger.info(f"감지된 표 수: {len(tables)}")
                
                for table_idx, table in enumerate(tables):
                    logger.info(f"\n=== 표 {table_idx + 1} ===")
                    
                    for cell in table.get("cells", []):
                        try:
                            # 셀의 위치 정보
                            row_idx = cell.get("rowIndex", -1)
                            col_idx = cell.get("columnIndex", -1)
                            
                            # 셀의 텍스트 정보
                            cell_text = ""
                            for text_line in cell.get("cellTextLines", []):
                                for word in text_line.get("cellWords", []):
                                    cell_text += word.get("inferText", "") + " "
                            cell_text = cell_text.strip()
                            
                            if cell_text:
                                logger.info(f"위치 [{row_idx}, {col_idx}]: {cell_text}")
                                
                                # 첫 번째 행은 요일 정보이므로 건너뛰기
                                if row_idx == 0:
                                    continue
                                
                                # 셀 정보 저장
                                cell_info = {
                                    "row": row_idx,
                                    "col": col_idx,
                                    "content": cell_text,
                                    "lines": cell_text.split()
                                }
                                cells.append(cell_info)
                                
                                # 스케줄 정보 추출
                                lines = cell_info["lines"]
                                if len(lines) > 0:
                                    try:
                                        # 첫 번째 줄이 날짜인지 확인
                                        day = int(lines[0])
                                        logger.info(f"날짜 처리: {day}")
                                        
                                        # 나머지 줄에서 스케줄 정보 추출
                                        i = 1
                                        while i < len(lines):
                                            line = lines[i]
                                            logger.info(f"라인 처리: {line}")
                                            
                                            # 시간 정보가 있는 경우
                                            if "~" in line:
                                                # 시간과 이름이 붙어있는 경우 분리
                                                if not line.endswith("~"):
                                                    time_parts = line.split("~")
                                                    if len(time_parts) == 2:
                                                        start_time = time_parts[0].strip()
                                                        raw_end = time_parts[1].strip()
                                                        
                                                        # 시간과 이름 분리
                                                        time_match = re.search(r'(\d{1,2}:\d{2})(.*)', raw_end)
                                                        if time_match:
                                                            end_time = time_match.group(1)
                                                            name = time_match.group(2).strip()
                                                            logger.info(f"시간과 이름 분리: {end_time}, {name}")
                                                        else:
                                                            end_time = raw_end
                                                            name = ""
                                                        
                                                        # 다음 줄이 있고 이름이 없는 경우
                                                        if not name and i + 1 < len(lines):
                                                            name = lines[i + 1]
                                                            i += 1
                                                        
                                                        schedule = {
                                                            "day": day,
                                                            "startAt": start_time,
                                                            "endAt": end_time,
                                                            "clientName": name
                                                        }
                                                        logger.info(f"스케줄 추가: {schedule}")
                                                        schedules.append(schedule)
                                                        i += 1  # 현재 라인 처리 완료 후 다음으로 이동
                                                    else:
                                                        i += 1
                                                else:
                                                    i += 1
                                            else:
                                                i += 1
                                    except ValueError as ve:
                                        logger.error(f"날짜 변환 오류: {str(ve)}")
                                        continue
                                    except Exception as e:
                                        logger.error(f"스케줄 처리 중 오류: {str(e)}")
                                        continue
                        except Exception as e:
                            logger.error(f"셀 처리 중 오류: {str(e)}")
                            continue
            
            # 일반 텍스트 필드도 함께 출력
            fields = image_result.get("fields", [])
            logger.info(f"\n=== 일반 텍스트 필드 ({len(fields)}개) ===")
            for field in fields:
                text = field.get("inferText", "")
                if text:
                    logger.info(f"감지된 텍스트: {text}")
        
        # 결과 응답 생성
        response = {
            "schedules": schedules
        }

        
        return response
        
    except Exception as e:
        logger.error(f"이미지 처리 오류: {str(e)}", exc_info=True)
        return None

# 테스트 코드 (직접 실행 시)
if __name__ == "__main__":
    # 이미지 파일 경로
    image_path = "calendar_image.png"
    
    # 이미지 파일 읽기
    with open(image_path, "rb") as f:
        image_data = f.read()
    
    # 이미지 처리
    result = process_image(image_data)
    
    if result and result["success"]:
        print("OCR 처리 완료!")
        print(f"추출된 텍스트 수: {len(result['extractedTexts'])}")
        
        # 처음 5개 텍스트 출력
        for i, text in enumerate(result["extractedTexts"][:5]):
            print(f"텍스트 {i+1}: {text}")
    else:
        print("OCR 처리 실패")