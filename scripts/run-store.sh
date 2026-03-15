#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT/.run/store.env"

mkdir -p "$(dirname "$ENV_FILE")"

cat > "$ENV_FILE" <<'EOF'
SPRING_PROFILES_ACTIVE=dev

# logger.*
LOGGER_BACKEND=slf4j
LOGGER_LEVEL=INFO
LOGGER_FAIL_ON_MISSING_BACKEND=true

# dev DB overrides (optional)
CATALOG_DATASOURCE_URL=jdbc:postgresql://localhost:5432/catalog
CATALOG_DATASOURCE_USERNAME=admin
CATALOG_DATASOURCE_PASSWORD=123
INVENTORY_DATASOURCE_URL=jdbc:postgresql://localhost:5432/inventory
INVENTORY_DATASOURCE_USERNAME=admin
INVENTORY_DATASOURCE_PASSWORD=123
EOF

set -a
source "$ENV_FILE"
set +a

exec "$ROOT/mvnw" -pl store spring-boot:run
