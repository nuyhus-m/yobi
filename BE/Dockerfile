# 빌드 단계
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# 실행 단계
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일을 복사
COPY --from=build /app/build/libs/*.jar app.jar

# application-prod.yml 파일을 직접 복사
COPY ./src/main/resources/application-prod.yml /app/resources/

# 환경 변수 설정 (docker-compose.yml에서 주입되므로 기본값만 설정)
ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=Asia/Seoul
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENV REDIS_PASSWORD=redis

# 애플리케이션이 사용할 포트 (추후 가릴 예정)
EXPOSE 8080

# 애플리케이션 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]