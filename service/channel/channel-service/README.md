# Channel service deployment

Build from the repository root. The development example expects a separate PostgreSQL database named `channel_context` at `channel-db` on the same private Docker network.

```sh
docker build -f service/channel/channel-service/Dockerfile -t grab-channel-service .
cp docker/env/channel-service.env.example docker/env/channel-service.env
docker run --name channel-service --network grab-services \
  --env-file docker/env/channel-service.env grab-channel-service
```

Set `SPRING_PROFILES_ACTIVE=prod` and supply `CHANNEL_DB_URL`, `CHANNEL_DB_USERNAME`, and `CHANNEL_DB_PASSWORD` through the deployment environment. Keep the real env file out of version control. The service uses one Hikari pool for saleschannel, region, and discovery. Flyway owns each active schema and production Hibernate validates it.

Set `GRAB_RPC_ENABLED=true` to export the existing contracts on port `1101` by default. Set the bind address to `0.0.0.0` in Docker and advertise the service's resolvable private DNS name. The legacy loopback defaults apply outside the Docker example. Keep the RMI registry on the private network.

The service runs headless. Local JMX exposes health and metrics. Liveness tracks application state and readiness checks the database. Docker checks the Java process or the enabled RMI socket. Logs go to console by default. For optional rolling files, set `LOGGER_FILE_ENABLED=true` and mount a writable directory at `LOGGER_FILE_PATH` for UID 1001.

Discovery resolves channel and region context in the same process. Set `GRAB_RPC_CLIENTS_MERCHANDISING_HOST` and `GRAB_RPC_CLIENTS_MERCHANDISING_PORT` for catalog, stock, and pricing lookups. Enable Merchandising's RMI provider on that endpoint. Deployments of Account that call this service must set `GRAB_RPC_CLIENTS_CHANNEL_PORT=1101` and the correct host.

Use a provisioned publishable key for storefront reads. Seller-owned channel key management requires merchant context, which the current RMI key management contract does not carry.

Marketplace channel data and the sales channel security manifest are prepared at bootstrap. Run the opt-in discovery rebuild as a separate job with `JOBS_DISCOVERY_REBUILD_ENABLED=true` and the Merchandising endpoint configured. Tune `STOREFRONTQUERY_REBUILD_BATCH_SIZE` if needed. The job exits with a status code by default. Normal server startup leaves the job disabled.

Merchant-approved provisioning, Merchandising discovery events, and Identity manifest registration need cross-process event delivery before cutover. Location-wide discovery refresh also needs a remote SKU-by-location lookup, which the published Inventory contract does not currently expose.

## Before moving traffic

Use a dedicated test database for standalone startup. Coordinate outbox ownership so only one runtime polls any given schema. Plan consumer migration and either drain or translate persisted legacy event type names before transferring an existing database. In-process Spring events stay inside this runtime.
