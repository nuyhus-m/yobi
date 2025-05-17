FROM python:3.9-slim
WORKDIR /app
ENV DEBIAN_FRONTEND=noninteractive

# 1) 시스템 패키지
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        build-essential \          # gcc, g++, libc6-dev 포함
        libpq-dev \                # PostgreSQL client headers
        postgresql-client \
        curl \
        cron && \
    rm -rf /var/lib/apt/lists/*

# 2) Python 의존성
COPY requirements.txt /tmp/requirements.txt
RUN set -eux; \
    # 2-1) psycopg2 줄 제거
    grep -vE '^psycopg2(==.*)?$' /tmp/requirements.txt > /tmp/req.clean.txt && \
    # 2-2) psycopg2-binary만 설치
    pip install --no-cache-dir psycopg2-binary==2.9.10 && \
    # 2-3) 나머지 의존성
    pip install --no-cache-dir -r /tmp/req.clean.txt && \
    # 2-4) 설치 확인
    pip install --no-cache-dir pipdeptree && \
    echo "=== psycopg2 installs ===" && pipdeptree | grep psycopg2 || true

# 3) 앱 코드
COPY . /app/

# 4) 모델 경로(선택)
VOLUME ["/srv/models"]

# 5) 크론 & 엔트리포인트
RUN mkdir -p /app/logs && \
    echo "59 15 * * 5 cd /app && python -m batch.batch_runner >> /app/logs/batch.log 2>&1" | crontab -

ENV PYTHONPATH=/app
CMD ["bash","-c","service cron start && uvicorn main:app --host 0.0.0.0 --port 6000"]
