from pydantic import BaseModel, Field
from typing import List, Dict, Any
from datetime import datetime

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