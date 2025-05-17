# from fastapi import APIRouter, HTTPException, Query, Depends
# from sqlalchemy.orm import Session, joinedload
# from core.database import get_db
# from models import Measure, Client, User, BodyComposition, BloodPressure, HeartRate, Schedule, WeeklyReport
# from typing import Dict, List, Optional
# from datetime import datetime, timedelta
# from openai import OpenAI
# import json
# import httpx
# import time
# from core.config import settings
# from schemas.health_data import HealthDataResponse
# import aiohttp  # 배치모드에서 세션 재사용용
# from openai import AsyncOpenAI  # 비동기 클라이언트

# router = APIRouter(
#     prefix="/health-data",
#     tags=["health-data"]
# )

# class HealthDataProcessor:
#     def __init__(self, db: Session):
#         self.db = db

#         # 비동기 OpenAI 클라이언트
#         self.openai_client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)
#         self.internal_ai_url = settings.INTERNAL_AI_URL

#          # 배치 작업용 설정
#         self.batch_mode = False
#         self.aiohttp_session = None

#     def set_batch_mode(self, enabled: bool, aiohttp_session: Optional[aiohttp.ClientSession] = None):
#         """배치 모드 설정"""
#         self.batch_mode = enabled
#         self.aiohttp_session = aiohttp_session

#     async def collect_health_data(self, client_id: int, user_id: int) -> Dict:
#         """건강 측정 데이터 수집"""
#         today = datetime.now()
#         start = today - timedelta(days=6)  # days 오타 수정

#         # 날짜 범위를 Epoch(ms)로 변환
#         start_epoch = int(start.replace(hour=0, minute=0, second=0, microsecond=0).timestamp() * 1000)
#         end_epoch = int((today - timedelta(days=1)).replace(hour=23, minute=59, second=59, microsecond=999000).timestamp() * 1000)

#         # 조회: 월~금 범위 내 측정
#         measures = (
#             self.db.query(Measure)
#             .options(
#                 joinedload(Measure.body_composition),
#                 joinedload(Measure.blood_pressure),
#                 joinedload(Measure.heart_rate),
#                 joinedload(Measure.client)
#             )
#             .filter(
#                 Measure.user_id == user_id,
#                 Measure.client_id == client_id,
#                 Measure.date >= start_epoch,
#                 Measure.date <= end_epoch
#             )
#             .order_by(Measure.date.asc())
#             .all()
#         )

#         # 데이터 포맷 변환
#         dm_list = []
#         for measure in measures:
#             bc = measure.body_composition
#             bp = measure.blood_pressure
#             hr = measure.heart_rate
#             client = measure.client

#             dm_item = {
#                 # 날짜
#                 "d": datetime.fromtimestamp(measure.date/1000).date().isoformat(),
                
#                 # body_composition 데이터 (None 체크)
#                 "bf": bc.bfp if bc else None,
#                 "mm": bc.smm if bc else None,
#                 "bw": bc.bfm if bc else None,  # 쉼표 누락 수정
#                 "prot": bc.protein if bc else None,
#                 "min": bc.mineral if bc else None,

#                 # client 데이터 (속성 접근 방식 수정)
#                 "wt": client.weight if client else None,
#                 "ht": client.height if client else None,

#                 # heart_rate 데이터
#                 "hr": hr.bpm if hr else None,
#                 "o2": hr.oxygen if hr else None,

#                 # blood_pressure 데이터 (None 체크 추가)
#                 "sys": bp.sbp if bp else None,
#                 "dia": bp.dbp if bp else None,
#             }
#             dm_list.append(dm_item)

#         return {
#             "ws": start.date().isoformat(),
#             "we": (today - timedelta(days=1)).date().isoformat(),  # 메소드 호출 수정
#             "dm": dm_list
#         }

#     async def call_internal_ai(self, health_data: Dict) -> Dict:
#         """내부 AI 서비스 호출"""
#         input_payload = {
#             "input": (
#                 "### 질문:\n" + json.dumps(health_data, ensure_ascii=False) + 
#                 "\n### 지시사항:\n1) 주간 평균·최소·최댓값을 **wsum** 필드에 작성하고\n"
#                 "2) 평균 대비 ±2 % 이상 벗어난 값을 **anom** 배열에\n"
#                 "   { \"d\": 날짜, \"m\": 지표, \"v\": 값, \"pct\": ±변동률 } 형식으로 기록하며\n"
#                 "3) **cmt.g** 에서\n"
#                 "   - 주간 총평 한 문단 작성\n"
#                 "   - 특이사항 날짜·지표·±% 요약\n"
#                 "4) 변동 지표에 맞춰 3가지 **fd**(식단) 추천:\n"
#                 "   · 체중·체지방 ↑ ⇒ 저지방 메뉴\n"
#                 "   · 혈압 ↑ ⇒ 저염 메뉴\n"
#                 "(그 외 필드는 작성하지 마세요)\n### 답변:\n"
#             )
#         }
        
