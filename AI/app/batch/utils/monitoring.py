# batch/utils/monitoring.py
"""
배치 모니터링 모듈
"""

import time
import psutil
import asyncio
import logging
from datetime import datetime, timedelta
from typing import Dict, List, Optional, Callable
from dataclasses import dataclass, field
from collections import defaultdict, deque
import json
import threading

logger = logging.getLogger(__name__)

@dataclass
class PerformanceMetrics:
    """성능 지표 데이터 클래스"""
    start_time: float = 0.0
    end_time: float = 0.0
    total_clients: int = 0
    success_count: int = 0
    error_count: int = 0
    processing_times: List[float] = field(default_factory=list)
    memory_usage: List[Dict] = field(default_factory=list)
    cpu_usage: List[float] = field(default_factory=list)
    errors: List[Dict] = field(default_factory=list)
    
    @property
    def duration(self) -> float:
        """총 실행 시간"""
        return (self.end_time or time.time()) - self.start_time
    
    @property
    def success_rate(self) -> float:
        """성공률"""
        if self.total_clients == 0:
            return 0.0
        return (self.success_count / self.total_clients) * 100
    
    @property
    def average_processing_time(self) -> float:
        """평균 처리 시간"""
        if not self.processing_times:
            return 0.0
        return sum(self.processing_times) / len(self.processing_times)
    
    @property
    def peak_memory_usage(self) -> float:
        """최대 메모리 사용량 (MB)"""
        if not self.memory_usage:
            return 0.0
        return max(mem['rss'] for mem in self.memory_usage) / 1024 / 1024
    
    @property
    def average_cpu_usage(self) -> float:
        """평균 CPU 사용률"""
        if not self.cpu_usage:
            return 0.0
        return sum(self.cpu_usage) / len(self.cpu_usage)
    
    def to_dict(self) -> Dict:
        """딕셔너리로 변환"""
        return {
            'start_time': self.start_time,
            'end_time': self.end_time,
            'duration': self.duration,
            'total_clients': self.total_clients,
            'success_count': self.success_count,
            'error_count': self.error_count,
            'success_rate': self.success_rate,
            'average_processing_time': self.average_processing_time,
            'peak_memory_usage_mb': self.peak_memory_usage,
            'average_cpu_usage': self.average_cpu_usage,
            'error_summary': [
                {'client_id': err['client_id'], 'error_type': err['error_type']} 
                for err in self.errors
            ]
        }


