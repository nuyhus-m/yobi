# 빌드 단계
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Gradle 파일 복사
COPY build.gradle settings.gradle ./
COPY src ./src

# Gradle 빌드 실행
RUN gradle clean bootJar --no-daemon

# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일을 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 환경 변수 설정
ENV TZ=Asia/Seoul

# 애플리케이션이 사용할 포트
EXPOSE 8080

# 애플리케이션 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]