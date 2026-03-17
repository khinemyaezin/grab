# Commerce Store Application

This is `the open-source repository`, designed to support online buying and selling through a flexible marketplace model. 
It supports product catalog management, customer shopping, and seller-driven commerce for retailer, 
consumer-to-consumer (C2C), and third-party seller (3P) use cases. Sellers can create and manage products, and customers can browse products and place orders online. For C2C listings, customers can propose a new price, and if the seller accepts, both parties can make a deal. The platform also supports a marketplace business model where the application can charge a platform fee on transactions.

Start with the [Business Requirements Document](docs/BRD/commerce-platform-brd.md).


## Build & Run
- Build core modules: `mvn clean install` (from repo root).
- Build/run store app: `mvn -pl store spring-boot:run` (ensure it depends on the published/installed modules).

## Docker Environments
- `docker-compose.yml` is the common runtime contract. It contains only the app service and required runtime environment variables.

Commands:

```bash
docker compose --env-file docker/env/dev.env -f docker-compose.yml down -v
docker compose --env-file docker/env/dev.env -f docker-compose.yml up --build -d
```
