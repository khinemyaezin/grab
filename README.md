# Grab Ecommerce Platform

This is designed to support online buying and selling through a flexible marketplace model. 
It supports product catalog management, customer shopping, and seller-driven commerce for retailer, 
consumer-to-consumer (C2C), and third-party seller (3P) use cases. Sellers can create and manage products, and customers can browse products and place orders online. For C2C listings, customers can propose a new price, and if the seller accepts, both parties can make a deal. The platform also supports a marketplace business model where the application can charge a platform fee on transactions.

Start here: [Commerce Platform](docs/Commerce_Platform.md) · [Documentation Guide](docs/DOCUMENTATION_GUIDE.md)

## Build & Run

```bash
# Install modules
mvn clean install

# Run the new app module
mvn -pl app -am spring-boot:run

# Run the retained legacy runner
mvn -pl store -am spring-boot:run
```

Run only one runner against a database set at a time. The app and legacy store runner must use isolated databases.

## Docker

`docker-compose.yml` defines the app service and runtime environment.
The app uses its own fresh PostgreSQL volume (`grab-app-dev-postgres-data`) by default.

```bash
# Start
docker compose --env-file docker/env/dev.env -f docker-compose.yml up --build -d

# Stop
docker compose --env-file docker/env/dev.env -f docker-compose.yml down -v
```
