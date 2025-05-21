<div align="center"> <h1>🤖 내 손 안의 요양 비서, 요비 (YOBI)</h1> <p>요양보호사를 위한 디지털 돌봄 기록 솔루션</p> </div> <br/> <div align="center"> <!-- 프로젝트 대표 이미지 --> <img src="./assets/image/yobi_intro.jpg" alt="YOBI Main" style="border-radius: 5px;"/> </div> <br/> <div align="center"> <a href="#">Notion</a> &nbsp;|&nbsp; <a href="#">기능 명세서</a> &nbsp;|&nbsp; <a href="#">API 문서</a> </div>
✍️ 프로젝트 개요
프로젝트명: 내 손 안의 요양 비서, 요비 (YOBI)

프로젝트 기간: 2025.04.07 ~ 2025.05.22

작성자: 이호정

🧠 프로젝트 소개
프로젝트 배경
요양보호사들이 어르신을 돌보는 과정에서 정보 혼선, 비효율적인 기록 방식, 주관적이고 일관성 없는 데이터 작성 등의 문제에 직면하고 있습니다.
요비(YOBI)는 이러한 문제를 해결하기 위해 디지털화된 자동 돌봄 기록 시스템을 제공합니다.

문제점 해결
음성 기반 일지 자동 작성을 통해 보호사의 수고 절감

건강 데이터 시각화로 보호사 및 관리자에게 직관적인 정보 제공

AI 주간 리포트 자동 생성으로 관리자 보고 간소화

비속어 및 사견 필터링으로 기록 신뢰도 확보

🚀 프로젝트 목표
건강 모니터링 대시보드 구축
→ 어르신 건강정보(혈압, 체온, 혈당 등)를 시각화하여 관리 용이

음성 기반 자동 일지 작성 기능 제공
→ 음성을 텍스트화, 핵심 정보만 추출하여 자동 정리

AI 기반 리포트 자동 생성
→ 수집된 일지와 건강데이터를 요약해 관리자 보고서로 활용

📦 주요 기능
1️⃣ 일정표 OCR 기능
수기 일정표를 사진으로 촬영하면 자동 등록

시각화된 UI로 스케줄을 한눈에 확인 가능

2️⃣ 건강 모니터링 대시보드
혈압, 혈당, 체온 등 건강 정보 실시간 시각화

보호사 중심으로 간결하고 직관적인 구성

3️⃣ AI 주간 리포트 자동 생성
AI가 활동 및 건강 변화를 자동 요약

관리자에게 보고할 수 있는 PDF 문서 형태 제공

4️⃣ 음성 기반 일지 작성
보호사가 말한 내용을 자동 텍스트로 변환

날짜별/어르신별로 정리되며 비속어 자동 필터링

5️⃣ OpenAI 기반 일지 요약
매일 작성된 기록을 AI가 요약해 주간 리포트로 변환

보호사는 전체 케어 흐름을 빠르게 파악 가능

📊 기대 효과
보호사 측
반복되는 문서 작업 감소 → 돌봄 집중도 향상

음성 기록 기반 자동화 → 업무 효율 개선

데이터 기반 대시보드 → 객관적 정보 전달 가능

관리자 측
정량적 기록과 AI 리포트 → 의사결정 효율화

일지 신뢰도 향상 → 서비스 품질 제고

⏱️ 프로젝트 일정
단계	기간	주요 활동
기획	2025.04.07~04.20	요구사항 도출, 서비스 컨셉 정의
디자인 및 설계	2025.04.21~05.02	UI/UX 설계, 프로토타입 작성
개발	2025.04.28~05.19	앱 및 서버 개발
테스트	2025.05.18~05.21	기능 테스트 및 오류 수정
배포	2025.05.22	최종 릴리즈 및 결과 보고

🧑‍💻 기술 스택
<table> <thead> <tr> <th>분류</th> <th>기술 스택</th> </tr> </thead> <tbody> <tr> <td>안드로이드</td> <td> <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=Kotlin&logoColor=white"/> <img src="https://img.shields.io/badge/Android%20Studio-3DDC84?style=flat&logo=android-studio&logoColor=white"/> </td> </tr> <tr> <td>백엔드</td> <td> <img src="https://img.shields.io/badge/Java-007396?style=flat&logo=java&logoColor=white"/> <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat&logo=spring-boot&logoColor=white"/> <img src="https://img.shields.io/badge/Spring_JPA-59666C?style=flat&logo=spring&logoColor=white"/> <img src="https://img.shields.io/badge/FastAPI-009688?style=flat&logo=fastapi&logoColor=white"/> </td> </tr> <tr> <td>AI</td> <td> <img src="https://img.shields.io/badge/Python-3776AB?style=flat&logo=python&logoColor=white"/> <img src="https://img.shields.io/badge/PyTorch-EE4C2C?style=flat&logo=pytorch&logoColor=white"/> <img src="https://img.shields.io/badge/TensorFlow-FF6F00?style=flat&logo=tensorflow&logoColor=white"/> <img src="https://img.shields.io/badge/Mistral_AI-000000?style=flat&logo=apacheairflow&logoColor=white"/> <img src="https://img.shields.io/badge/LoRA-7952B3?style=flat&logo=ai&logoColor=white"/> </td> </tr> <tr> <td>데이터베이스</td> <td> <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=flat&logo=postgresql&logoColor=white"/> <img src="https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white"/> </td> </tr> <tr> <td>DevOps</td> <td> <img src="https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white"/> <img src="https://img.shields.io/badge/Docker_Compose-0899CD?style=flat&logo=docker&logoColor=white"/> <img src="https://img.shields.io/badge/Jenkins-D24939?style=flat&logo=jenkins&logoColor=white"/> <img src="https://img.shields.io/badge/AWS_EC2-FF9900?style=flat&logo=amazon-aws&logoColor=white"/> </td> </tr> <tr> <td>협업 도구</td> <td> <img src="https://img.shields.io/badge/GitLab-FC6D26?style=flat&logo=gitlab&logoColor=white"/> <img src="https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white"/> <img src="https://img.shields.io/badge/Notion-000000?style=flat&logo=notion&logoColor=white"/> <img src="https://img.shields.io/badge/Jira-0052CC?style=flat&logo=jira&logoColor=white"/> </td> </tr> </tbody> </table>