class BatchMonitor:
    """배치 작업 모니터링"""
    
    def __init__(self, update_interval: float = 5.0):
        self.update_interval = update_interval
        self.metrics = PerformanceMetrics()
        self.is_monitoring = False
        self.monitoring_task = None
        self.client_progress = {}
        self.stage_timings = defaultdict(list)
        self.realtime_callbacks = []
        
        # 최근 성능 이력 (최대 100개 보관)
        self.performance_history = deque(maxlen=100)
        
    def start_monitoring(self):
        """모니터링 시작"""
        self.metrics.start_time = time.time()
        self.is_monitoring = True
        self.monitoring_task = asyncio.create_task(self._monitor_loop())
        logger.info("배치 모니터링 시작")
    
    async def stop_monitoring(self):
        """모니터링 중지"""
        self.is_monitoring = False
        self.metrics.end_time = time.time()
        
        if self.monitoring_task:
            self.monitoring_task.cancel()
            try:
                await self.monitoring_task
            except asyncio.CancelledError:
                pass
        
        # 최종 리포트 생성
        report = self.get_final_report()
        self.performance_history.append(report)
        
        logger.info("배치 모니터링 종료")
        logger.info(f"최종 결과: {json.dumps(report, ensure_ascii=False, indent=2)}")
        
        return report
    
    async def _monitor_loop(self):
        """모니터링 루프"""
        try:
            while self.is_monitoring:
                # 시스템 리소스 수집
                self._collect_system_metrics()
                
                # 실시간 콜백 실행
                await self._execute_callbacks()
                
                await asyncio.sleep(self.update_interval)
        except asyncio.CancelledError:
            pass
        except Exception as e:
            logger.error(f"모니터링 중 오류: {str(e)}")
    
    def _collect_system_metrics(self):
        """시스템 지표 수집"""
        try:
            # 프로세스 정보
            process = psutil.Process()
            
            # 메모리 사용량
            memory_info = process.memory_info()
            self.metrics.memory_usage.append({
                'timestamp': time.time(),
                'rss': memory_info.rss,  # 상주 세트 크기
                'vms': memory_info.vms,  # 가상 메모리 크기
                'percent': process.memory_percent()
            })
            
            # CPU 사용률
            cpu_percent = process.cpu_percent()
            self.metrics.cpu_usage.append(cpu_percent)
            
            # 시스템 전체 정보
            system_info = {
                'cpu_percent': psutil.cpu_percent(),
                'memory_percent': psutil.virtual_memory().percent,
                'disk_usage': psutil.disk_usage('/').percent
            }
            
        except Exception as e:
            logger.error(f"시스템 지표 수집 중 오류: {str(e)}")
    
    def record_client_start(self, client_id: int, client_name: str = None):
        """클라이언트 처리 시작 기록"""
        self.client_progress[client_id] = {
            'start_time': time.time(),
            'name': client_name,
            'status': 'processing',
            'stages': {}
        }
    
    def record_client_stage(self, client_id: int, stage: str):
        """클라이언트 처리 단계 기록"""
        if client_id in self.client_progress:
            self.client_progress[client_id]['stages'][stage] = time.time()
    
    def record_client_success(self, client_id: int, processing_time: float):
        """클라이언트 처리 성공 기록"""
        self.metrics.success_count += 1
        self.metrics.processing_times.append(processing_time)
        
        if client_id in self.client_progress:
            self.client_progress[client_id]['status'] = 'success'
            self.client_progress[client_id]['end_time'] = time.time()
            
            # 단계별 시간 기록
            self._record_stage_timings(client_id)
    
    def record_client_error(self, client_id: int, error: Exception, processing_time: float):
        """클라이언트 처리 에러 기록"""
        self.metrics.error_count += 1
        self.metrics.processing_times.append(processing_time)
        
        error_info = {
            'client_id': client_id,
            'error_type': type(error).__name__,
            'error_message': str(error),
            'timestamp': time.time(),
            'processing_time': processing_time
        }
        self.metrics.errors.append(error_info)
        
        if client_id in self.client_progress:
            self.client_progress[client_id]['status'] = 'error'
            self.client_progress[client_id]['error'] = error_info
            self.client_progress[client_id]['end_time'] = time.time()
            
            # 단계별 시간 기록
            self._record_stage_timings(client_id)
    
    def _record_stage_timings(self, client_id: int):
        """단계별 처리 시간 기록"""
        client_info = self.client_progress[client_id]
        start_time = client_info['start_time']
        
        prev_time = start_time
        for stage, stage_time in client_info['stages'].items():
            duration = stage_time - prev_time
            self.stage_timings[stage].append(duration)
            prev_time = stage_time
    
    def set_total_clients(self, count: int):
        """총 클라이언트 수 설정"""
        self.metrics.total_clients = count
    
    def get_current_status(self) -> Dict:
        """현재 상태 반환"""
        completed = self.metrics.success_count + self.metrics.error_count
        progress = (completed / self.metrics.total_clients * 100) if self.metrics.total_clients > 0 else 0
        
        # 예상 남은 시간 계산
        if completed > 0 and self.metrics.duration > 0:
            avg_time_per_client = self.metrics.duration / completed
            remaining_clients = self.metrics.total_clients - completed
            estimated_remaining_time = avg_time_per_client * remaining_clients
        else:
            estimated_remaining_time = 0
        
        return {
            'total_clients': self.metrics.total_clients,
            'completed': completed,
            'success': self.metrics.success_count,
            'error': self.metrics.error_count,
            'progress_percent': progress,
            'elapsed_time': self.metrics.duration,
            'estimated_remaining_time': estimated_remaining_time,
            'average_processing_time': self.metrics.average_processing_time,
            'current_memory_mb': self.metrics.peak_memory_usage,
            'current_cpu_percent': self.metrics.average_cpu_usage
        }
    
    def get_detailed_statistics(self) -> Dict:
        """상세 통계 정보 반환"""
        # 처리 시간 통계
        if self.metrics.processing_times:
            times = sorted(self.metrics.processing_times)
            processing_stats = {
                'min': min(times),
                'max': max(times),
                'avg': self.metrics.average_processing_time,
                'median': times[len(times) // 2],
                'p95': times[int(len(times) * 0.95)],
                'p99': times[int(len(times) * 0.99)]
            }
        else:
            processing_stats = {}
        
        # 단계별 평균 시간
        stage_stats = {}
        for stage, times in self.stage_timings.items():
            if times:
                stage_stats[stage] = {
                    'avg': sum(times) / len(times),
                    'min': min(times),
                    'max': max(times)
                }
        
        # 에러 통계
        error_stats = defaultdict(int)
        for error in self.metrics.errors:
            error_stats[error['error_type']] += 1
        
        return {
            'processing_time_stats': processing_stats,
            'stage_timing_stats': stage_stats,
            'error_distribution': dict(error_stats),
            'resource_usage': {
                'peak_memory_mb': self.metrics.peak_memory_usage,
                'avg_cpu_percent': self.metrics.average_cpu_usage
            }
        }
    
    def get_final_report(self) -> Dict:
        """최종 리포트 생성"""
        basic_metrics = self.metrics.to_dict()
        detailed_stats = self.get_detailed_statistics()
        
        return {
            **basic_metrics,
            'detailed_statistics': detailed_stats,
            'client_breakdown': {
                'successful_clients': [
                    cid for cid, info in self.client_progress.items() 
                    if info['status'] == 'success'
                ],
                'failed_clients': [
                    {'client_id': cid, 'error': info.get('error', {})} 
                    for cid, info in self.client_progress.items() 
                    if info['status'] == 'error'
                ]
            }
        }
    
    def add_realtime_callback(self, callback: Callable):
        """실시간 콜백 추가"""
        self.realtime_callbacks.append(callback)
    
    async def _execute_callbacks(self):
        """실시간 콜백 실행"""
        status = self.get_current_status()
        for callback in self.realtime_callbacks:
            try:
                if asyncio.iscoroutinefunction(callback):
                    await callback(status)
                else:
                    callback(status)
            except Exception as e:
                logger.error(f"콜백 실행 중 오류: {str(e)}")
    
    def export_metrics(self, filepath: str):
        """지표를 파일로 내보내기"""
        report = self.get_final_report()
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(report, f, ensure_ascii=False, indent=2)
        logger.info(f"지표 내보내기 완료: {filepath}")


class ProgressTracker:
    """진행 상황 추적기"""
    
    def __init__(self, total: int, description: str = "Processing"):
        self.total = total
        self.description = description
        self.completed = 0
        self.start_time = time.time()
        self.last_update = 0
        self.update_interval = 1.0  # 1초마다 업데이트
    
    def update(self, increment: int = 1):
        """진행 상황 업데이트"""
        self.completed += increment
        
        # 업데이트 간격 체크
        current_time = time.time()
        if current_time - self.last_update >= self.update_interval:
            self._print_progress()
            self.last_update = current_time
    
    def _print_progress(self):
        """진행 상황 출력"""
        progress = self.completed / self.total * 100
        elapsed = time.time() - self.start_time
        
        if self.completed > 0:
            eta = (elapsed / self.completed) * (self.total - self.completed)
            eta_str = f", ETA: {eta:.1f}s"
        else:
            eta_str = ""
        
        progress_bar = self._create_progress_bar(progress)
        
        print(f"\r{self.description}: {progress_bar} {self.completed}/{self.total} "
              f"({progress:.1f}%) [{elapsed:.1f}s{eta_str}]", end="", flush=True)
    
    def _create_progress_bar(self, progress: float, width: int = 30) -> str:
        """진행률 바 생성"""
        filled = int(width * progress / 100)
        bar = "█" * filled + "░" * (width - filled)
        return f"[{bar}]"
    
    def finish(self):
        """완료 처리"""
        self.completed = self.total
        self._print_progress()
        print()  # 새 줄 추가


class SystemResourceMonitor:
    """시스템 리소스 모니터"""
    
    def __init__(self):
        self.monitoring = False
        self.resource_history = deque(maxlen=1000)
        self.alerts = []
        
        # 경고 임계값
        self.memory_threshold = 80.0  # 80%
        self.cpu_threshold = 85.0     # 85%
        self.disk_threshold = 90.0    # 90%
    
    def start_monitoring(self, interval: float = 1.0):
        """리소스 모니터링 시작"""
        self.monitoring = True
        self.monitor_thread = threading.Thread(target=self._monitor_resources, args=(interval,))
        self.monitor_thread.daemon = True
        self.monitor_thread.start()
        logger.info("시스템 리소스 모니터링 시작")
    
    def stop_monitoring(self):
        """리소스 모니터링 중지"""
        self.monitoring = False
        logger.info("시스템 리소스 모니터링 중지")
    
    def _monitor_resources(self, interval: float):
        """리소스 모니터링 루프"""
        while self.monitoring:
            try:
                # CPU 사용률
                cpu_percent = psutil.cpu_percent(interval=0.1)
                
                # 메모리 사용률
                memory = psutil.virtual_memory()
                
                # 디스크 사용률
                disk = psutil.disk_usage('/')
                
                # 네트워크 통계
                net_io = psutil.net_io_counters()
                
                resource_data = {
                    'timestamp': time.time(),
                    'cpu_percent': cpu_percent,
                    'memory_percent': memory.percent,
                    'memory_used_gb': memory.used / 1024 / 1024 / 1024,
                    'memory_total_gb': memory.total / 1024 / 1024 / 1024,
                    'disk_percent': disk.percent,
                    'disk_used_gb': disk.used / 1024 / 1024 / 1024,
                    'disk_total_gb': disk.total / 1024 / 1024 / 1024,
                    'network_bytes_sent': net_io.bytes_sent,
                    'network_bytes_recv': net_io.bytes_recv
                }
                
                self.resource_history.append(resource_data)
                
                # 경고 체크
                self._check_alerts(resource_data)
                
                time.sleep(interval)
                
            except Exception as e:
                logger.error(f"리소스 모니터링 중 오류: {str(e)}")
                time.sleep(interval)
    
    def _check_alerts(self, resource_data: Dict):
        """리소스 경고 체크"""
        alerts = []
        
        if resource_data['cpu_percent'] > self.cpu_threshold:
            alerts.append({
                'type': 'cpu',
                'message': f"CPU 사용률 높음: {resource_data['cpu_percent']:.1f}%",
                'timestamp': resource_data['timestamp']
            })
        
        if resource_data['memory_percent'] > self.memory_threshold:
            alerts.append({
                'type': 'memory',
                'message': f"메모리 사용률 높음: {resource_data['memory_percent']:.1f}%",
                'timestamp': resource_data['timestamp']
            })
        
        if resource_data['disk_percent'] > self.disk_threshold:
            alerts.append({
                'type': 'disk',
                'message': f"디스크 사용률 높음: {resource_data['disk_percent']:.1f}%",
                'timestamp': resource_data['timestamp']
            })
        
        self.alerts.extend(alerts)
        
        # 최근 100개만 보관
        if len(self.alerts) > 100:
            self.alerts = self.alerts[-100:]
        
        # 경고 로그 출력
        for alert in alerts:
            logger.warning(alert['message'])
    
    def get_current_status(self) -> Dict:
        """현재 리소스 상태 반환"""
        if not self.resource_history:
            return {}
        
        latest = self.resource_history[-1]
        
        # 최근 5분간의 평균
        five_minutes_ago = time.time() - 300
        recent_data = [r for r in self.resource_history if r['timestamp'] > five_minutes_ago]
        
        if recent_data:
            avg_cpu = sum(r['cpu_percent'] for r in recent_data) / len(recent_data)
            avg_memory = sum(r['memory_percent'] for r in recent_data) / len(recent_data)
        else:
            avg_cpu = latest['cpu_percent']
            avg_memory = latest['memory_percent']
        
        return {
            'current': latest,
            'averages_5min': {
                'cpu_percent': avg_cpu,
                'memory_percent': avg_memory
            },
            'alerts': self.alerts[-10:],  # 최근 10개 경고
            'alert_count': len(self.alerts)
        }


# 전역 모니터 인스턴스
batch_monitor = BatchMonitor()
system_monitor = SystemResourceMonitor()

# 편의 함수들
def start_monitoring():
    """배치 모니터링 시작"""
    batch_monitor.start_monitoring()
    system_monitor.start_monitoring()

async def stop_monitoring():
    """배치 모니터링 중지"""
    system_monitor.stop_monitoring()
    return await batch_monitor.stop_monitoring()

def record_client_success(client_id: int, processing_time: float):
    """클라이언트 성공 기록"""
    batch_monitor.record_client_success(client_id, processing_time)

def record_client_error(client_id: int, error: Exception, processing_time: float):
    """클라이언트 에러 기록"""
    batch_monitor.record_client_error(client_id, error, processing_time)

def get_current_status():
    """현재 상태 조회"""
    return batch_monitor.get_current_status()

def get_system_status():
    """시스템 상태 조회"""
    return system_monitor.get_current_status()

# 데코레이터 함수
def monitor_function(func):
    """함수 실행 시간 모니터링 데코레이터"""
    def wrapper(*args, **kwargs):
        start_time = time.time()
        try:
            result = func(*args, **kwargs)
            duration = time.time() - start_time
            logger.info(f"{func.__name__} 실행 시간: {duration:.3f}초")
            return result
        except Exception as e:
            duration = time.time() - start_time
            logger.error(f"{func.__name__} 실행 실패 ({duration:.3f}초): {str(e)}")
            raise
    return wrapper

async def monitor_async_function(func):
    """비동기 함수 실행 시간 모니터링 데코레이터"""
    start_time = time.time()
    try:
        result = await func
        duration = time.time() - start_time
        logger.info(f"비동기 함수 실행 시간: {duration:.3f}초")
        return result
    except Exception as e:
        duration = time.time() - start_time
        logger.error(f"비동기 함수 실행 실패 ({duration:.3f}초): {str(e)}")
        raise