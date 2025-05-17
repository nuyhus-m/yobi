FROM python:3.9-slim
WORKDIR /app

# 1) 시스템 패키지
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      build-essential \
      gcc \
      libc6-dev \                # ← glibc headers (limits.h)
      libpq-dev \
      postgresql-client \
      curl \
      cron && \
    rm -rf /var/lib/apt/lists/*

# 2) Python 의존성 (+ psycopg2 trace는 유지)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt && \
    pip install --no-cache-dir pipdeptree && \
    echo "=== psycopg2 dependency trace ===" && \
    pipdeptree | grep psycopg2 || echo "✅ none"

# 3) 앱 코드
COPY . /app/

# (★ 모델 다운로드 레이어 제거)

# 선택) 모델 경로 볼륨
VOLUME ["/srv/models"]

# 4) 로그·크론
RUN mkdir -p /app/logs && \
    echo "59 15 * * 5 cd /app && python -m batch.batch_runner >> /app/logs/batch.log 2>&1" | crontab -

ENV PYTHONPATH=/app

RUN echo '#!/bin/bash\nservice cron start\nuvicorn main:app --host 0.0.0.0 --port 6000' > /start.sh \
 && chmod +x /start.sh
CMD ["/start.sh"]
