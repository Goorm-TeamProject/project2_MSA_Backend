#빌드 단계
FROM gradle:8.4-jdk17 AS builder
WORKDIR /app

# Gradle 프로젝트 전체 복사
COPY . .

# 빌드 실행
RUN gradle clean build --no-daemon

# 실행 단계
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 권한 및 사용자 설정
RUN addgroup --system appgroup \
 && adduser --system --ingroup appgroup appuser \
 && chown appuser:appgroup /app/app.jar \
 && chmod 400 /app/app.jar

USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
