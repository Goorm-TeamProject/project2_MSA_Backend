# ─────────────────────────────────────────────────────────────────────────────
# 1) 빌더 스테이지: gateway-service 모듈만 Gradle로 빌드
# ─────────────────────────────────────────────────────────────────────────────
FROM gradle:8.4-jdk17 AS builder
WORKDIR /workspace

# 전체 프로젝트 복사 (settings.gradle, gradlew, 공통 라이브러리, gateway-service 등)
COPY . .

# 테스트는 건너뛰고 gateway-service:bootJar 태스크만 실행
RUN gradle :gateway-service:clean :gateway-service:bootJar -x test --no-daemon

# ─────────────────────────────────────────────────────────────────────────────
# 2) 런타임 스테이지: 빌드한 fat-jar를 최소 이미지로 패키징
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌더에서 생성된 JAR만 복사
COPY --from=builder /workspace/gateway-service/build/libs/*.jar app.jar

# 보안용 사용자/그룹 생성 및 퍼미션 설정
RUN addgroup --system appgroup \
 && adduser  --system --ingroup appgroup appuser \
 && chown appuser:appgroup /app/app.jar \
 && chmod 400           /app/app.jar

USER appuser

EXPOSE 8080

# prod 프로파일을 강제 활성화해서 application-prod.yml이 적용되도록
ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","/app/app.jar"]
