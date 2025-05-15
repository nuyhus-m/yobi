# batch/batch_retry.py


import asyncio
import argparse
from batch_generator import BatchReportGenerator
from core.config import settings

""" 
실패 리포트 수동 작업 처리 

터미널에서 실행:
python batch/batch_retry.py --batch-log-id 123

"""

async def main():
    parser = argparse.ArgumentParser(description='배치 작업 재처리')
    parser.add_argument('--batch-log-id', type=int, required=True, help='재처리할 배치 로그 ID')
    
    args = parser.parse_args()
    
    batch_generator = BatchReportGenerator(
        database_url=settings.DATABASE_URL
    )
    
    try:
        retry_results = await batch_generator.retry_failed_clients(args.batch_log_id)
        print(f"재처리 결과: {retry_results}")
    except Exception as e:
        print(f"재처리 중 오류 발생: {e}")

if __name__ == '__main__':
    asyncio.run(main())


