# # 여러 최적화 기법 적용
# FROM python:3.9-slim

# # 컨테이너 안에서의 작업 디렉토리 설정
# WORKDIR /app

# # 시스템 패키지 설치 및 캐시 정리를 한 번에
# RUN apt-get update && \
#     apt-get install -y --no-install-recommends \
#     postgresql-client \
#     libpq-dev \
#     gcc \
#     curl \
#     cron \
#     && rm -rf /var/lib/apt/lists/*  # apt 캐시 삭제

# # Python 의존성 설치 (캐시 없이)
# COPY requirements.txt /app/
# RUN pip install --no-cache-dir -r requirements.txt

# # 애플리케이션 코드 복사
# COPY . /app/

# # 로그 디렉토리 생성
# RUN mkdir -p /app/logs

# # crontab 설정 (배치 작업용)
# # 배치 스크립트 실행 시간을 UTC 기준으로 조정
# RUN echo "59 15 * * 5 cd /app && python -m batch.batch_runner >> /app/logs/batch.log 2>&1" | crontab -

# # 환경 변수 설정
# ENV PYTHONPATH=/app

# # 시작 스크립트
# RUN echo '#!/bin/bash\nservice cron start\nuvicorn main:app --host 0.0.0.0 --port 6000' > start.sh && chmod +x start.sh

# # 실행
# CMD ["./start.sh"]