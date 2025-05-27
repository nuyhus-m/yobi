# batch/batch_generator.py
"""
배치 리포트 생성 모듈
"""

import asyncio
import aiohttp
import json
import time
import logging
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import List, Dict, Optional
from datetime import datetime, timedelta
from contextlib import contextmanager
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker, Session
from services.health_data_service import HealthDataProcessor
from models import BatchLog

logger = logging.getLogger(__name__)

class BatchReportGenerator:
    """배치 리포트 생성기"""
    
    def __init__(self, database_url: str, max_workers: int = 20, max_concurrent: int = 10):
        self.database_url = database_url
        self.max_workers = max_workers
        self.max_concurrent = max_concurrent
        self.engine = create_engine(database_url)
        self.SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=self.engine)
        
        # 성능 지표
        self.stats = {
            "start_time": None,
            "end_time": None,
            "total_clients": 0,
            "success_count": 0,
            "error_count": 0,
            "errors": []
        }

# [데이터베이스 세션 관리 블록] 안전한 데이터베이스 세션 생성 및 처리
# -------------------------------------------------------------------------
# 목적:
# 1. 데이터베이스 세션의 안전한 생성
# 2. 예외 발생 시 롤백 처리
# 3. 세션 자원의 안전한 해제
    
    @contextmanager
    def get_db_session(self):
        """데이터베이스 세션 컨텍스트 매니저"""
        db = self.SessionLocal()
        try:
            yield db
        except Exception as e:
            db.rollback()
            raise
        finally:
            db.close()
# -------------------------------------------------------------------------           




# [배치 리포트 생성 워크플로우 블록] 클라이언트 리포트 대량 생성 및 관리
# -------------------------------------------------------------------------
# 목적:
# 1. 활성 클라이언트 식별 및 필터링
# 2. 병렬 리포트 생성 프로세스 관리
# 3. 배치 작업 성과 추적 및 통계 생성
# 4. 배치 실행 결과 로깅 및 모니터링
#   주요 워크플로우:
#       - 병렬 리포트 생성
#       - 활성 클라이언트 조회
#       - 결과 종합 및 통계 계산
#       - 배치 로그 저장
    

    async def generate_all_reports(self) -> Dict:
        """모든 활성 클라이언트의 리포트 생성"""
        self.stats["start_time"] = time.time()
        logger.info("=== 배치 리포트 생성 시작 ===")
        
        # 활성 클라이언트 조회
        active_clients = await self.get_active_clients()
        self.stats["total_clients"] = len(active_clients)
        
        if not active_clients:
            logger.warning("활성 클라이언트가 없습니다.")
            return self._create_result_summary()
        
        # 비동기 HTTP 세션 생성
        connector = aiohttp.TCPConnector(limit=self.max_concurrent)
        timeout = aiohttp.ClientTimeout(total=60)
        
        async with aiohttp.ClientSession(connector=connector, timeout=timeout) as session:
            # 세마포어: 동시 실행 수 제한
            semaphore = asyncio.Semaphore(self.max_concurrent)
            
            async def process_with_semaphore(client_data):
                # 세마포어 획득 → 작업 수행 → 세마포어 해제
                async with semaphore:
                    return await self.generate_single_report(client_data, session) # ✅🏃본격적으로 리포트 만들기!!
            
            # 모든 클라이언트에 대해 동시 실행
            tasks = [process_with_semaphore(client) for client in active_clients]
            
            # 진행 상황 모니터링을 위한 콜백
            completed_tasks = 0
            async def progress_callback(task):
                nonlocal completed_tasks
                completed_tasks += 1
                progress = (completed_tasks / len(tasks)) * 100
                logger.info(f"진행 상황: {completed_tasks}/{len(tasks)} ({progress:.1f}%)")
            
            # 작업 실행 및 진행 상황 추적
            results = []
            for task in asyncio.as_completed(tasks):
                result = await task
                await progress_callback(task)
                results.append(result)
                
                # 결과 분류
                if result.get("status") == "success":
                    self.stats["success_count"] += 1
                else:
                    self.stats["error_count"] += 1
        
        self.stats["end_time"] = time.time()
        
        # 결과 요약
        result_summary = self._create_result_summary()
        result_summary["details"] = results
        
        logger.info("=== 배치 리포트 생성 완료 ===")
        logger.info(f"총 소요시간: {result_summary['total_duration']:.2f}초")
        logger.info(f"성공: {self.stats['success_count']}, 실패: {self.stats['error_count']}")
        
        # 배치 로그 저장
        await self._save_batch_log(result_summary)
        
        return result_summary
    

    async def get_active_clients(self) -> List[Dict]:
        """활성 클라이언트 목록 조회"""
        with self.get_db_session() as db:
            # 최근 7일 내에 측정 데이터가 있는 클라이언트만 조회
            seven_days_ago = int((time.time() - 7 * 24 * 3600) * 1000)
            
            query = text("""
                SELECT DISTINCT c.client_id, c.user_id, c.name
                FROM clients c 
                JOIN measure m ON c.client_id = m.client_id 
                WHERE m.date >= :seven_days_ago
                ORDER BY c.client_id
            """)
            
            result = db.execute(query, {"seven_days_ago": seven_days_ago})
            clients = [
                {
                    "client_id": row.client_id, 
                    "user_id": row.user_id, 
                    "name": row.name
                } 
                for row in result
            ]

            
            
            logger.info(f"활성 클라이언트 {len(clients)}명 조회 완료")

            # return clients

            # client_id가 1인 클라이언트만 찾아서 반환
            for client in clients:
                if client["client_id"] == 1:
                    logger.info(f"client_id가 1인 클라이언트 조회 완료")
                    return [client]
            
            # client_id가 1인 클라이언트가 없는 경우
            logger.info("client_id가 1인 클라이언트가 없습니다")
            return None


    def _create_result_summary(self) -> Dict:
        """배치 실행 결과 요약 생성"""
        total_duration = (self.stats["end_time"] - self.stats["start_time"]) if self.stats["end_time"] else 0
        
        return {
            "batch_start": self.stats["start_time"],
            "batch_end": self.stats["end_time"],
            "total_duration": total_duration,
            "total_clients": self.stats["total_clients"],
            "success_count": self.stats["success_count"],
            "error_count": self.stats["error_count"],
            "success_rate": (self.stats["success_count"] / self.stats["total_clients"] * 100) if self.stats["total_clients"] > 0 else 0,
            "average_time_per_client": total_duration / self.stats["total_clients"] if self.stats["total_clients"] > 0 else 0,
            "errors": self.stats["errors"]
        }
    
    async def _save_batch_log(self, result_summary: Dict):
        """배치 실행 결과 로그 저장 (실패한 건 저장장)"""
        try:
            
            with self.get_db_session() as db:
                # 실패한 클라이언트 ID 추출
                failed_client_ids = [
                    error['client_id'] 
                    for error in result_summary.get('errors', [])
                ]

                batch_log_data = {
                    "executed_at": int(result_summary["batch_start"] * 1000),
                    "total_clients": result_summary["total_clients"],
                    "success_count": result_summary["success_count"],
                    "error_count": result_summary["error_count"],
                    "duration": result_summary["total_duration"],
                    "success_rate": result_summary["success_rate"],
                    "failed_client_ids": failed_client_ids,  # 실패한 클라이언트 ID 저장
                    "details": json.dumps(result_summary, ensure_ascii=False)
                }
                
                # 실제 로그 저장 로직
                query = text("""
                    INSERT INTO batch_logs (
                        executed_at, total_clients, success_count, 
                        error_count, duration, success_rate, 
                        failed_client_ids, details
                    ) VALUES (
                        :executed_at, :total_clients, :success_count, 
                        :error_count, :duration, :success_rate, 
                        :failed_client_ids, :details
                    )
                """)
                db.execute(query, batch_log_data)
                db.commit()
                
                logger.info("배치 실행 로그 저장 완료")
                
        except Exception as e:
            logger.error(f"배치 로그 저장 실패: {str(e)}")
