# 🤖 내 손 안의 요양 비서, 요비 (YOBI)

> 요양보호사를 위한 디지털 돌봄 기록 솔루션

---

## 📌 프로젝트 개요

- **프로젝트 명:** 내 손 안의 요양 비서, 요비
- **작성일:** 2025.05.19
- **작성자:** 이호정

요비는 요양보호사가 어르신을 돌보는 과정에서 발생하는 정보 혼선, 주관적인 기록, 비효율적인 업무 환경을 개선하기 위해 디지털화, 자동화된 돌봄 기록 시스템을 제공합니다.

---

## 📦 프로젝트 범위

### ✅ 포함 기능
- 음성 기반 자동 일지 작성
- 건강 모니터링 대시보드
- AI 기반 주간 리포트 자동 생성
- 일정 OCR 기능

### ❌ 제외 기능
- 어르신의 건강 데이터 수집 하드웨어 개발
- 실시간 영상 모니터링 기능

---

## 🗓️ 일정 계획

| 단계           | 기간                  | 주요 활동                     |
|----------------|-----------------------|-------------------------------|
| 기획           | 2025.04.07~04.20      | 요구사항 도출, 서비스 컨셉 정의 |
| 디자인 및 설계 | 2025.04.21~05.02      | UI/UX 설계 및 프로토타입 작성 |
| 개발           | 2025.04.28~05.19      | 앱 및 서버 개발               |
| 테스트         | 2025.05.18~05.21      | 기능 테스트 및 오류 수정     |
| 배포           | 2025.05.22            | 최종 릴리즈 및 결과 보고     |

---

## 📋 요구사항 명세

### 기능 요구사항
- 일정표 인식 기능 (OCR)
- 건강 데이터 대시보드
- 음성 → 텍스트 일지 작성
- AI 요약 리포트 생성
- 비속어 및 사견 필터링

### 비기능 요구사항
- 음성 인식 정확도 > 95%
- 서버 응답 시간 < 2초
- 데이터 보안 및 개인정보 보호 조치

---

## 🧠 문제 인식

- 보호사별 어르신 배치에 따라 **정보 혼선 발생**
- **객관적인 데이터 없이 일지를 작성**해야 하는 불편함
- 주관적이고 일관되지 않은 기록으로 인한 **신뢰성과 효율성 저하**

---

## 🎯 해결 방향 및 목표

1. **건강 모니터링 대시보드** 제공  
   → 혈압, 체온, 혈당 등의 데이터 시각화

2. **음성 기반 자동 일지 작성**  
   → 핵심 정보만 추출, 자동 텍스트화

3. **AI 주간 리포트 자동 생성**  
   → 관리자 보고용 문서로 활용 가능

---

## 🔍 차별성

### 기존 요양 앱의 한계
- 대부분 일자리 연결 중심
- **실제 업무 보조 기능 부재**

### 요비(YOBI)의 특징
- OCR로 일정 자동 등록
- 음성 기반 일지 + 필터링
- 건강 대시보드 + AI 리포트 통합 제공

---

## 🧩 핵심 기능

### 🗓️ 일정표 OCR 기능
- 수기 일정표를 사진으로 촬영 → 자동 등록
- 시각화된 달력 UI로 스케줄 관리 가능

### 📊 건강 모니터링 대시보드
- 혈압, 혈당, 체온 등 건강 정보 시각화
- 일간/주간 단위 변화 추이 확인 가능
- **보호사 중심의 직관적 정보 전달**

### 🧠 AI 주간 리포트 자동 생성
- 건강 변화 + 활동 기록 자동 요약
- 특이사항 포함된 리포트로 관리자 보고 가능

### 🎤 음성 기반 일지 작성
- 말한 내용을 자동 텍스트화
- 날짜별/어르신별 정리
- 사견 및 비속어 자동 정제

### 🔍 OpenAI 기반 일지 요약
- 일별 기록을 AI가 요약 → 주간 단위 보고서 생성
- 보호사는 별도 수작업 없이 **케어 전반을 파악 가능**

---

## 🎯 기대 효과

- **신뢰도 높은 기록 기반 돌봄 실현**
- **업무 자동화로 보호사의 돌봄 집중도 향상**
- **데이터 기반의 보고 시스템 도입으로 관리자 판단 보조**

---


# 기술 스택

### Android
<!-- Programming Languages & Mobile -->
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android%20Studio-346AC1?style=for-the-badge&logo=android-studio&logoColor=white)
![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)


### BackEnd
<!-- Backend Frameworks -->
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring JPA](https://img.shields.io/badge/Spring%20JPA-59666C?style=for-the-badge&logo=spring&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white)


### AI
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![PyTorch](https://img.shields.io/badge/PyTorch-EE4C2C?style=for-the-badge&logo=pytorch&logoColor=white)
![TensorFlow](https://img.shields.io/badge/TensorFlow-FF6F00?style=for-the-badge&logo=tensorflow&logoColor=white)
![Mistral](https://img.shields.io/badge/Mistral_AI-000000?style=for-the-badge&logo=apacheairflow&logoColor=white)
![LoRA](https://img.shields.io/badge/LoRA-7952B3?style=for-the-badge&logo=ai&logoColor=white)


### Database
<!-- Database -->
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)


### DevOps
<!-- DevOps -->
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Docker Compose](https://img.shields.io/badge/Docker%20Compose-0899CD?style=for-the-badge&logo=docker&logoColor=white)
![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white)
![AWS EC2](https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazon-aws&logoColor=white)

### Tools
<!-- Tools -->
![GitLab](https://img.shields.io/badge/GitLab-FC6D26?style=for-the-badge&logo=gitlab&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Jira](https://img.shields.io/badge/Jira-0052CC?style=for-the-badge&logo=jira&logoColor=white)

