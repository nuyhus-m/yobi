from typing import List, Optional
from pydantic import BaseModel

class ScheduleItem(BaseModel):
    day: int
    startAt: str
    endAt: str
    clientName: str

class OcrResponse(BaseModel):
    schedules: List[ScheduleItem]