# batch/batch_scheduler.py
"""
배치 스케줄링 관리 모듈
"""


import schedule
import time
import asyncio
import signal
import logging
from datetime import datetime, timedelta
from typing import Callable, Optional

logger = logging.getLogger(__name__)

class WeeklyReportScheduler:
    """주간 리포트 스케줄러"""
    
    def __init__(self, batch_job_func: Callable):
        self.batch_job_func = batch_job_func  # batch_runner의 run_batch_job 메서드를 저장
        self.is_running = True
        self.last_batch_result = None  # 마지막 배치 결과 저장
        self.setup_signal_handlers()
    
    def setup_signal_handlers(self):
        """시그널 핸들러 설정"""
        signal.signal(signal.SIGINT, self._signal_handler)
        signal.signal(signal.SIGTERM, self._signal_handler)
    
    def _signal_handler(self, signum, frame):
        """안전한 종료를 위한 시그널 핸들러"""
        logger.info(f"신호 {signum} 수신. 안전하게 종료 중...")
        self.is_running = False


# [배치 작업 스케줄링 및 실행 블록] 주간 배치 작업의 스케줄링, 실행, 결과 처리
# -------------------------------------------------------------------------
# 목적:
# 1. 배치 작업의 실행 시간 예약
# 2. 비동기 작업을 동기 방식으로 안전하게 실행
# 3. 배치 작업 결과 로깅 및 모니터링

    def schedule_weekly_job(self):
        """ 1. 주간 배치 작업 스케줄 설정"""
        # schedule 라이브러리를 사용해 특정 시간에 _run_async_job 메서드 예약
        schedule.every().friday.at("23:59").do(self._run_async_job)
        
        # 개발/테스트용 옵션
        # schedule.every().minute.do(self._run_async_job)  # 매분 실행
        # schedule.every().hour.do(self._run_async_job)    # 매시간 실행
        
        logger.info("주간 배치 작업 스케줄이 설정되었습니다.")
    

    def _run_async_job(self): # 저장된 run_batch_job 메서드 실행
        """2. 비동기 작업을 동기 방식으로 실행"""
        try:
            # 새로운 이벤트 루프에서 실행
            
            loop = asyncio.new_event_loop()
            
            # 안되면
            #loop = asyncio.get_running_loop()

            asyncio.set_event_loop(loop)


            try:
                # 전달받은 배치 작업 함수 실행 (batch_runner의 run_batch_job 메서드)
                self.last_batch_result = loop.run_until_complete(self.batch_job_func())

                # 결과 로깅 또는 추가 처리 가능
                self._process_batch_result(self.last_batch_result)
            finally:
                loop.close()
        except Exception as e:
            logger.error(f"배치 작업 실행 중 오류: {str(e)}", exc_info=True)


    def _process_batch_result(self, result):
        """3. 배치 결과 처리 메서드"""
        logger.info("=== 배치 작업 결과 ===")
        logger.info(f"총 처리 클라이언트: {result['total_clients']}")
        logger.info(f"성공: {result['success_count']}")
        logger.info(f"실패: {result['error_count']}")
        logger.info(f"성공률: {result['success_rate']:.1f}%")
    
# -------------------------------------------------------------------------




    async def run_forever(self):
        """스케줄러를 영구적으로 실행"""
        logger.info("스케줄러가 시작되었습니다.")
        
        while self.is_running:
            schedule.run_pending()
            await asyncio.sleep(1)  # 1초마다 확인
        
        logger.info("스케줄러가 종료되었습니다.")
    


    def run_forever_sync(self):
        """동기 방식으로 스케줄러 실행"""
        logger.info("스케줄러가 시작되었습니다. (동기 모드)")
        
        while self.is_running:
            schedule.run_pending()
            time.sleep(60)  # 1분마다 확인
        
        logger.info("스케줄러가 종료되었습니다.")
    


    def get_next_run_time(self) -> Optional[datetime]:
        """다음 실행 시간 조회"""

        # schedule 라이브러리에 등록된 모든 작업 목록 가져오기
        jobs = schedule.jobs

        # 등록된 작업이 없으면 None 반환
        if not jobs:
            return None
        
        # 각 작업의 다음 실행 시간을 리스트로 추출
        # job.next_run이 있는 작업들만 필터링
        next_times = [job.next_run for job in jobs if job.next_run]

        # 가장 빠른 실행 시간 반환 (next_times가 비어있으면 None)
        return min(next_times) if next_times else None
    


    def list_scheduled_jobs(self):
        """스케줄된 작업 목록 출력"""
        jobs = schedule.jobs
        logger.info(f"총 {len(jobs)}개의 스케줄된 작업이 있습니다.")
        
        for i, job in enumerate(jobs, 1):
            logger.info(f"{i}. {job}")
            logger.info(f"   다음 실행: {job.next_run}")
    
    def clear_all_jobs(self):
        """모든 스케줄된 작업 제거"""
        schedule.clear()
        logger.info("모든 스케줄된 작업이 제거되었습니다.")





# 특별한 스케줄링 옵션들
class AdvancedScheduler(WeeklyReportScheduler):
    """고급 스케줄링 기능이 포함된 스케줄러"""
    
    def schedule_with_retry(self, max_retries: int = 3, retry_interval: int = 60):
        """재시도 기능이 포함된 스케줄"""
        def job_with_retry():
            for attempt in range(max_retries):
                try:
                    logger.info(f"배치 작업 실행 시도 {attempt + 1}/{max_retries}")
                    self._run_async_job()
                    logger.info("배치 작업 성공")
                    break
                except Exception as e:
                    logger.error(f"배치 작업 실패 (시도 {attempt + 1}): {str(e)}")
                    if attempt < max_retries - 1:
                        logger.info(f"{retry_interval}초 후 재시도...")
                        time.sleep(retry_interval)
                    else:
                        logger.error("모든 재시도 실패")
                        raise
        
        schedule.every().friday.at("23:59").do(job_with_retry)
    
    def schedule_with_window(self, start_time: str, end_time: str):
        """특정 시간 범위 내에서만 실행"""
        def job_with_window():
            current_time = datetime.now().time()
            start = datetime.strptime(start_time, "%H:%M").time()
            end = datetime.strptime(end_time, "%H:%M").time()
            
            if start <= current_time <= end:
                self._run_async_job()
            else:
                logger.info(f"실행 시간 범위({start_time}-{end_time})가 아닙니다.")
        
        # 매시간 확인하여 범위 내에서 실행
        schedule.every().hour.do(job_with_window)
    
    def schedule_conditional(self, condition_func: Callable[[], bool]):
        """조건부 실행"""
        def conditional_job():
            if condition_func():
                self._run_async_job()
            else:
                logger.info("조건을 만족하지 않아 배치 작업을 건너뜁니다.")
        
        schedule.every().friday.at("23:59").do(conditional_job)


# 사용 예시
if __name__ == "__main__":
    # 테스트용 배치 작업
    async def test_batch_job():
        logger.info("테스트 배치 작업 시작")
        await asyncio.sleep(2)  # 2초 대기
        logger.info("테스트 배치 작업 완료")
    
    # 스케줄러 생성 및 실행
    scheduler = WeeklyReportScheduler(test_batch_job)
    scheduler.schedule_weekly_job()
    scheduler.list_scheduled_jobs()
    
    # 동기 방식으로 실행
    scheduler.run_forever_sync()

