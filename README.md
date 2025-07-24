# Kubernetes 기반 MSA 프로젝트

# 프로젝트 개요
이 프로젝트는 **AWS EKS 기반의 MSA 환경**에서 **GitOps**, **IaC**, **자동 복구**, **리소스 최적화** 등 실무형 클라우드 운영 전략을 직접 설계하고 구현한 결과물입니다.  
목표는 **운영 효율성과 확장성, 보안, 복원력**을 갖춘 인프라를 구축하고 실제로 **프로덕션 환경 수준의 트래픽 및 장애 시나리오**에 대응하는 것입니다.

## 주요 기능 및 구현 요소

### 아키텍처 특징
- **모듈화된 Helm Chart**로 서비스 단위 배포
- **Argo CD 기반 GitOps 파이프라인** 구축
- **Terraform + Helm을 활용한 IaC**
- **멀티 노드 EKS 클러스터 구성** 및 **노드그룹 자동 확장(Auto Scaling)**
- **ALB + Ingress Controller + Spring Cloud Gateway** 기반 서비스 메시
- **Redis 기반 JWT 토큰 블랙리스트 및 인증 구조**
- **S3 기반 Velero 백업 및 재해 복구 시나리오 적용**
- **SQS + Lambda 기반 로그 전송 및 처리 자동화**
- **CloudWatch Logs, AWS Budgets, Trusted Advisor를 통한 비용/모니터링 관리**


## 기술 스택

| 영역            | 기술 |
|-----------------|------|
| 인프라          | AWS EKS, EC2, ALB, Route 53, IAM, S3, CloudWatch, Budgets |
| IaC / GitOps    | Terraform, Helm, Argo CD |
| 백엔드          | Spring Boot (Java 21), Spring Security, Redis, MySQL |
| 메시지 브로커   | AWS SQS |
| CI/CD           | Jenkins, GitHub Webhook, Argo CD |
| 모니터링 / 복원 | Prometheus, Velero, CloudWatch Logs, OpenTelemetry |
| 보안            | WAF, Token Blacklist(Redis), MFA, OWASP ZAP 자동화 |
| 인증            | JWT (access/refresh), Cookie 기반 인증 (Spring Gateway Filter) |

---
 서비스 구성도

![아키텍처 다이어그램](<img width="11768" height="13316" alt="image" src="https://github.com/user-attachments/assets/33c4782a-ce48-49ce-90e0-96ccbb7afac2" />
) 




