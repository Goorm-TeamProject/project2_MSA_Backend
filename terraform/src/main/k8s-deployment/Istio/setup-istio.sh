#!/bin/bash

set -e  # 에러 발생 시 즉시 종료

# 1. Istio 네임스페이스 생성
echo "[1/5] Creating namespace: istio-system"
kubectl create namespace istio-system || echo "Namespace already exists"

# 2. Istio 설치 (default 프로파일로)
echo "[2/5] Installing Istio Control Plane..."
istioctl install --set profile=default -y

# 3. 설치 확인
echo "[3/5] Waiting for istio-system pods..."
kubectl -n istio-system wait --for=condition=Available --timeout=120s deployment/istiod

# 4. default 네임스페이스에 사이드카 자동 주입 활성화
echo "[4/5] Enabling sidecar injection on default namespace..."
kubectl label namespace default istio-injection=enabled --overwrite

# 5. Gateway + VirtualService 배포 (있다면)
if [ -f "istio/gateway.yaml" ] && [ -f "istio/virtualservice.yaml" ]; then
  echo "[5/5] Applying Istio Gateway & VirtualService"
  kubectl apply -f istio/gateway.yaml
  kubectl apply -f istio/virtualservice.yaml
else
  echo "[5/5] Skipping Gateway & VirtualService (YAML not found)"
fi

echo "Istio setup complete."
