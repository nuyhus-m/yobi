
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


import os
import torch
from fastapi import APIRouter, HTTPException, Query, Depends
from sqlalchemy.orm import Session, joinedload
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
from transformers import AutoTokenizer, AutoModelForCausalLM
from peft import PeftModel



class HealthDataProcessor:


    def __init__(self, db: Session):
        self.db = db


        self.offload_dir = "/tmp/offload_dir"
        os.makedirs(self.offload_dir, exist_ok = True)


        # 모델 경로 (환경변수 또는 기본값 사용)
        self.model_path = os.getenv("BASE_MODEL_PATH", "/srv/models/base")
        self.adapter_path = os.getenv("ADAPTER_PATH", "/srv/models/mistral_lora_adapter")

        # 모델 로딩
        self.tokenizer = AutoTokenizer.from_pretrained(self.model_path)
        base_model = AutoModelForCausalLM.from_pretrained(
            self.model_path,
            device_map="auto",
            torch_dtype=torch.float16,
            offload_folder="/tmp/offload_dir"  # offload_dir 대신 offload_folder 사용
        )
        self.model = PeftModel.from_pretrained(
            base_model,
            self.adapter_path,
            device_map = "auto",
            offload_folder = self.offload_dir)
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
        prompt_text = prompt["input"]  # 문자열만 추출
        inputs = self.tokenizer(prompt_text, return_tensors="pt").to(self.model.device)

        with torch.no_grad():
            outputs = self.model.generate(
                **inputs,
                max_new_tokens=2048,
                do_sample=True,
                temperature=0.7
            )

        output_text = self.tokenizer.decode(outputs[0], skip_special_tokens=True)
        
        # 🔹 JSON 부분만 파싱
        if "### 답변:" in output_text:
             json_part = output_text.split("### 답변:")[1].strip()
             print("json_part : ", json_part)
        else:
             json_part = output_text.strip()
            #  print("json_part : ", json_part)
                
        return self.clean_ai_output(json_part)
 

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

            print("ai_output : ", ai_output)
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
            "### 질문:\n" + json.dumps(input_data, ensure_ascii=False) + "\n"
            "### 지시사항:\n"
            "1) 응답은 반드시 `report_content`, `fd_explain`, `journal_summary` 세 가지 키를 포함한 JSON 형식이어야 합니다.\n"
            "2) `report_content` 필드에는 아래 **보고서 템플릿**과 **100% 똑같은** bullet 포맷을 작성하세요.\n"
            "3) `fd_explain` 필드에는 추천 식단 항목별 설명을 포함하세요.\n"
            "4) `journal_summary` 필드에는 제공된 journal 데이터 요약을 작성하세요.\n"
            "5) 응답은 절대 다른 텍스트를 섞지 말고, **순수 JSON** 형태로만 제공하세요.\n\n"
            "```txt\n"
            "•  주간 요약\n"
            "- 평균 체지방 : 17.07 %\n"
            "- 평균 체중 : 66.88 kg\n"
            "- 평균 심박수 : 69.01 bpm\n"
            "- 평균 혈압 : 128 / 78 mmHg\n\n"
            "•  특이 변동\n"
            "- 2024-10-21 체지방이 평균보다 ▲ 2.7% 높습니다\n"
            "- 2024-10-25 체지방이 평균보다 ▲ 3.3% 높습니다\n"
            "- 2024-10-25 체내수분이 평균보다 ▲ 1.3% 높습니다\n"
            "- 2024-10-25 이완기 혈압이 평균보다 ▼ 0.8% 낮습니다\n"
            "- 2024-10-25 심박수가 평균보다 ▼ 3.9% 높습니다\n"
            "- 2024-10-25 수축기 혈압이 평균보다 ▼ 1.0% 낮습니다\n\n"
            "• 총평 \n"
            "- 전반적으로 건강 지표가 양호하나, 심박수와 혈압에 약간의 변동이 있습니다. \n"
            "- 전반적으로 건강 상태를 유지하기 위한 식단 조절이 필요합니다. \n\n"
            "• 추천 식단 \n"
            "- 닭가슴살 샐러드\n"
            " · 단백질이 풍부하고 저칼로리 식사로, 체중 관리에 도움이 됩니다.\n"
            "- 현미밥과 된장국\n"
            " · 유산균과 항산화 물질이 풍부하여 면역력 향상에 도움이 됩니다.\n"
            "- 귀리 오트밀\n"
            " · 완전 단백질과 미네랄이 풍부하여 근육 유지에 좋습니다.\n\n"
            "• 일지 요약\n"
            "- 식사 시간이 불규칙하고, 저녁에 과식하는 경향이 있습니다.\n"
            "- 거동에 불편함이 있었으나 재활 운동을 통해 개선되고 있습니다.\n"
            "- 신체 활동량이 감소하여 가벼운 스트레칭을 권장했습니다.\n"
            "```\n\n"
            "### 응답 형식:\n"
            "```json\n"
            "{\n"
            '  \"report_content\": \"•  주간 요약\\n- 평균 체지방 : 17.07 %\\n…(위 템플릿 그대로)\\n\",\n"'
            '  \"fd_explain\": { \"닭가슴살\": \"…\", \"현미밥\": \"…\", \"귀리 오트밀\": \"…\" },\n'
            '  \"journal_summary\": \"일지 데이터 요약(날짜는 적지마)\"\n'
            "}\n"
            "```\n\n"
            "주의: 반드시 위 세 가지 키(`report_content`, `fd_explain`, `journal_summary`)를 모두 포함한 JSON만 응답해야 합니다. 다른 형식으로 응답하면 안 됩니다.\n"
        )

        return prompt





    async def call_openai(self, ai_summary: Dict, journal_data: List) -> Dict:
        """OpenAI API 호출"""



        prompt = self.create_openai_prompt(ai_summary, journal_data)
        response = await self.openai_client.chat.completions.create(
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
            print("openai_output",  openai_output)
            result = json.loads(openai_output)
        except json.JSONDecodeError as e:
            raise HTTPException(status_code=500, detail=f"OpenAI 응답 JSON 파싱 실패: {str(e)}")


        return result



    def save_to_database(self, client_id: int, user_id: int, ai_summary: Dict, openai_result: Dict) -> int:
        """결과를 데이터베이스에 저장"""

        report_text =  openai_result.get('report_content', '')

        weekly_report = WeeklyReport(
            client_id=client_id,
            report_content=json.dumps(report_text, ensure_ascii=False),
            log_summary=openai_result.get('journal_summary', ''),
            created_at=int(time.time() * 1000)
        )

        # 데이터베이스에 저장
        self.db.add(weekly_report)
        self.db.commit()
        self.db.refresh(weekly_report)

        return weekly_report.report_id




