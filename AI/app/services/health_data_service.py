# serviace/health_data_service.py
"""
건강 데이터 처리 및 분석 서비스 모듈

이 모듈은 다음과 같은 주요 기능을 제공합니다:
- 건강 데이터 수집
- AI를 활용한 건강 데이터 분석
- 내부/외부 AI 서비스 호출
- 건강 리포트 생성 및 저장

주요 클래스:
- HealthDataProcessor: 건강 데이터 처리 및 분석을 담당하는 핵심 프로세서
"""

from fastapi import APIRouter, HTTPException, Query, Depends
from sqlalchemy.orm import Session, joinedload
from core.database import get_db
from models import Measure, Client, User, BodyComposition, BloodPressure, HeartRate, Schedule, WeeklyReport
from typing import Dict, List, Optional
from datetime import datetime, timedelta
from openai import OpenAI
import json
import httpx
import time
from core.config import settings
from schemas.health_data import HealthDataResponse
import aiohttp  # 배치모드에서 세션 재사용용
from openai import AsyncOpenAI  # 비동기 클라이언트




class HealthDataProcessor:


    def __init__(self, db: Session):
        self.db = db


        # 모델 경로 (환경변수 또는 기본값 사용)
        self.model_path = os.getenv("BASE_MODEL_PATH", "/srv/models/base")
        self.adapter_path = os.getenv("ADAPTER_PATH", "/srv/models/mistral_lora_adapter")

        # 모델 로딩
        self.tokenizer = AutoTokenizer.from_pretrained(self.model_path)
        base_model = AutoModelForCausalLM.from_pretrained(
            self.model_path,
            device_map="auto",
            torch_dtype=torch.float16  # 또는 bfloat16, 환경에 따라 조절
        )
        self.model = PeftModel.from_pretrained(base_model, self.adapter_path)
        self.model.eval()  # 평가 모드

        # 비동기 OpenAI 클라이언트
        self.openai_client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)

         # 배치 작업용 설정
        self.batch_mode = False
        self.aiohttp_session = None



    def set_batch_mode(self, enabled: bool, aiohttp_session: Optional[aiohttp.ClientSession] = None):
        """배치 모드 설정"""
        self.batch_mode = enabled
        self.aiohttp_session = aiohttp_session



    async def collect_health_data(self, client_id: int, user_id: int) -> Dict:
        """건강 측정 데이터 수집"""
        today = datetime.now()
        start = today - timedelta(days=6)  # days 오타 수정

        # 날짜 범위를 Epoch(ms)로 변환
        start_epoch = int(start.replace(hour=0, minute=0, second=0, microsecond=0).timestamp() * 1000)
        end_epoch = int((today - timedelta(days=1)).replace(hour=23, minute=59, second=59, microsecond=999000).timestamp() * 1000)

        # 조회: 월~금 범위 내 측정
        measures = (
            self.db.query(Measure)
            .options(
                joinedload(Measure.body_composition),
                joinedload(Measure.blood_pressure),
                joinedload(Measure.heart_rate),
                joinedload(Measure.client)
            )
            .filter(
                Measure.user_id == user_id,
                Measure.client_id == client_id,
                Measure.date >= start_epoch,
                Measure.date <= end_epoch
            )
            .order_by(Measure.date.asc())
            .all()
        )

        # 데이터 포맷 변환
        dm_list = []
        for measure in measures:
            bc = measure.body_composition
            bp = measure.blood_pressure
            hr = measure.heart_rate
            client = measure.client

            dm_item = {
                # 날짜
                "d": datetime.fromtimestamp(measure.date/1000).date().isoformat(),
                
                # body_composition 데이터 (None 체크)
                "bf": bc.bfp if bc else None,
                "mm": bc.smm if bc else None,
                "bw": bc.bfm if bc else None,  # 쉼표 누락 수정
                "prot": bc.protein if bc else None,
                "min": bc.mineral if bc else None,

                # client 데이터 (속성 접근 방식 수정)
                "wt": client.weight if client else None,
                "ht": client.height if client else None,

                # heart_rate 데이터
                "hr": hr.bpm if hr else None,
                "o2": hr.oxygen if hr else None,

                # blood_pressure 데이터 (None 체크 추가)
                "sys": bp.sbp if bp else None,
                "dia": bp.dbp if bp else None,
            }
            dm_list.append(dm_item)

        return {
            "ws": start.date().isoformat(),
            "we": (today - timedelta(days=1)).date().isoformat(),  # 메소드 호출 수정
            "dm": dm_list
        }
    


    

    async def call_internal_ai(self, health_data: Dict) -> Dict:
        """HuggingFace Mistral 모델(EC2에 저장 중)로 직접 추론"""

        prompt = {
            "input": (
                "### 질문:\n" + json.dumps(health_data, ensure_ascii=False) + 
                "\n### 지시사항:\n1) 주간 평균·최소·최댓값을 **wsum** 필드에 작성하고\n"
                "2) 평균 대비 ±2 % 이상 벗어난 값을 **anom** 배열에\n"
                "   { \"d\": 날짜, \"m\": 지표, \"v\": 값, \"pct\": ±변동률 } 형식으로 기록하며\n"
                "3) **cmt.g** 에서\n"
                "   - 주간 총평 한 문단 작성\n"
                "   - 특이사항 날짜·지표·±% 요약\n"
                "4) 변동 지표에 맞춰 3가지 **fd**(식단) 추천:\n"
                "   · 체중·체지방 ↑ ⇒ 저지방 메뉴\n"
                "   · 혈압 ↑ ⇒ 저염 메뉴\n"
                "(그 외 필드는 작성하지 마세요)\n### 답변:\n"
            )
        }

        # 🔹 Tokenize & Model Inference
        inputs = self.tokenizer(prompt, return_tensors="pt").to(self.model.device)

        with torch.no_grad():
            outputs = self.model.generate(
                **inputs,
                max_new_tokens=512,
                do_sample=False,
                temperature=0.7
            )

        output_text = self.tokenizer.decode(outputs[0], skip_special_tokens=True)

        # 🔹 JSON 부분만 파싱
        try:
            json_part = output_text.strip()
            return json.loads(json_part)
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"모델 추론 결과 파싱 실패: {str(e)}")
        
         # 배치 모드에서는 세션 재사용
        if self.batch_mode and self.aiohttp_session:
            async with self.aiohttp_session.post(
                self.internal_ai_url,
                json=input_payload,
                timeout=aiohttp.ClientTimeout(total=30)
            ) as response:
                response.raise_for_status()
                return await response.json()
        else:
            # 단일 요청 모드
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    self.internal_ai_url,
                    json=input_payload,
                    timeout=30.0
                )
                response.raise_for_status()
                return response.json()
        



    def clean_ai_output(self, ai_output: str) -> Dict:
        """AI output 정제"""
        try:
            # 문자열이 아닌 dict인 경우 처리
            if isinstance(ai_output, dict):
                return ai_output
                
            # JSON 문자열 파싱
            if ai_output.startswith('"') and ai_output.endswith('"'):
                # JSON 문자열이 따옴표로 둘러싸인 경우
                ai_output = ai_output[1:-1]
                ai_output = ai_output.replace('\\"', '"')
            
            return json.loads(ai_output)
            
        except json.JSONDecodeError as e:
            raise HTTPException(status_code=500, detail=f"AI output 파싱 실패: {str(e)}")
        


    
    async def get_journal_data(self, client_id: int, user_id: int) -> List[Dict]:
        """일지 데이터 조회"""
        today = datetime.now()
        start = today - timedelta(days=6)  # days 오타 수정

        # 날짜 범위를 Epoch(ms)로 변환
        start_epoch = int(start.replace(hour=0, minute=0, second=0, microsecond=0).timestamp() * 1000)
        end_epoch = int((today - timedelta(days=1)).replace(hour=23, minute=59, second=59, microsecond=999000).timestamp() * 1000)

        schedules = (
            self.db.query(Schedule)
            .filter(
                Schedule.user_id == user_id,  
                Schedule.client_id == client_id,  
                Schedule.visited_date >= start_epoch,
                Schedule.visited_date <= end_epoch,
                Schedule.log_content.isnot(None)  # 일지가 있는 것만
            )
            .order_by(Schedule.visited_date.asc())
            .all()
        )

        return [
            {
                "date": datetime.fromtimestamp(schedule.visited_date / 1000).date().isoformat(),
                "start_time": datetime.fromtimestamp(schedule.start_at / 1000).strftime("%H:%M") if schedule.start_at else None,
                "end_time": datetime.fromtimestamp(schedule.end_at / 1000).strftime("%H:%M") if schedule.end_at else None,
                "log_content": schedule.log_content or ""
            }
            for schedule in schedules
        ]
    




    def create_openai_prompt(self, ai_summary: Dict, journal_data: List) -> str:
        """OpenAI 프롬프트 생성"""
        # AI 요약 데이터 포맷팅
        wsum = ai_summary.get("wsum", {})
        anomalies = ai_summary.get("anom", [])
        ai_comment = ai_summary.get("cmt", {}).get("g", "")
        food_suggestions = ai_summary.get("fd", [])  

        # 돌봄 일지 데이터 포맷팅
        journal_summary = ""
        if journal_data:
            journal_entries = []
            for entry in journal_data[:5]:
                date = entry.get('date', '')
                content = entry.get('log_content', '')
                journal_entries.append(f"{date}: {content}")
            journal_summary = " | ".join(journal_entries)

        input_data = {
        "wsum": wsum,
        "anom": anomalies,
        "ai_comment": ai_comment,
        "food": food_suggestions,
        "journal": journal_summary
        }   
        
        prompt = (
        "### 질문:\n" + json.dumps(input_data, ensure_ascii=False) + 
        "\n### 지시사항:\n1) **ai_comment**에 건강상태 평가 작성 (500자 이하)\n"
        "2) **food**에 추천음식별 이유 작성 (dict 형태)\n"
        "3) **journal**에 돌봄일지 요약 작성 (300자 이하)\n"
        "(그 외 필드는 작성하지 마세요)\n### 답변:\n"
        )

        return prompt
        




    async def call_openai(self, ai_summary: Dict, journal_data: List) -> Dict:
        """OpenAI API 호출"""

        

        prompt = self.create_openai_prompt(ai_summary, journal_data)
        response = self.openai_client.chat.completions.create(
                                                                model="gpt-4o-mini",
                                                                messages=[{"role":"user","content":prompt}],
                                                                temperature=0.7 )
        openai_output = response.choices[0].message.content.strip()

        # JSON 앞뒤 불필요한 텍스트 제거
        if "```json" in openai_output:
            openai_output = openai_output.split("```json")[1].split("```")[0].strip()
        elif "```" in openai_output:
            openai_output = openai_output.split("```")[1].strip()
        
        # JSON 파싱 시도
        try:
            result = json.loads(openai_output)
        except json.JSONDecodeError as e:
            raise HTTPException(status_code=500, detail=f"OpenAI 응답 JSON 파싱 실패: {str(e)}")

        # 필수 필드 검증
        required_fields = ["comment", "fd_explain", "summary"]
        missing_fields = [f for f in required_fields if f not in result]
        if missing_fields:
            raise ValueError(f"필수 필드 누락: {missing_fields}")
            
        return result
    

        
    def save_to_database(self, client_id: int, user_id: int, ai_summary: Dict, openai_result: Dict) -> int:
        """결과를 데이터베이스에 저장"""
        # OpenAI 결과에서 필요한 데이터 추출
        # report_content = self._prepare_report_content(openai_result)
        # log_summary = openai_result.get('summary', ''
        # report_content 생성 (JSON 형태로 저장)
        report_content = {
            "ai_analysis": ai_summary,  # 1차 AI 결과 전체
            "final_comment": openai_result.get('comment', ''),  # OpenAI 최종 코멘트
            "food_explain": openai_result.get('fd_explain', {}),  # 식단 설명
            "created_at": int(time.time() * 1000),
            "version": "1.0"  # 스키마 버전
        }

        # 새로운 weekly_report 객체 생성
        weekly_report = WeeklyReport(
            client_id=client_id,
            report_content=json.dumps(report_content, ensure_ascii=False),
            log_summary=openai_result.get('summary', ''),
            created_at=int(time.time() * 1000)  # milliseconds로 변환 / bigint로 저장
        )

        # 데이터베이스에 저장
        self.db.add(weekly_report)
        self.db.commit()
        self.db.refresh(weekly_report)
        
        return weekly_report.report_id
    

    

    # def _prepare_report_content(self, openai_result: Dict) -> str:
    #     """OpenAI 결과를 Text 형식으로 변환"""
    #     report_parts = []
        
    #     # 코멘트 추가
    #     if comment := openai_result.get('comment'):
    #         report_parts.append(f"## 주간 건강 상태 종합 평가\n{comment}\n")
        
    #     # 식단 설명 추가
    #     if fd_explain := openai_result.get('fd_explain'):
    #         report_parts.append("## 추천 식단 설명")
    #         for food, reason in fd_explain.items():
    #             report_parts.append(f"- **{food}**: {reason}")
    #         report_parts.append("")
        
    #     # 요약 추가
    #     if summary := openai_result.get('summary'):
    #         report_parts.append(f"## 주간 돌봄 상황 요약\n{summary}")
        
    #     return "\n".join(report_parts)
