#!/bin/bash
set -e

echo "Stopping and removing existing containers..."
docker compose --env-file docker/env/dev.env -f docker-compose.yml down -v

echo "Building and starting containers..."
docker compose --env-file docker/env/dev.env -f docker-compose.yml up --build -d

echo "Dev environment is up and running!"
