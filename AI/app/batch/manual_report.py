# batch/batch_client_report.py
import sys
import asyncio
import argparse
from pathlib import Path

"""
리포트 수동 생성

특정 클라이언트 ID의 리포트 생성
python batch/batch_client_report.py report --client-id 123

현재 배치 작업 상태 확인
python batch/batch_client_report.py status

"""

# 프로젝트 루트 경로 추가
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

from batch.batch_generator import BatchReportGenerator
from core.config import settings

async def main():
    parser = argparse.ArgumentParser(description='단일 클라이언트 리포트 생성 및 배치 상태 확인')
    
    # 서브커맨드 추가
    subparsers = parser.add_subparsers(dest='command', help='실행할 작업')
    
    # 단일 클라이언트 리포트 생성 서브커맨드
    report_parser = subparsers.add_parser('report', help='단일 클라이언트 리포트 생성')
    report_parser.add_argument('--client-id', type=int, required=True, help='리포트를 생성할 클라이언트 ID')
    
    # 배치 상태 확인 서브커맨드
    status_parser = subparsers.add_parser('status', help='현재 배치 작업 상태 확인')
    
    # 인자 파싱
    args = parser.parse_args()
    
    # BatchReportGenerator 초기화
    batch_generator = BatchReportGenerator(
        database_url=settings.DATABASE_URL
    )
    
    try:
        if args.command == 'report':
            # 단일 클라이언트 리포트 생성
            result = await batch_generator.generate_single_client_report(args.client_id)
            print("\n단일 클라이언트 리포트 생성 결과:")
            print(f"클라이언트 ID: {result.get('client_id')}")
            print(f"상태: {result.get('status')}")
            if result.get('status') == 'success':
                print(f"리포트 ID: {result.get('report_id')}")
            else:
                print(f"오류: {result.get('error', '알 수 없는 오류')}")
        
        elif args.command == 'status':
            # 현재 배치 작업 상태 확인
            stats = batch_generator.get_current_stats()
            print("\n현재 배치 작업 상태:")
            print(f"실행 중: {'예' if stats['is_running'] else '아니오'}")
            print(f"경과 시간: {stats['elapsed_time']:.2f}초")
            print(f"총 클라이언트: {stats['total_clients']}")
            print(f"성공: {stats['success_count']}")
            print(f"실패: {stats['error_count']}")
            print(f"남은 작업: {stats['remaining']}")
        
        else:
            parser.print_help()
    
    except Exception as e:
        print(f"오류 발생: {e}")

if __name__ == '__main__':
    asyncio.run(main())