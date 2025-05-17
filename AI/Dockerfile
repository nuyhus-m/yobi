# requirements 분석용 임시 이미지
FROM python:3.9-slim AS deps-inspect

WORKDIR /app
COPY requirements.txt .

# ① pip 최신화 + pipdeptree 설치
RUN pip install --no-cache-dir --upgrade pip \
 && pip install --no-cache-dir pipdeptree

# ② dry-run 으로 dependency 트리 확인
#    --report 파일에 JSON 트리가 생깁니다.
RUN pip install --dry-run --report deps.json -r requirements.txt
