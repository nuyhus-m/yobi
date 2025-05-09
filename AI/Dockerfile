# 1. Python 이미지를 기반으로 시작
FROM python:3.9-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. PostgreSQL 개발 라이브러리 설치
RUN apt-get update && \
    apt-get install -y postgresql-client libpq-dev gcc && \
    rm -rf /var/lib/apt/lists/*

# 4. requirements.txt 파일 복사
COPY requirements.txt /app/

# 5. 의존성 설치
RUN pip install --no-cache-dir -r requirements.txt

# 6. app 디렉토리 복사
COPY app /app/app/

# 7. main.py 복사
COPY main.py /app/

# 8. FastAPI 앱 실행 (포트를 6000으로 변경)
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "6000"] 
