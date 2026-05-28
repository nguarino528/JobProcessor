#!/bin/bash
set -e

AWS_REGION="us-east-1"
ECR_REGISTRY="646053151564.dkr.ecr.us-east-1.amazonaws.com/project/job-app"
IMAGE="$ECR_REGISTRY:$1"

aws ecr get-login-password --region "$AWS_REGION" | \
  docker login --username AWS --password-stdin "$ECR_REGISTRY"

docker pull "$IMAGE"

docker rm -f job-app || true

docker run -d \
  --name job-app \
  -p 8080:8080 \
  --restart=unless-stopped \
  "$IMAGE"