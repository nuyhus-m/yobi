# Python 3.9-slim 이미지 사용
FROM python:3.9-slim

# 작업 디렉토리 설정
WORKDIR /app

# 시스템 패키지 설치 및 캐시 정리
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    build-essential \
    gcc \
    libpq-dev \
    libc-dev \
    postgresql-client \
    curl \
    cron \
    && rm -rf /var/lib/apt/lists/*

# Python 의존성 설치 및 psycopg2 추적용 pipdeptree 추가
COPY requirements.txt /app/
RUN pip install --no-cache-dir -r requirements.txt && \
    pip install pipdeptree && \
    echo "====== psycopg2 dependency trace 시작 ======" && \
    pipdeptree | grep psycopg2 || echo "✅ psycopg2 dependency trace 끝 (없으면 문제없음)" && \
    echo "==========================================="

# 애플리케이션 코드 복사
COPY . /app/

# 모델 다운로드 (환경 변수 지정)
ARG HF_TOKEN
RUN export HF_TOKEN=$HF_TOKEN && \
    export BASE_MODEL_PATH=/srv/models/base && \
    export ADAPTER_PATH=/srv/models/mistral_lora_adapter && \
    export HF_HOME=/srv/models/cache && \
    export TEST_MODEL_LOADING=true && \
    python3 app/ai_model/download_models.py || echo "⚠️ 모델 다운로드 실패 (이미 있거나 무시)"

# 로그 디렉토리 생성
RUN mkdir -p /app/logs

# crontab 등록
RUN echo "59 15 * * 5 cd /app && python -m batch.batch_runner >> /app/logs/batch.log 2>&1" | crontab -

# 환경 변수 설정
ENV PYTHONPATH=/app

# 시작 스크립트 준비
RUN echo '#!/bin/bash\nservice cron start\nuvicorn main:app --host 0.0.0.0 --port 6000' > start.sh && chmod +x start.sh

# 컨테이너 시작 시 실행
CMD ["./start.sh"]
