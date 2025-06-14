# 1) Builder 단계: 멀티모듈 환경에서 ac-service 모듈만 빌드
FROM gradle:8.4-jdk17 AS builder
WORKDIR /app

# 루트 Gradle 설정 복사
COPY settings.gradle settings.gradle
COPY build.gradle    build.gradle

# 공통 라이브러리(필요하다면)
COPY common-lib      common-lib

# ac-service 모듈만 복사
COPY account-service      account-service

# ac-service 모듈만 bootJar 태스크로 빌드 (테스트 생략)
RUN gradle clean :account-service:bootJar --no-daemon -x test

# 2) Run 단계: 경량 JRE 이미지에 복사해서 실행
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /app/account-service/build/libs/*.jar app.jar

# 권한 및 사용자 설정
RUN addgroup --system appgroup \
 && adduser  --system --ingroup appgroup appuser \
 && chown appuser:appgroup /app/app.jar \
 && chmod 400     /app/app.jar

USER appuser

# ac-service 기본 포트 (application.yml 에 맞춰 조정)
EXPOSE 8082

# 컨테이너 시작 커맨드
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