#          # 배치 모드에서는 세션 재사용
#         if self.batch_mode and self.aiohttp_session:
#             async with self.aiohttp_session.post(
#                 self.internal_ai_url,
#                 json=input_payload,
#                 timeout=aiohttp.ClientTimeout(total=30)
#             ) as response:
#                 response.raise_for_status()
#                 return await response.json()
#         else:
#             # 단일 요청 모드
#             async with httpx.AsyncClient() as client:
#                 response = await client.post(
#                     self.internal_ai_url,
#                     json=input_payload,
#                     timeout=30.0
#                 )
#                 response.raise_for_status()
#                 return response.json()
        
#     def clean_ai_output(self, ai_output: str) -> Dict:
#         """AI output 정제"""
#         try:
#             # 문자열이 아닌 dict인 경우 처리
#             if isinstance(ai_output, dict):
#                 return ai_output
                
#             # JSON 문자열 파싱
#             if ai_output.startswith('"') and ai_output.endswith('"'):
#                 # JSON 문자열이 따옴표로 둘러싸인 경우
#                 ai_output = ai_output[1:-1]
#                 ai_output = ai_output.replace('\\"', '"')
            
#             return json.loads(ai_output)
            
#         except json.JSONDecodeError as e:
#             raise HTTPException(status_code=500, detail=f"AI output 파싱 실패: {str(e)}")
    
#     async def get_journal_data(self, client_id: int, user_id: int) -> List[Dict]:
#         """일지 데이터 조회"""
#         today = datetime.now()
#         start = today - timedelta(days=6)  # days 오타 수정

#         # 날짜 범위를 Epoch(ms)로 변환
#         start_epoch = int(start.replace(hour=0, minute=0, second=0, microsecond=0).timestamp() * 1000)
#         end_epoch = int((today - timedelta(days=1)).replace(hour=23, minute=59, second=59, microsecond=999000).timestamp() * 1000)

#         schedules = (
#             self.db.query(Schedule)
#             .filter(
#                 Schedule.user_id == user_id,  
#                 Schedule.client_id == client_id,  
#                 Schedule.visited_date >= start_epoch,
#                 Schedule.visited_date <= end_epoch,
#                 Schedule.log_content.isnot(None)  # 일지가 있는 것만
#             )
#             .order_by(Schedule.visited_date.asc())
#             .all()
#         )

#         return [
#             {
#                 "date": datetime.fromtimestamp(schedule.visited_date / 1000).date().isoformat(),
#                 "start_time": datetime.fromtimestamp(schedule.start_at / 1000).strftime("%H:%M") if schedule.start_at else None,
#                 "end_time": datetime.fromtimestamp(schedule.end_at / 1000).strftime("%H:%M") if schedule.end_at else None,
#                 "log_content": schedule.log_content or ""
#             }
#             for schedule in schedules
#         ]
    
#     def create_openai_prompt(self, ai_summary: Dict, journal_data: List) -> str:
#         """OpenAI 프롬프트 생성"""
#         # AI 요약 데이터 포맷팅
#         wsum = ai_summary.get("wsum", {})
#         anomalies = ai_summary.get("anom", [])
#         ai_comment = ai_summary.get("cmt", {}).get("g", "")
#         food_suggestions = ai_summary.get("fd", [])  

#         # 돌봄 일지 데이터 포맷팅
#         journal_summary = ""
#         if journal_data:
#             journal_entries = []
#             for entry in journal_data[:5]:
#                 date = entry.get('date', '')
#                 content = entry.get('log_content', '')
#                 journal_entries.append(f"{date}: {content}")
#             journal_summary = " | ".join(journal_entries)

#         input_data = {
#         "wsum": wsum,
#         "anom": anomalies,
#         "ai_comment": ai_comment,
#         "food": food_suggestions,
#         "journal": journal_summary
#         }   
        
#         prompt = (
#         "### 질문:\n" + json.dumps(input_data, ensure_ascii=False) + 
#         "\n### 지시사항:\n1) **comment**에 건강상태 평가 작성 (500자 이하)\n"
#         "2) **fd_explain**에 추천음식별 이유 작성 (dict 형태)\n"
#         "3) **summary**에 돌봄일지 요약 작성 (300자 이하)\n"
#         "(그 외 필드는 작성하지 마세요)\n### 답변:\n"
#     )

