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
The app provisions five fresh PostgreSQL volumes by default: `grab-app-dev-product-merchandising-data`, `grab-app-dev-order-management-data`, `grab-app-dev-identity-account-data`, `grab-app-dev-channel-context-data`, and `grab-app-dev-discovery-data`. The previous `grab-app-dev-postgres-data` volume is left untouched and is not attached to these services. Starting this Compose topology creates empty databases; it does not transfer existing data.

The databases are available on host ports 5432–5436 in topology order: Product Merchandising, Order Management, Identity & Accounts, Channel Context, and Discovery. Inside Compose, the app connects to each service on port 5432. Schemas are initialized in each database and domain migrations create tables on app startup.

```bash
# Start
docker compose --env-file docker/env/dev.env -f docker-compose.yml up --build -d

# Stop
docker compose --env-file docker/env/dev.env -f docker-compose.yml down
```
