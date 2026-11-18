# Account service deployment

Build the image from the repository root:

```sh
docker build -f service/account/account-service/Dockerfile -t grab-account-service .
```

Copy `docker/env/account-service.env.example` to an ignored `docker/env/account-service.env`, then set the database credentials, production profile, and JWT keystore values for the target deployment. For local development, the example can be used directly. Run the container on a private network that can reach the dedicated account database:

```sh
docker run --env-file docker/env/account-service.env --network <private-network> grab-account-service
```

The `prod` profile requires `ACCOUNT_DB_URL`, `ACCOUNT_DB_USERNAME`, `ACCOUNT_DB_PASSWORD`, `JWT_KEYSTORE_PATH`, and `JWT_KEYSTORE_PASSWORD`. Mount a PKCS#12 keystore read-only at that path. Its `JWT_KEY_ALIAS` entry, defaulting to `account-service`, must contain an RSA private key and certificate. Flyway applies schema changes and Hibernate validates identity, merchant, and customer schemas.

Account startup publishes identity and merchant security manifests and the merchant role declaration. Set `BOOTSTRAP_GATE_READINESS=true` when readiness should wait for those required bootstrap tasks. Their retry timing is controlled by the `BOOTSTRAP_*` variables. Security manifest republishing and identity revalidation intervals can be tuned with the corresponding `SECURITY_MANIFEST_*` variables in the environment example.

RMI stays disabled unless `GRAB_RPC_ENABLED=true`. Set `GRAB_RPC_BIND_ADDRESS=0.0.0.0` in a container, advertise a hostname clients can resolve, and allow registry port 1099 on the private network. Merchant onboarding calls the Channel `RegionPort`, so configure `GRAB_RPC_CLIENTS_CHANNEL_HOST` and `GRAB_RPC_CLIENTS_CHANNEL_PORT` when that operation is needed.

The service has no HTTP listener. Health and metrics are exposed through the local JMX server. Container health checks use process liveness with RMI disabled and the RMI registry socket when enabled.

## Migration dependencies before traffic cutover

- BFFs must obtain the account signing public key and use the account remote message contracts.
- Identity and merchant security-manifest bootstrap, publication, and account event listeners now run in `account-service`; verify no second runtime owns the same account outboxes during rollout.
- Replace in-process outbox event delivery with cross-process event transport. Persisted legacy event type names need a compatibility reader or a planned drain.
- Assign a single runtime to each account outbox before enabling polling against a shared database.
