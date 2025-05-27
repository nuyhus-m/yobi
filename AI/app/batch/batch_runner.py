# batch/batch_runner.py
"""
배치 실행의 주요 워크플로우를 관리하는 메인 스크립트
"""

import sys
import asyncio
import argparse
import logging
import json

from datetime import datetime
from pathlib import Path
from typing import Dict, Optional

# 프로젝트 루트 경로 설정
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

# 내부 모듈 import
from batch.batch_scheduler import WeeklyReportScheduler
from batch.batch_generator import BatchReportGenerator
from core.config import settings

class BatchRunnerConfig:
    """배치 실행 설정 관리"""
    @staticmethod
    def setup_logging():
        """로깅 시스템 초기화"""
        log_level = getattr(settings, 'LOG_LEVEL', 'INFO')
        log_format = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
        
        # 로그 디렉토리 생성
        log_dir = Path(project_root) / 'logs'
        log_dir.mkdir(exist_ok=True)
        
        # 로그 파일 생성
        log_file = log_dir / f'batch_{datetime.now().strftime("%Y%m%d")}.log'
        
        logging.basicConfig(
            level=getattr(logging, log_level.upper()),
            format=log_format,
            handlers=[
                logging.FileHandler(log_file, encoding='utf-8'),
                logging.StreamHandler(sys.stdout)
            ]
        )



class BatchRunner:
    """배치 작업의 메인 실행 및 관리 클래스"""
    
    def __init__(self):
        # 로깅 설정
        BatchRunnerConfig.setup_logging()
        self.logger = logging.getLogger(__name__)
        
        # 배치 생성기 초기화
        self.batch_generator = BatchReportGenerator(
            database_url=settings.DATABASE_URL,

            # max_workers: 20개의 스레드/프로세스 풀 생성 가능/ 전체 작업 처리 능력
            # max_concurrent: 한 번에 10개의 작업만 동시 실행 /동시 실행 제한
            max_workers=getattr(settings, 'BATCH_MAX_WORKERS', 20),        
            max_concurrent=getattr(settings, 'BATCH_MAX_CONCURRENT', 10)    
        )


    
    async def run_scheduler(self):
        """스케줄러 모드로 실행"""
        self.logger.info("=== 스케줄러 모드 시작 ===")
        
        # 배치 작업 함수를 WeeklyReportScheduler의 생성자에게 전달 (run_batch_job)
        scheduler = WeeklyReportScheduler(self.run_batch_job)
        
        # 스케줄 설정
        # WeeklyReportScheduler에 스케쥴 설정하라고함 -> _run_async_job
        scheduler.schedule_weekly_job()
        

    # [디버깅 블록] 스케줄 등록 상태 및 작업 정보 확인
    # -----------------------------------------------
        # 목적:
        # 1. 작업 성공적 등록 여부 확인
        # 2. 다음 실행 시간 검증
        # 3. 현재 등록된 스케줄 작업 상세 정보 출력

        next_run = scheduler.get_next_run_time()
        if next_run:
            self.logger.info(f"다음 배치 실행 예정 시간: {next_run}")
        
        # 스케줄된 작업 목록 표시
        scheduler.list_scheduled_jobs()
    # -----------------------------------------------
        
        # 스케줄러 실행 (무한 대기)
        await scheduler.run_forever()



    
    async def run_batch_job(self):
        """주간 배치 작업 실행의 메인 메서드"""
        self.logger.info("=== 주간 리포트 배치 작업 시작 ===")
        
        try:
            # 배치 작업 실행
            result = await self.batch_generator.generate_all_reports()
            
            # 알림 전송
            # await BatchNotificationService.send_completion_notification(result)
            
            return result
            
        except Exception as e:
            self.logger.error(f"배치 작업 실행 중 치명적 오류: {str(e)}", exc_info=True)
            
            # 에러 알림 전송
            # await BatchNotificationService.send_error_notification(e)
            raise

# 파일 하단에 추가
if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(description='배치 작업 실행기')
    parser.add_argument('--mode', choices=['schedule', 'run_now'], default='schedule',
                      help='실행 모드 (schedule: 스케줄러 모드, run_now: 즉시 실행)')
    args = parser.parse_args()
    
    # 배치 러너 초기화
    runner = BatchRunner()
    
    # 비동기 이벤트 루프 실행
    if args.mode == 'schedule':
        # 스케줄러 모드
        asyncio.run(runner.run_scheduler())
    else:
        # 즉시 실행 모드
        asyncio.run(runner.run_batch_job())
    