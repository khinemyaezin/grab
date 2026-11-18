# Checkout service deployment

Build from the repository root. The development example expects a separate PostgreSQL database named `order_management` at `checkout-db` on the same private Docker network.

```sh
docker build -f service/checkout/checkout-service/Dockerfile -t grab-checkout-service .
cp docker/env/checkout-service.env.example docker/env/checkout-service.env
docker run --name checkout-service --network grab-services \
  --env-file docker/env/checkout-service.env grab-checkout-service
```

Set `SPRING_PROFILES_ACTIVE=prod` and supply `CHECKOUT_DB_URL`, `CHECKOUT_DB_USERNAME`, and `CHECKOUT_DB_PASSWORD` through the deployment environment. Keep the real env file out of version control. The service uses one Hikari pool for cart. Flyway owns each active schema and production Hibernate validates it.

Set `GRAB_RPC_ENABLED=true` to export the existing contracts on port `1102` by default. Set the bind address to `0.0.0.0` in Docker and advertise the service's resolvable private DNS name. The legacy loopback defaults apply outside the Docker example. Keep the RMI registry on the private network.

The service runs headless. Local JMX exposes health and metrics. Liveness tracks application state and readiness checks the database. Docker checks the Java process or the enabled RMI socket. Logs go to console by default. For optional rolling files, set `LOGGER_FILE_ENABLED=true` and mount a writable directory at `LOGGER_FILE_PATH` for UID 1001.

Enable RMI on Channel and Merchandising and configure both peer endpoints for cart writes. Checkout connects to peers when a command needs them. Local Cart reads and startup require only the Checkout database. Order and Payment contracts are retained for future runtimes. Their migrations and RMI providers are inactive here.

## Before moving traffic

Use a dedicated test database for standalone startup. Coordinate outbox ownership so only one runtime polls any given schema. Plan consumer migration and either drain or translate persisted legacy event type names before transferring an existing database. In-process Spring events stay inside this runtime.
