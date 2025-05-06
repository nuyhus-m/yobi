from typing import List, Optional
from pydantic import BaseModel

class ScheduleItem(BaseModel):
    date: str  # YYYY-MM-DD 형식
    startAt: str  # HH:MM 형식
    endAt: str  # HH:MM 형식
    clientName: str  # 클라이언트 이름

class OcrResponse(BaseModel):
    userId: int
    year: int
    month: int
    schedules: List[ScheduleItem]