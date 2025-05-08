# 1. Python 이미지를 기반으로 시작
FROM python:3.9-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. requirements.txt 파일 복사
COPY requirements.txt /app/

# 4. 의존성 설치
RUN pip install --no-cache-dir -r requirements.txt

# 5. .env 파일 복사
COPY .env /app/

# 6. app 디렉토리 복사
COPY app /app/app/

# 7. main.py 복사
COPY main.py /app/

# 8. FastAPI 앱 실행 (포트를 6000으로 변경)
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "6000"] 