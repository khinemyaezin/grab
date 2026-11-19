# Workflow service deployment

Build from the repository root:

```sh
docker build -f service/workflow/workflow-service/Dockerfile -t grab-workflow-service .
cp docker/env/workflow-service.env.example docker/env/workflow-service.env
docker run --name workflow-service --network grab-services \
  --env-file docker/env/workflow-service.env grab-workflow-service
```

Set `SPRING_PROFILES_ACTIVE=prod` and supply `WORKFLOW_DB_URL` (or `WORKFLOWS_DATASOURCE_URL`), `WORKFLOW_DB_USERNAME`, and `WORKFLOW_DB_PASSWORD` through the deployment environment. Keep the real env file out of version control. The service uses one Hikari pool for workflow execution, state, and outbox tables. Flyway owns the migrations under `db/migration/workflows` and production Hibernate validates them.

Set `GRAB_RPC_ENABLED=true` to export the workflow contracts on port `1103` by default. Set the bind address to `0.0.0.0` in Docker and advertise the service's resolvable private DNS name. Keep the RMI registry on the private network.

The service runs headless. Local JMX exposes health and metrics. Liveness tracks application state and readiness checks the database. Docker checks the Java process or the enabled RMI socket. Logs go to console by default. For optional rolling files, set `LOGGER_FILE_ENABLED=true` and mount a writable directory at `LOGGER_FILE_PATH` for UID 1001.

## Before moving traffic

Use a dedicated test database for standalone startup. Coordinate outbox ownership so only one runtime polls the workflow outbox table. Persisted events and outbox tasks are published reliably via the transactional outbox.
