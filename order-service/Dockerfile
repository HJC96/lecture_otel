FROM eclipse-temurin:17-jdk-jammy

# OpenTelemetry Java Agent 다운로드
RUN apt-get update && apt-get install -y wget && \
    wget -O /opt/opentelemetry-javaagent.jar \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

WORKDIR /app

# Gradle wrapper와 build 파일들 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN chmod +x ./gradlew && ./gradlew build -x test

# 빌드된 JAR 파일 복사
RUN cp build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8082

# 애플리케이션 실행 (OpenTelemetry 활성화)
CMD ["java", "-javaagent:/opt/opentelemetry-javaagent.jar", "-jar", "app.jar"]