#     async def call_openai(self, ai_summary: Dict, journal_data: List) -> Dict:
#         """OpenAI API 호출"""
#         prompt = self.create_openai_prompt(ai_summary, journal_data)
            
#         response = self.openai_client.chat.completions.create(**prompt)
#         openai_output = response.choices[0].message.content.strip()

#         # JSON 앞뒤 불필요한 텍스트 제거
#         if "```json" in openai_output:
#             openai_output = openai_output.split("```json")[1].split("```")[0].strip()
#         elif "```" in openai_output:
#             openai_output = openai_output.split("```")[1].strip()
        
#         # JSON 파싱 시도
#         try:
#             result = json.loads(openai_output)
#         except json.JSONDecodeError as e:
#             raise HTTPException(status_code=500, detail=f"OpenAI 응답 JSON 파싱 실패: {str(e)}")

#         # 필수 필드 검증
#         required_fields = ["comment", "fd_explain", "summary"]
#         missing_fields = [f for f in required_fields if f not in result]
#         if missing_fields:
#             raise ValueError(f"필수 필드 누락: {missing_fields}")
            
#         return result
        
#     def save_to_database(self, client_id: int, user_id: int, ai_summary: Dict, openai_result: Dict) -> int:
#         """결과를 데이터베이스에 저장"""
#         # OpenAI 결과에서 필요한 데이터 추출
#         # report_content = self._prepare_report_content(openai_result)
#         # log_summary = openai_result.get('summary', '')

#         # report_content 생성 (JSON 형태로 저장)
#         report_content = {
#             "ai_analysis": ai_summary,  # 1차 AI 결과 전체
#             "final_comment": openai_result.get('comment', ''),  # OpenAI 최종 코멘트
#             "food_explain": openai_result.get('fd_explain', {}),  # 식단 설명
#             "created_at": int(time.time() * 1000),
#             "version": "1.0"  # 스키마 버전
#         }

#         # 새로운 weekly_report 객체 생성
#         weekly_report = WeeklyReport(
#             client_id=client_id,
#             report_content=json.dumps(report_content, ensure_ascii=False),
#             log_summary=openai_result.get('summary', ''),
#             created_at=int(time.time() * 1000)  # milliseconds로 변환 / bigint로 저장
#         )

#         # 데이터베이스에 저장
#         self.db.add(weekly_report)
#         self.db.commit()
#         self.db.refresh(weekly_report)
        
#         return weekly_report.report_id

#     # def _prepare_report_content(self, openai_result: Dict) -> str:
#     #     """OpenAI 결과를 Text 형식으로 변환"""
#     #     report_parts = []
        
#     #     # 코멘트 추가
#     #     if comment := openai_result.get('comment'):
#     #         report_parts.append(f"## 주간 건강 상태 종합 평가\n{comment}\n")
        
#     #     # 식단 설명 추가
#     #     if fd_explain := openai_result.get('fd_explain'):
#     #         report_parts.append("## 추천 식단 설명")
#     #         for food, reason in fd_explain.items():
#     #             report_parts.append(f"- **{food}**: {reason}")
#     #         report_parts.append("")
        
#     #     # 요약 추가
#     #     if summary := openai_result.get('summary'):
#     #         report_parts.append(f"## 주간 돌봄 상황 요약\n{summary}")
        
#     #     return "\n".join(report_parts)


# @router.post("/ai/report/{client_id}/complete", response_model=HealthDataResponse)
# async def generate_complete_health_report(
#     client_id: int,
#     user_id: int = Query(..., description="사용자 ID"),
#     include_journal: bool = Query(True, description="일지 포함 여부"),
#     db: Session = Depends(get_db)
# ):
#     """
#     건강 데이터 수집부터 최종 리포트까지 한 번에 처리
    
#     1. 건강 측정 데이터 수집
#     2. 내부 AI 서비스 호출
#     3. 일지 데이터 조회
#     4. OpenAI 최종 분석
#     5. 결과 DB 저장
#     """
    
#     try: 
#         processor = HealthDataProcessor(db)

#         # 1. 건강 데이터 수집
#         health_data = await processor.collect_health_data(client_id, user_id)
        
#         # 2. AI 호출
#         internal_ai_response = await processor.call_internal_ai(health_data)
        
#         # AI output 정제
#         ai_summary = processor.clean_ai_output(internal_ai_response.get("output", ""))
        
#         # 3. 일지 데이터 조회
#         journal_data = []
#         if include_journal:
#             journal_data = await processor.get_journal_data(client_id, user_id)
        
#         # 4. OpenAI 최종 분석
#         openai_result = await processor.call_openai(ai_summary, journal_data)

#         # 5. 데이터베이스 저장
#         report_id = processor.save_to_database(client_id, user_id, ai_summary, openai_result)