# -------------------------------------------------------------------------






# [단일 클라이언트 리포트 생성 블록] 개별 클라이언트 건강 리포트 생성 프로세스
# -------------------------------------------------------------------------
# 목적:
# 1. 개별 클라이언트의 건강 데이터 수집 및 분석
# 2. AI 기반 건강 상태 종합 평가
# 3. 일지 데이터 통합
# 4. OpenAI를 활용한 최종 리포트 생성
# 5. 데이터베이스 저장
#
# 주요 워크플로우:
#   - 건강 데이터 수집
#   - 내부 AI 분석
#   - 일지 데이터 통합
#   - 최종 AI 분석
#   - 리포트 저장
#
# 오류 처리:
#   - 각 단계별 예외 캡처
#   - 오류 정보 기록
#   - 배치 통계에 오류 반영


    
    async def generate_single_report(self, client_data: Dict, session: aiohttp.ClientSession) -> Dict:

        """단일 클라이언트 리포트 생성"""
        client_id = client_data['client_id']
        user_id = client_data['user_id']
        client_name = client_data.get('name', f'Client-{client_id}')
        
        start_time = time.time()
        
        try:
            with self.get_db_session() as db:
                # HealthDataProcessor를 직접 import (여기서는 예시)
                from services.health_data_service import HealthDataProcessor
                processor = HealthDataProcessor(db)
                
                # 배치 모드 설정
                processor.set_batch_mode(True, session) # aiohttp 세션 전달
                
                # 1. 건강 데이터 수집
                health_data = await processor.collect_health_data(client_id, user_id)
                
                # 2. 내부 AI 호출
                internal_ai_response = await processor.call_internal_ai(health_data)
                
                # 3. AI output 정제
                ai_summary = internal_ai_response
                
                # 4. 일지 데이터 조회
                journal_data = await processor.get_journal_data(client_id, user_id)
                
                # 5. OpenAI 호출
                openai_result = await processor.call_openai(ai_summary, journal_data)
                
                # 6. 데이터베이스 저장
                report_id = processor.save_to_database(client_id, user_id, ai_summary, openai_result)
                
                end_time = time.time()
                duration = end_time - start_time
                
                logger.info(f"리포트 생성 성공 - {client_name} (Client ID: {client_id}), 소요시간: {duration:.2f}초")
                
                return {
                    "client_id": client_id,
                    "client_name": client_name,
                    "report_id": report_id,
                    "status": "success",
                    "duration": duration,
                    "health_records": len(health_data.get("dm", [])),
                    "journal_entries": len(journal_data),
                    "anomalies": len(ai_summary.get("anom", []))
                }
                
        except Exception as e:
            end_time = time.time()
            duration = end_time - start_time
            
            error_info = {
                "client_id": client_id,
                "client_name": client_name,
                "error": str(e),
                "error_type": type(e).__name__,
                "duration": duration
            }
            
            logger.error(f"리포트 생성 실패 - {client_name} (Client ID: {client_id}): {str(e)}")
            self.stats["errors"].append(error_info)
            
            return {
                "client_id": client_id,
                "client_name": client_name,
                "status": "error",
                "error": str(e),
                "duration": duration
            }
    
    
