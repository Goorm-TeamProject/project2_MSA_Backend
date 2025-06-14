# 1) Builder 단계
FROM gradle:8.4-jdk17 AS builder
WORKDIR /app

# 루트 Gradle 설정 복사
COPY settings.gradle settings.gradle
COPY build.gradle    build.gradle
COPY common-lib      common-lib
COPY user-service    user-service

# user-service 모듈만 빌드 (테스트 제외)
RUN gradle clean :user-service:bootJar --no-daemon -x test

# 2) Run 단계
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드된 user-service JAR 복사 (builder 스테이지에서 가져옴)
COPY --from=builder /app/user-service/build/libs/*.jar app.jar

# prod, 기본 yml 파일도 복사 (로컬 경로 기준!)
COPY user-service/src/main/resources/application-prod.yml application-prod.yml
COPY user-service/src/main/resources/application.yml application.yml

# 권한 및 사용자 설정
RUN addgroup --system appgroup \
 && adduser  --system --ingroup appgroup appuser \
 && chown appuser:appgroup /app/app.jar \
 && chmod 400     /app/app.jar

USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.config.location=file:/app/application-prod.yml", "--spring.profiles.active=prod"]
