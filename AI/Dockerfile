FROM python:3.9-slim
WORKDIR /app
ENV DEBIAN_FRONTEND=noninteractive

# 1) 시스템 패키지 + NVIDIA CUDA 도구 설치
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        build-essential \
        libpq-dev \
        postgresql-client \
        curl \
        cron \
        wget \
        gnupg \
        software-properties-common && \
    # NVIDIA CUDA 저장소 추가
    wget -O /etc/apt/preferences.d/cuda-repository-pin-600 https://developer.download.nvidia.com/compute/cuda/repos/ubuntu2004/x86_64/cuda-ubuntu2004.pin && \
    apt-key adv --fetch-keys https://developer.download.nvidia.com/compute/cuda/repos/ubuntu2004/x86_64/7fa2af80.pub && \
    add-apt-repository "deb https://developer.download.nvidia.com/compute/cuda/repos/ubuntu2004/x86_64/ /" && \
    apt-get update && \
    # CUDA 런타임만 설치 (full toolkit은 너무 큼)
    apt-get install -y --no-install-recommends \
        cuda-runtime-11-8 \
        libcudnn8 && \
    # 정리
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# CUDA 환경 변수 설정
ENV PATH="/usr/local/cuda/bin:${PATH}"
ENV LD_LIBRARY_PATH="/usr/local/cuda/lib64:${LD_LIBRARY_PATH}"

# 2) Python 의존성
COPY requirements.txt /tmp/requirements.txt
RUN set -eux; \
    pip install --no-cache-dir -r /tmp/requirements.txt && \
    pip install --no-cache-dir psycopg2-binary==2.9.10 && \
    # transformers와 peft를 별도로 설치 (기존 유지)
    pip install --no-cache-dir transformers==4.51.3 peft && \
    # CUDA 지원 PyTorch 설치
    pip uninstall -y torch torchvision && \
    pip install torch torchvision --index-url https://download.pytorch.org/whl/cu118 && \
    # bitsandbytes GPU 버전 설치 (필요시)
    pip install -U bitsandbytes && \
    # accelerate 설치 (필요시)
    pip install -U accelerate

# 3) 앱 코드
COPY . /app/

# 4) 모델 경로(선택)
VOLUME ["/srv/models"]

# 5) 크론 & 엔트리포인트
RUN mkdir -p /app/logs && \
    echo "27 23 * *0 cd /app && python -m batch.batch_runner --mode=run_now >> /app/logs/batch.log 2>&1" | crontab -

# 6) GPU 인식 확인 스크립트 (빌드 시 검증용, 선택사항)
RUN echo "#!/bin/bash\npython -c \"import torch; print('CUDA 가능:', torch.cuda.is_available()); print('GPU 개수:', torch.cuda.device_count()); print('GPU 이름:', torch.cuda.get_device_name(0) if torch.cuda.is_available() else 'No GPU')\"" > /app/check_gpu.sh && \
    chmod +x /app/check_gpu.sh

ENV PYTHONPATH=/app
CMD ["bash","-c","service cron start && uvicorn main:app --host 0.0.0.0 --port 6000"]
