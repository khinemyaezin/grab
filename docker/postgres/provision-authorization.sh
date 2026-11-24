#!/bin/sh
set -eu

: "${AUTHORIZATION_DB_NAME:?}"
: "${AUTHORIZATION_DB_USERNAME:?}"
: "${AUTHORIZATION_DB_PASSWORD:?}"
: "${AUTHORIZATION_DB_SCHEMA:=authorization_server}"
export AUTHORIZATION_DB_SCHEMA

if [ "$AUTHORIZATION_DB_USERNAME" = "$PGUSER" ]; then
    echo "The authorization database requires a dedicated role" >&2
    exit 1
fi

psql -X -d postgres -v ON_ERROR_STOP=1 <<'SQL'
\getenv role AUTHORIZATION_DB_USERNAME
\getenv password AUTHORIZATION_DB_PASSWORD
\getenv db AUTHORIZATION_DB_NAME
SELECT format('CREATE ROLE %I LOGIN PASSWORD %L', :'role', :'password')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = :'role')\gexec
SELECT format('ALTER ROLE %I WITH PASSWORD %L', :'role', :'password')\gexec
SELECT format('CREATE DATABASE %I', :'db')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = :'db')\gexec
GRANT CONNECT ON DATABASE :"db" TO :"role";
SQL

psql -X -d "$AUTHORIZATION_DB_NAME" -v ON_ERROR_STOP=1 <<'SQL'
\getenv role AUTHORIZATION_DB_USERNAME
\getenv schema AUTHORIZATION_DB_SCHEMA
CREATE SCHEMA IF NOT EXISTS :"schema" AUTHORIZATION :"role";
GRANT USAGE, CREATE ON SCHEMA :"schema" TO :"role";
SQL
