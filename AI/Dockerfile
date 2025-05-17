FROM python:3.9-slim AS trace-deps

WORKDIR /app
COPY requirements.txt .

# pip 최신화 + 분석툴 설치
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* \
 && pip install --no-cache-dir --upgrade pip pipdeptree

# dry-run 으로 설치 시뮬레이션 (다운로드만 하고 설치 X)
RUN pip install --dry-run --report deps.json -r requirements.txt

# psycopg2를 요구하는 패키지 찾기
RUN apt-get update && apt-get install -y jq && rm -rf /var/lib/apt/lists/* \
 && cat deps.json | jq '.. | objects | select(.name? == "psycopg2") | .requested'
