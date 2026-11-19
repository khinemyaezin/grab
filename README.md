# Commerce Store

This is designed to support online buying and selling through a flexible marketplace model. 
It supports product catalog management, customer shopping, and seller-driven commerce for retailer, 
consumer-to-consumer (C2C), and third-party seller (3P) use cases. Sellers can create and manage products, and customers can browse products and place orders online. For C2C listings, customers can propose a new price, and if the seller accepts, both parties can make a deal. The platform also supports a marketplace business model where the application can charge a platform fee on transactions.

Start here: [Commerce Platform](docs/Commerce_Platform.md) · [Documentation Guide](docs/DOCUMENTATION_GUIDE.md)

## Build & Run

```bash
# Install modules
mvn clean install

# Run the store app
mvn -pl store spring-boot:run
```

## Docker

The deployment architecture uses a base topology (`docker-compose.yml`) with environment overlays:
- **Development**: `docker-compose.override.yml` (auto-loaded; exposes local database/RMI ports and launches pgAdmin at `http://localhost:5050`)
- **Production**: `docker-compose.prod.yml` (hardened network isolation, resource limits, log rotation, no pgAdmin)

### Development

```bash
# Start dev environment (auto-loads docker-compose.yml + docker-compose.override.yml)
docker compose --env-file docker/env/dev.env up --build -d

# Stop dev environment
docker compose --env-file docker/env/dev.env down -v
```

pgAdmin 4 is available at [http://localhost:5050](http://localhost:5050) with pre-configured connections for all 5 domain databases (`account`, `merchandising`, `channel`, `checkout`, `authorization`).

### Production

```bash
# Start production environment
docker compose --env-file docker/env/prod.env -f docker-compose.yml -f docker-compose.prod.yml up -d

# Stop production environment
docker compose --env-file docker/env/prod.env -f docker-compose.yml -f docker-compose.prod.yml down
```