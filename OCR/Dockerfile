# 1. Python 이미지를 기반으로 시작
FROM python:3.9-slim

# 2. 시스템 의존성 설치
RUN apt-get update && apt-get install -y \
    gcc \
    postgresql-client \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# 3. 비루트 사용자 생성
RUN useradd -m -u 1000 appuser

# 4. 작업 디렉토리 설정
WORKDIR /app

# 5. requirements.txt 파일 복사
COPY requirements.txt /app/

# 6. 의존성 설치
RUN pip install --no-cache-dir -r requirements.txt

# 7. .env 파일 복사
# COPY .env /app/

# 8. 애플리케이션 코드 복사
COPY . /app/

# 9. 권한 설정
RUN chown -R appuser:appuser /app

# 10. 비루트 사용자로 전환 !
USER appuser

# 11. FastAPI 앱 실행
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
