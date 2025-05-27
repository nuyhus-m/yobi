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
    pip install --no-cache-dir transformers==4.51.3 peft && \
    # 2025년 5월 기준 최신 CUDA 12.1 지원 PyTorch 설치
    pip uninstall -y torch torchvision && \
    pip install torch==2.2.0 torchvision==0.17.0 --index-url https://download.pytorch.org/whl/cu121

# 3) 앱 코드
COPY . /app/

# 4) 모델 경로(선택)
VOLUME ["/srv/models"]

# 5) 크론 & 엔트리포인트 - 수정된 부분: 요일 사이에 공백 추가
RUN mkdir -p /app/logs && \
    echo "27 23 * * 0 cd /app && python -m batch.batch_runner --mode=run_now >> /app/logs/batch.log 2>&1" | crontab -

# GPU 체크 스크립트 추가
RUN echo "#!/bin/bash\npython -c \"import torch; print('CUDA 가능:', torch.cuda.is_available()); print('GPU 개수:', torch.cuda.device_count()); print('GPU 정보:', torch.cuda.get_device_name(0) if torch.cuda.is_available() else 'No GPU')\"" > /app/check_gpu.sh && \
    chmod +x /app/check_gpu.sh

ENV PYTHONPATH=/app
CMD ["bash","-c","service cron start && uvicorn main:app --host 0.0.0.0 --port 6000"]
