# fast API로 구성한 OCR 입니다.

## 구동방법

### 가상환경 구성 및 활성화

python -m venv venv
source venv/Scripts/activate
pip install -r requirements.txt

### fastAPI 실행

uvicorn main:app --reload
