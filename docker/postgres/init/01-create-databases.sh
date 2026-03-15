#!/bin/sh
set -eu

for db in $MODULE_DATABASES; do
  psql --username "$POSTGRES_USER" --dbname postgres -v ON_ERROR_STOP=1 -v db="$db" <<'SQL'
SELECT format('CREATE DATABASE %I', :'db')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = :'db')\gexec
SQL
done
