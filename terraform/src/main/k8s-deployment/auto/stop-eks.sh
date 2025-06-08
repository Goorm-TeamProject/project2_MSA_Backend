#!/bin/bash

CLUSTER_NAME="Eouil-eks-cluster"
NODEGROUP_NAME="app-20250607142833687100000001"
REGION="ap-northeast-2"

echo "Stopping EKS NodeGroup..."
aws eks update-nodegroup-config \
  --cluster-name $CLUSTER_NAME \
  --nodegroup-name $NODEGROUP_NAME \
  --scaling-config minSize=0,maxSize=1,desiredSize=0 \
  --region $REGION

echo "NodeGroup scale-down complete."
