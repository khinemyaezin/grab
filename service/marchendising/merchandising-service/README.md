# Merchandising service deployment

Build from the repository root. The development example expects a separate PostgreSQL database named `product_merchandising` at `merchandising-db` on the same private Docker network.

```sh
docker build -f service/marchendising/merchandising-service/Dockerfile -t grab-merchandising-service .
cp docker/env/merchandising-service.env.example docker/env/merchandising-service.env
docker run --name merchandising-service --network grab-services \
  --env-file docker/env/merchandising-service.env grab-merchandising-service
```

Set `SPRING_PROFILES_ACTIVE=prod` and supply `MERCHANDISING_DB_URL`, `MERCHANDISING_DB_USERNAME`, and `MERCHANDISING_DB_PASSWORD` through the deployment environment. Keep the real env file out of version control. The service uses one Hikari pool for catalog, inventory, and pricing. Flyway owns each active schema and production Hibernate validates it.

Set `GRAB_RPC_ENABLED=true` to export the existing contracts on port `1100` by default. Set the bind address to `0.0.0.0` in Docker and advertise the service's resolvable private DNS name. The legacy loopback defaults apply outside the Docker example. Keep the RMI registry on the private network.

The service runs headless. Local JMX exposes health and metrics. Liveness tracks application state and readiness checks the database. Docker checks the Java process or the enabled RMI socket. Logs go to console by default. For optional rolling files, set `LOGGER_FILE_ENABLED=true` and mount a writable directory at `LOGGER_FILE_PATH` for UID 1001.

Catalog uses the existing S3 adapter. Set `STORAGE_S3_ENDPOINT`, `STORAGE_S3_REGION`, `STORAGE_S3_BUCKET`, `STORAGE_S3_ACCESS_KEY`, `STORAGE_S3_SECRET_KEY`, and `STORAGE_S3_PUBLIC_BASE_URL` for the target object store. The upload limit is set with `STORAGE_UPLOAD_MAX_SIZE_BYTES`. Use bucket credentials with access to the configured key prefix.

Catalog and inventory publish their security manifests during bootstrap and republish them on a schedule. Catalog variant deletion cascades to inventory and pricing inside this service. Merchant lifecycle events that update catalog availability and the Identity consumers for access manifests need cross-process delivery before cutover.

## Before moving traffic

Use a dedicated test database for standalone startup. Coordinate outbox ownership so only one runtime polls any given schema. Plan consumer migration and either drain or translate persisted legacy event type names before transferring an existing database. In-process Spring events stay inside this runtime.
