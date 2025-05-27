# batch/utils/notification.py
"""
알림 기능 모듈
"""

import asyncio
import aiohttp
import json
import logging
from typing import Dict, List, Optional, Union
from datetime import datetime
from enum import Enum

logger = logging.getLogger(__name__)

class NotificationType(Enum):
    """알림 타입"""
    INFO = "info"
    SUCCESS = "success"
    WARNING = "warning"
    ERROR = "error"

class NotificationService:
    """통합 알림 서비스"""
    
    def __init__(self):
        self.slack_webhook_url = None
        self.email_config = None
        self.webhook_urls = {}
        
    def configure_slack(self, webhook_url: str, channel: str = None):
        """Slack 설정"""
        self.slack_webhook_url = webhook_url
        self.slack_channel = channel
        logger.info("Slack 알림이 설정되었습니다")
        
    def configure_email(self, smtp_server: str, port: int, username: str, password: str, 
                       sender_email: str, recipient_emails: List[str]):
        """이메일 설정"""
        self.email_config = {
            'smtp_server': smtp_server,
            'port': port,
            'username': username,
            'password': password,
            'sender_email': sender_email,
            'recipient_emails': recipient_emails
        }
        logger.info("이메일 알림이 설정되었습니다")
    
    def add_webhook(self, name: str, url: str):
        """커스텀 웹훅 추가"""
        self.webhook_urls[name] = url
        logger.info(f"웹훅 '{name}'이 추가되었습니다")
    
    async def send_batch_completion(self, result: Dict):
        """배치 완료 알림"""
        success_rate = result.get('success_rate', 0)
        
        # 상태에 따른 타입 결정
        if result.get('error_count', 0) == 0:
            notification_type = NotificationType.SUCCESS
        elif success_rate >= 80:
            notification_type = NotificationType.WARNING
        else:
            notification_type = NotificationType.ERROR
        
        title = "🎉 배치 작업 완료" if notification_type == NotificationType.SUCCESS else "⚠️ 배치 작업 완료 (일부 실패)"
        
        # 상세 정보 구성
        details = {
            "총 클라이언트": result.get('total_clients', 0),
            "성공": result.get('success_count', 0),
            "실패": result.get('error_count', 0),
            "성공률": f"{success_rate:.1f}%",
            "총 소요시간": f"{result.get('total_duration', 0):.2f}초",
            "평균 처리시간": f"{result.get('average_time_per_client', 0):.2f}초/클라이언트"
        }
        
        # 에러 상세 정보
        error_details = None
        if result.get('errors'):
            error_list = [f"클라이언트 {err['client_id']}: {err['error']}" 
                         for err in result['errors'][:3]]
            if len(result['errors']) > 3:
                error_list.append(f"... 및 {len(result['errors']) - 3}개 더")
            error_details = "\n".join(error_list)
        
        # 알림 발송
        await self._send_notification(
            title=title,
            message="주간 리포트 배치 작업이 완료되었습니다.",
            details=details,
            error_details=error_details,
            notification_type=notification_type
        )
    
    async def send_batch_error(self, error: Exception, context: Dict = None):
        """배치 에러 알림"""
        title = "🚨 배치 작업 실패"
        message = f"주간 리포트 배치 작업 중 오류가 발생했습니다."
        
        details = {
            "에러 타입": type(error).__name__,
            "에러 메시지": str(error),
            "발생 시간": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        }
        
        if context:
            details.update(context)
        
        await self._send_notification(
            title=title,
            message=message,
            details=details,
            notification_type=NotificationType.ERROR
        )
    
    async def send_batch_start(self, client_count: int):
        """배치 시작 알림"""
        title = "🚀 배치 작업 시작"
        message = f"{client_count}개 클라이언트의 주간 리포트 생성을 시작합니다."
        
        details = {
            "대상 클라이언트 수": client_count,
            "시작 시간": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        }
        
        await self._send_notification(
            title=title,
            message=message,
            details=details,
            notification_type=NotificationType.INFO
        )
    
    async def send_custom_notification(self, title: str, message: str, 
                                     details: Dict = None, 
                                     notification_type: NotificationType = NotificationType.INFO):
        """커스텀 알림"""
        await self._send_notification(title, message, details, notification_type=notification_type)
    
    async def _send_notification(self, title: str, message: str, 
                               details: Dict = None, error_details: str = None,
                               notification_type: NotificationType = NotificationType.INFO):
        """실제 알림 발송"""
        # Slack 알림
        if self.slack_webhook_url:
            await self._send_slack_notification(title, message, details, error_details, notification_type)
        
        # 이메일 알림
        if self.email_config:
            await self._send_email_notification(title, message, details, error_details)
        
        # 커스텀 웹훅
        for name, url in self.webhook_urls.items():
            await self._send_webhook_notification(url, title, message, details, error_details)
    
    async def _send_slack_notification(self, title: str, message: str, 
                                     details: Dict = None, error_details: str = None,
                                     notification_type: NotificationType = NotificationType.INFO):
        """Slack 알림 발송"""
        try:
            # 색상 설정
            color_map = {
                NotificationType.INFO: "info",
                NotificationType.SUCCESS: "good",
                NotificationType.WARNING: "warning",
                NotificationType.ERROR: "danger"
            }
            
            payload = {
                "text": title,
                "attachments": [
                    {
                        "color": color_map[notification_type],
                        "text": message,
                        "fields": [],
                        "footer": f"Health Report Batch | {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}"
                    }
                ]
            }
            
            # 채널 설정
            if hasattr(self, 'slack_channel') and self.slack_channel:
                payload["channel"] = self.slack_channel
            
            # 상세 정보 추가
            if details:
                for key, value in details.items():
                    payload["attachments"][0]["fields"].append({
                        "title": key,
                        "value": str(value),
                        "short": True
                    })
            
            # 에러 상세 정보 추가
            if error_details:
                payload["attachments"][0]["fields"].append({
                    "title": "에러 상세",
                    "value": error_details,
                    "short": False
                })
            
            async with aiohttp.ClientSession() as session:
                async with session.post(self.slack_webhook_url, json=payload, timeout=10) as response:
                    if response.status == 200:
                        logger.info("Slack 알림 발송 성공")
                    else:
                        logger.error(f"Slack 알림 발송 실패: HTTP {response.status}")
                        
        except Exception as e:
            logger.error(f"Slack 알림 발송 중 오류: {str(e)}")
    
    async def _send_email_notification(self, title: str, message: str, 
                                     details: Dict = None, error_details: str = None):
        """이메일 알림 발송"""
        try:
            import aiosmtplib
            from email.mime.text import MIMEText
            from email.mime.multipart import MIMEMultipart
            
            # 이메일 메시지 구성
            msg = MIMEMultipart()
            msg['From'] = self.email_config['sender_email']
            msg['To'] = ', '.join(self.email_config['recipient_emails'])
            msg['Subject'] = title
            
            # HTML 본문 구성
            html_body = f"""
            <html>
                <body>
                    <h2>{title}</h2>
                    <p>{message}</p>
            """
            
            if details:
                html_body += "<table border='1' style='border-collapse: collapse;'>"
                for key, value in details.items():
                    html_body += f"<tr><td><strong>{key}</strong></td><td>{value}</td></tr>"
                html_body += "</table>"
            
            if error_details:
                html_body += f"<h3>에러 상세</h3><pre>{error_details}</pre>"
            
            html_body += """
                </body>
            </html>
            """
            
            msg.attach(MIMEText(html_body, 'html', 'utf-8'))
            
            # 이메일 발송
            await aiosmtplib.send(
                msg,
                hostname=self.email_config['smtp_server'],
                port=self.email_config['port'],
                start_tls=True,
                username=self.email_config['username'],
                password=self.email_config['password']
            )
            
            logger.info("이메일 알림 발송 성공")
            
        except Exception as e:
            logger.error(f"이메일 알림 발송 중 오류: {str(e)}")
    
    async def _send_webhook_notification(self, url: str, title: str, message: str, 
                                       details: Dict = None, error_details: str = None):
        """일반 웹훅 알림 발송"""
        try:
            payload = {
                "title": title,
                "message": message,
                "timestamp": datetime.now().isoformat(),
                "details": details or {},
                "error_details": error_details
            }
            
            async with aiohttp.ClientSession() as session:
                async with session.post(url, json=payload, timeout=10) as response:
                    if response.status == 200:
                        logger.info(f"웹훅 알림 발송 성공: {url}")
                    else:
                        logger.error(f"웹훅 알림 발송 실패: {url}, HTTP {response.status}")
                        
        except Exception as e:
            logger.error(f"웹훅 알림 발송 중 오류 ({url}): {str(e)}")


# 전역 알림 서비스 인스턴스
notification_service = NotificationService()

# 편의 함수들
async def send_batch_completion(result: Dict):
    """배치 완료 알림 전송"""
    await notification_service.send_batch_completion(result)

async def send_batch_error(error: Exception, context: Dict = None):
    """배치 에러 알림 전송"""
    await notification_service.send_batch_error(error, context)

async def send_batch_start(client_count: int):
    """배치 시작 알림 전송"""
    await notification_service.send_batch_start(client_count)