#         return HealthDataResponse(
#             success=True,
#             report_id=report_id,
#             comment=openai_result.get("comment", ""),
#             fd_explain=openai_result.get("fd_explain", {}),
#             summary=openai_result.get("summary", ""),
#             metadata={
#                 "health_records": len(health_data.get("dm", [])),
#                 "journal_entries": len(journal_data),
#                 "anomalies_found": len(ai_summary.get("anom", [])),
#                 "food_suggestions": ai_summary.get("fd", [])
#             }
#         )
        
#     except httpx.HTTPError as e:
#         raise HTTPException(status_code=503, detail=f"AI 서비스 호출 실패: {str(e)}")
#     except json.JSONDecodeError as e:
#         raise HTTPException(status_code=500, detail=f"JSON 파싱 실패: {str(e)}")
#     except Exception as e:
#         db.rollback()
#         raise HTTPException(status_code=500, detail=f"리포트 생성 실패: {str(e)}")




# @router.get("/ai/report/{clientId}", response_model=HealthDataResponse) 
# def send_weekly_health(clientId: int, user_id: int, db: Session = Depends(get_db)):
#     today = datetime.now()
#     start = today - timedelta(days=6)

#     # 날짜 범위를 Epoch(ms)로 변환
#     start_epoch = int(start.replace(hour=0, minute=0, second=0, microsecond=0).timestamp() * 1000)
#     end_epoch = int((today - timedelta(days=1)).replace(hour=23, minute=59, second=59, microsecond=999000).timestamp() * 1000)

#     # 조회: 월~금 범위 내 측정값
#     measures = (
#         db.query(Measure)
#         .filter(
#             Measure.user_id == user_id,
#             Measure.client_id == clientId,
#             Measure.date >= start_epoch,
#             Measure.date <=end_epoch
#         )
#         .order_by(Measure.date.asc())
#         .all()
#     )

#     # 데이터 포맷 변환
#     dm_list =[]
#     for m in measures:
#         bc = m.body_composition
#         bp = m.blood_pressure
#         hr = m.heart_rate
#         client = m.client
    
#         dm_list.append({
#             "bf": bc.bfp,
#             "mm": bc.smm,
#             "bw": bc.bfm,
#             "prot": bc.protein,
#             "min": bc.mineral,
#             "wt": client.weight,
#             "ht": client.height,  
#             "hr": hr.bpm,  
#             "o2": hr.oxygen,  
#             "sys": bp.sbp if bc else None, 
#             "dia": bp.dbp if bc else None,
#             "d": datetime.fromtimestamp(m.date / 1000).date().isoformat()
#         })
    


#     # 프롬프트 생성
#     input_payload = {
#         "input": f"### 질문:\n{json.dumps({'ws': start.date().isoformat(), 'we': (today - timedelta(days=1)).date().isoformat(), 'dm': dm_list}, ensure_ascii=False)}\n"
#                  "### 지시사항:\nwsum, anom, cmt.g(평균 대비 %), fd 생성\n### 답변:\n"
#     }

#     return input_payload



# # # 1차
# # # 2차 일지 조회 + 1차 아웃 풋 -> 오픈 API 









# # @router.get("/", response_model=HealthDataResponse)
# # async def get_health_data(
# #     user_id: str,
# #     page: int = Query(1, ge=1),
# #     size: int = Query(10, ge=1, le=100)
# # ):
# #     """
# #     사용자의 건강 데이터를 조회합니다.
# #     DB에서 데이터를 가져와 AI 학습을 위한 형태로 반환합니다.
# #     """
# #     # TODO: Implement database query
# #     return {
# #         "data": [],
# #         "total_records": 0
# #     }

# # @router.get("/{data_id}", response_model=HealthDataBase)
# # async def get_health_data_by_id(data_id: int):
# #     """
# #     특정 건강 데이터를 조회
# #     """
# #     # TODO: Implement database query
# #     raise HTTPException(status_code=404, detail="Health data not found")

# # # CRUD 예시 코드
# # @router.post("/")
# # async def create_health_data(data: HealthDataBase):
# #     """새로운 건강 데이터를 생성합니다."""
# #     # TODO: Implement database storage
# #     return {"id": 1, **data.dict()}

# # @router.put("/{data_id}")
# # async def update_health_data(data_id: int, data: HealthDataBase):
# #     """건강 데이터를 수정합니다."""
# #     # TODO: Implement database update
# #     raise HTTPException(status_code=404, detail="Health data not found")

# # @router.delete("/{data_id}")
# # async def delete_health_data(data_id: int):
# #     """건강 데이터를 삭제합니다."""
# #     # TODO: Implement database delete
# #     raise HTTPException(status_code=404, detail="Health data not found") 