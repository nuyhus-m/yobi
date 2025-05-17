# ──────────────────────────────
#  Python 3.9 slim 기반 이미지
# ──────────────────────────────
FROM python:3.9-slim

# 작업 디렉터리
WORKDIR /app

# ① 필수 시스템 패키지 설치
#    - build-essential + gcc        : C 컴파일러/툴체인
#    - libc6-dev                    : glibc 헤더(limits.h 포함)
#    - libpq-dev + postgresql-client: psycopg2 빌드 & psql 유틸
#    - curl, cron                   : 기타 유틸리티
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        build-essential \
        gcc \
        libc6-dev \
        libpq-dev \
        postgresql-client \
        curl \
        cron \
    && rm -rf /var/lib/apt/lists/*

# ② Python 의존성 + psycopg2 의존성 트레이스
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt && \
    pip install --no-cache-dir pipdeptree && \
    echo "====== psycopg2 dependency trace 시작 ======" && \
    pipdeptree | grep psycopg2 || echo "✅ psycopg2 dependency trace 끝 (없으면 문제없음)" && \
    echo "==========================================="

# ③ 애플리케이션 코드 복사
COPY . /app/

# ④ 모델 다운로드 (토큰·경로 환경변수로 제어)
ARG HF_TOKEN
RUN export HF_TOKEN=$HF_TOKEN && \
    export BASE_MODEL_PATH=/srv/models/base && \
    export ADAPTER_PATH=/srv/models/mistral_lora_adapter && \
    export HF_HOME=/srv/models/cache && \
    export TEST_MODEL_LOADING=true && \
    python3 app/ai_model/download_models.py || echo "⚠️ 모델 다운로드 실패 (이미 있거나 무시)"

# ⑤ 로그 디렉터리
RUN mkdir -p /app/logs

# ⑥ 주간 배치 작업 등록 (매주 금요일 15:59 UTC+0 ≒ 토요일 00:59 KST)
RUN echo "59 15 * * 5 cd /app && python -m batch.batch_runner >> /app/logs/batch.log 2>&1" | crontab -

# ⑦ 런타임 환경 변수
ENV PYTHONPATH=/app

# ⑧ 컨테이너 시작 스크립트
RUN echo '#!/bin/bash\nservice cron start\nuvicorn main:app --host 0.0.0.0 --port 6000' > start.sh && \
    chmod +x start.sh

# ⑨ 시작 명령
CMD ["./start.sh"]
