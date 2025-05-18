FROM python:3.9-slim
WORKDIR /app
ENV DEBIAN_FRONTEND=noninteractive

# 1) 시스템 패키지
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        build-essential \
        libpq-dev \
        postgresql-client \
        curl \
        cron && \
    rm -rf /var/lib/apt/lists/*

# 2) Python 의존성
COPY requirements.txt /tmp/requirements.txt
RUN set -eux; \
    pip install --no-cache-dir -r /tmp/requirements.txt && \
    pip install --no-cache-dir psycopg2-binary==2.9.10 && \
    pip install --no-cache-dir transformers==4.51.3 peft

# 3) 앱 코드
COPY . /app/

# 4) 모델 경로(선택)
VOLUME ["/srv/models"]

# 5) 크론 & 엔트리포인트
네, 정확히 맞습니다! Dockerfile에서 crontab 설정 부분을 수정하면 됩니다. 일요일 23시에 실행되도록 다음과 같이 변경하세요:
dockerfileFROM python:3.9-slim
WORKDIR /app
ENV DEBIAN_FRONTEND=noninteractive

# 1) 시스템 패키지
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        build-essential \
        libpq-dev \
        postgresql-client \
        curl \
        cron && \
    rm -rf /var/lib/apt/lists/*

# 2) Python 의존성
COPY requirements.txt /tmp/requirements.txt
RUN set -eux; \
    pip install --no-cache-dir -r /tmp/requirements.txt && \
    pip install --no-cache-dir psycopg2-binary==2.9.10 && \
    pip install --no-cache-dir transformers==4.51.3 peft

# 3) 앱 코드
COPY . /app/

# 4) 모델 경로(선택)
VOLUME ["/srv/models"]

# 5) 크론 & 엔트리포인트
RUN mkdir -p /app/logs && \
    echo "0 23 * * 0 cd /app && python -m batch.batch_runner --mode=run_now >> /app/logs/batch.log 2>&1" | crontab -

ENV PYTHONPATH=/app
CMD ["bash","-c","service cron start && uvicorn main:app --host 0.0.0.0 --port 6000"]
