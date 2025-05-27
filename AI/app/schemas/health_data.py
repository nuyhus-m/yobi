from pydantic import BaseModel, Field
from typing import List, Dict, Any
from datetime import datetime
from sqlalchemy.orm import Session
from typing import Dict, List, Optional
from datetime import datetime, timedelta
from openai import OpenAI
from pydantic import BaseModel


# Request/Response 모델들
class OpenAIResponse(BaseModel):
    comment: str
    fd_explain: Dict[str, str]

class CompleteReportResponse(BaseModel):
    success: bool
    report_id: int
    openai_response: OpenAIResponse
    processing_time: float
    metadata: Dict[str, Any]

# 추가 스키마들
class HealthDataRequest(BaseModel):
    """건강 데이터 요청 스키마"""
    include_journal: bool = True
    days: int = 7

class JournalEntry(BaseModel):
    """일지 항목 스키마"""
    date: str
    content: str
    mood: Optional[str] = None
    exercise: Optional[str] = None

class HealthMetric(BaseModel):
    """건강 지표 스키마"""
    bf: Optional[float] = None  # 체지방률
    mm: Optional[float] = None  # 골격근량
    bw: Optional[float] = None  # 체지방량
    prot: Optional[float] = None  # 단백질
    min: Optional[float] = None  # 무기질
    wt: Optional[float] = None  # 체중
    ht: Optional[float] = None  # 신장
    hr: Optional[int] = None  # 심박수
    o2: Optional[int] = None  # 산소포화도
    sys: Optional[int] = None  # 수축기혈압
    dia: Optional[int] = None  # 이완기혈압
    d: str  # 날짜


class HealthReportResponse(BaseModel):
    success: bool
    report_id: int
    comment: str
    fd_explain: Dict[str, str]
    summary: str
    metadata: Dict[str, Any]




class HealthDataBase(BaseModel):
    """
    건강 데이터의 기본 모델 편의에 따라서 수정하여 사용하면 됩니다.
    DB에서 불러온 건강 데이터와 AI 학습을 위한 데이터 구조를 정의
    """
    user_id: str
    timestamp: datetime = Field(default_factory=datetime.now)
    health_metrics: Dict[str, Any]
    ai_training_data: Dict[str, Any]

class HealthDataResponse(BaseModel):
    """
    건강 데이터 조회 결과를 반환합니다.
    """
    data: List[HealthDataBase]
    total_records: int 