# -------------------------------------------------------------------------



# [배치 실패 클라이언트 재처리 블록] 과거 배치 작업의 실패한 클라이언트 재시도
# -------------------------------------------------------------------------
# 목적:
# 1. 이전 배치 작업에서 실패한 클라이언트 식별
# 2. 실패한 클라이언트 개별 리포트 재생성
# 3. 실패 원인 분석 및 복구
#
# 주요 워크플로우:
#   - 배치 로그에서 실패 클라이언트 ID 추출
#   - 클라이언트 정보 조회
#   - 개별 리포트 재생성
#   - 재처리 결과 수집
#
# 오류 처리:
#   - 개별 클라이언트 실패 시 전체 프로세스 중단 방지
#   - 실패 로그 기록

    async def retry_failed_clients(self, batch_log_id: int):
        """특정 배치의 실패한 클라이언트 재처리"""
        with self.get_db_session() as db:
            # 배치 로그 조회
            batch_log = db.query(BatchLog).filter(BatchLog.id == batch_log_id).first()
            
            if not batch_log:
                raise ValueError("해당 배치 로그를 찾을 수 없습니다.")
            
            # 실패한 클라이언트 ID 추출
            failed_client_ids = json.loads(batch_log.failed_client_ids)

            # 실패한 클라이언트들 재처리
            retry_results = []
            for client_id in failed_client_ids:
                try:
                    # 클라이언트 정보 조회
                    client_data = self._get_client_data(client_id)
                    
                    # 단일 클라이언트 리포트 재생성
                    async with aiohttp.ClientSession() as session:
                        result = await self.generate_single_report(client_data, session)
                    retry_results.append(result)
                
                except Exception as e:
                    logger.error(f"클라이언트 {client_id} 재처리 실패: {str(e)}")
            
            return retry_results

    def _get_client_data(self, client_id: int) -> Dict:
        """클라이언트 기본 정보 조회"""
        with self.get_db_session() as db:
            query = text("""
                SELECT client_id, user_id, name 
                FROM clients 
                WHERE client_id = :client_id
            """)
            result = db.execute(query, {"client_id": client_id}).first()
            
            if not result:
                raise ValueError(f"클라이언트 ID {client_id}를 찾을 수 없습니다.")
            
            return {
                "client_id": result.client_id,
                "user_id": result.user_id,
                "name": result.name
            }
# -------------------------------------------------------------------------





    async def generate_single_client_report(self, client_id: int) -> Dict:
        """단일 클라이언트의 리포트 생성 (테스트용)"""
        with self.get_db_session() as db:
            client_query = text("SELECT client_id, user_id, name FROM clients WHERE client_id = :client_id")
            result = db.execute(client_query, {"client_id": client_id}).first()
            
            if not result:
                raise ValueError(f"클라이언트 ID {client_id}를 찾을 수 없습니다.")
            
            client_data = {
                "client_id": result.client_id,
                "user_id": result.user_id,
                "name": result.name
            }
            
            async with aiohttp.ClientSession() as session:
                return await self.generate_single_report(client_data, session)
    
    def get_current_stats(self) -> Dict:
        """현재 진행 상황 조회"""
        return {
            "is_running": self.stats["end_time"] is None and self.stats["start_time"] is not None,
            "elapsed_time": (time.time() - self.stats["start_time"]) if self.stats["start_time"] else 0,
            "total_clients": self.stats["total_clients"],
            "success_count": self.stats["success_count"],
            "error_count": self.stats["error_count"],
            "remaining": self.stats["total_clients"] - self.stats["success_count"] - self.stats["error_count"]
        }


# 사용 예시
if __name__ == "__main__":
    async def test_batch_generation():
        from core.config import settings
        
        generator = BatchReportGenerator(
            database_url=settings.DATABASE_URL,
            max_workers=10,
            max_concurrent=5
        )
        
        # 단일 클라이언트 테스트
        # result = await generator.generate_single_client_report(1)
        # print(f"단일 클라이언트 결과: {result}")
        
        # 전체 배치 실행
        result = await generator.generate_all_reports()
        print(f"배치 결과: {result}")
    
    asyncio.run(test_batch_generation())