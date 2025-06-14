#!/bin/bash

CLUSTER_NAME="Eouil-eks-cluster"
NODEGROUP_NAME="app-20250611142419999400000001"
REGION="ap-northeast-2"

echo "Starting EKS NodeGroup..."
aws eks update-nodegroup-config \
  --cluster-name $CLUSTER_NAME \
  --nodegroup-name $NODEGROUP_NAME \
  --scaling-config minSize=1,maxSize=3,desiredSize=1 \
  --region $REGION

echo "NodeGroup scale-up complete."
