#!/bin/bash
set -e

docker buildx build -t grab-store-dev .
docker rm -f grab-store-dev
docker run -d \
  --name grab-store-dev \
  --env-file docker/env/dev.env \
  -e "JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
  --network grab-dev-network \
  -p 8080:8080 \
  -p 5005:5005 \
  grab-store-dev
