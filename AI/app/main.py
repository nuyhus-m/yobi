#!/usr/bin/env python3
import asyncio
import sys
from pathlib import Path

# 프로젝트 루트 경로 설정
project_root = Path(__file__).parent
sys.path.insert(0, str(project_root))

from batch.batch_runner import BatchRunner

async def main():
    runner = BatchRunner()
    
    # 배치 작업 즉시 실행
    await runner.run_batch_job()
    
    # 또는 스케줄러로 실행 (주기적으로 실행하려면)
    # await runner.run_scheduler()

if __name__ == "__main__":
    asyncio.run(main())