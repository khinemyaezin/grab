# Seller BFF

Standalone Spring Boot web boundary for the existing seller API. It preserves `/api/v1/...` routes and uses Account, Merchandising, and Channel over RMI. It has no database and no dependency on `app`.

## Run

Build from the repository root with `mvn -pl :seller-bff -am package`. Run with Java 21 and `SPRING_PROFILES_ACTIVE=dev`; production requires `JWT_PUBLIC_KEY_PATH` to point to the public certificate matching Account's PKCS#12 signing key.

Copy `docker/env/seller-bff.env.example` to a private `.env`, update CORS and the mounted JWT certificate path, then build and run on the same private Docker network as the core services:

```sh
docker build -f service/seller-bff/Dockerfile -t grab-seller-bff .
docker run --rm --env-file .env --network grab-services -p 8080:8080 -v /secure/account-jwt-public.pem:/run/secrets/account-jwt-public.pem:ro grab-seller-bff
```

Configure the three `GRAB_RPC_CLIENTS_*_HOST` and `*_PORT` values to reachable private endpoints. Core services must have RMI enabled and their registry ports must not be exposed publicly. Account uses 1099, Merchandising 1100, and Channel 1101. Use the same stable test keystore and corresponding public certificate for standalone integration testing.

Health and metrics are available through HTTP Actuator at `/actuator/health` and `/actuator/metrics`; restrict management access at the network perimeter. The BFF preserves the existing API paths. Traffic routing from `app` to this service is a separate deployment cutover.
