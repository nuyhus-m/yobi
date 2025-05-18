# models.py
from sqlalchemy import Column, BigInteger, Integer, ForeignKey, Float, String, Date, SmallInteger, Text, Boolean, Numeric, JSON
from sqlalchemy.orm import relationship
from core.database import Base

class Measure(Base):
    __tablename__ = "measure"

    measure_id = Column(BigInteger, primary_key=True)
    date = Column(BigInteger, nullable=False)
    user_id = Column(Integer, ForeignKey("users.user_id"), nullable=False)
    client_id = Column(Integer, ForeignKey("clients.client_id"), nullable=False)
    composition_id = Column(BigInteger, ForeignKey("body_composition.composition_id"), nullable=False)
    blood_id = Column(BigInteger, ForeignKey("blood_pressure.blood_id"), nullable=False)
    heart_id = Column(BigInteger, ForeignKey("heart_rate.heart_id"), nullable=True)
    stress_id = Column(BigInteger, ForeignKey("stress.stress_id"), nullable=True)
    temperature_id = Column(BigInteger, ForeignKey("temperature.temperature_id"), nullable=True)

    # 관계
    client = relationship("Client", back_populates="measures")
    user = relationship("User", back_populates="measures") 
    body_composition = relationship("BodyComposition", foreign_keys=[composition_id])
    blood_pressure = relationship("BloodPressure", foreign_keys=[blood_id])
    heart_rate = relationship("HeartRate", foreign_keys=[heart_id])
    stress = relationship("Stress", foreign_keys=[stress_id])
    temperature = relationship("Temperature", foreign_keys=[temperature_id])

class Client(Base):
    __tablename__ = "clients"

    client_id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.user_id"), nullable=False)

    name = Column(String(10), nullable=False)
    birth = Column(Date, nullable=False)
    gender = Column(Integer, nullable=False)
    height = Column(Float, nullable=False)
    weight = Column(Float, nullable=False)
    image = Column(String(255), nullable=True)
    address = Column(String(100), nullable=False)

    # 관계: 사용자 → 클라이언트
    user = relationship("User", back_populates="clients")
    # 관계: 클라이언트 → 측정값들
    measures = relationship("Measure", back_populates="client")
    schedules = relationship("Schedule", back_populates="client")


class User(Base):
    __tablename__ = "users"

    user_id = Column(Integer, primary_key=True)
    name = Column(String(10), nullable=False)
    employee_number = Column(Integer, nullable=False)
    password = Column(String(255), nullable=False)
    consent = Column(Boolean, nullable=False)
    image = Column(String(255), nullable=True)

    # 관계 설정: 한 명의 유저는 여러 클라이언트와 측정 데이터를 가질 수 있음
    clients = relationship("Client", back_populates="user")
    measures = relationship("Measure", back_populates="user")
    schedules = relationship("Schedule", back_populates="user")


class BodyComposition(Base):
    __tablename__ = "body_composition"

    composition_id = Column(BigInteger, primary_key=True)
    bfp = Column(Float)
    bfm = Column(Float)
    smm = Column(Float)
    bmr = Column(Float)
    protein = Column(Float)
    mineral = Column(Float)
    ecf = Column(Float)
    created_at = Column(BigInteger)


class BloodPressure(Base):
    __tablename__ = "blood_pressure"

    blood_id = Column(BigInteger, primary_key=True)
    sbp = Column(Float)
    dbp = Column(Float)
    created_at = Column(BigInteger)

class HeartRate(Base):
    __tablename__ = "heart_rate"

    heart_id = Column(BigInteger, primary_key=True)
    bpm = Column(SmallInteger)
    oxygen = Column(SmallInteger)
    created_at = Column(BigInteger)


class WeeklyReport(Base):
    __tablename__ = "weekly_report"

    report_id = Column(BigInteger, primary_key=True)
    client_id = Column(Integer, ForeignKey('clients.client_id')) 
    report_content = Column(Text)
    log_summary = Column(Text)
    created_at = Column(BigInteger)


class Schedule(Base):
    __tablename__ = "schedule"

    schedule_id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey("users.user_id"), nullable=False)
    client_id = Column(Integer, ForeignKey("clients.client_id"), nullable=False)
    visited_date = Column(BigInteger, nullable=False)
    start_at = Column(BigInteger, nullable=False)
    end_at = Column(BigInteger, nullable=False)
    log_content = Column(String(150))
    log_created_at = Column(BigInteger)
    log_updated_at = Column(BigInteger)

    # 관계 설정
    user = relationship("User", back_populates="schedules")
    client = relationship("Client", back_populates="schedules")


class BatchLog(Base):
    """
    배치 작업 로그 모델
    
    배치 작업의 실행 결과와 상세 정보를 저장하는 테이블
    """
    __tablename__ = 'batch_logs'

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    executed_at = Column(BigInteger, nullable=False)
    total_clients = Column(Integer, nullable=False)
    success_count = Column(Integer, nullable=False)
    error_count = Column(Integer, nullable=False)
    duration = Column(Numeric(10, 2), nullable=False)
    failed_client_ids = Column(JSON, nullable=True)
    details = Column(JSON, nullable=True)


