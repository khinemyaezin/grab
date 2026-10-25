package com.grab.app.shared.persistence;

import com.grab.app.EcommerceApplication;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("migration")
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(classes = EcommerceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"app.seed.enabled=false", "API_SECURITY=false"})
@ActiveProfiles("dev")
class ConsolidatedPersistenceTopologyIntegrationTest {
    @Container
    private static final PostgreSQLContainer<?> PRODUCT = database();
    @Container
    private static final PostgreSQLContainer<?> ORDERS = database()
            .withInitScript("db/test/order-management-init.sql");
    @Container
    private static final PostgreSQLContainer<?> IDENTITY = database();
    @Container
    private static final PostgreSQLContainer<?> CHANNEL = database();
    @Container
    private static final PostgreSQLContainer<?> DISCOVERY = database();

    private static final Map<String, List<String>> SCHEMAS_BY_DATABASE = Map.of(
            "product-merchandising", List.of("catalog", "inventory", "pricing"),
            "order-management", List.of("cart", "workflows", "order", "payment"),
            "identity-account", List.of("identity", "merchant", "customer"),
            "channel-context", List.of("saleschannel", "region"),
            "discovery", List.of("discovery"));

    private static final List<String> PERSISTENCE_UNITS = List.of(
            "catalogEntityManagerFactory", "inventoryEntityManagerFactory", "pricingEntityManagerFactory",
            "cartEntityManagerFactory", "workflowsEntityManagerFactory", "identityEntityManagerFactory",
            "merchantEntityManagerFactory", "customerEntityManagerFactory", "salesChannelEntityManagerFactory",
            "regionEntityManagerFactory", "storefrontQueryEntityManagerFactory");

    @Autowired
    private ApplicationContext context;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        register(registry, "PM", PRODUCT);
        register(registry, "OM", ORDERS);
        register(registry, "IA", IDENTITY);
        registry.add("CHANNEL_CONTEXT_DATASOURCE_URL", CHANNEL::getJdbcUrl);
        registry.add("CHANNEL_CONTEXT_DATASOURCE_USERNAME", CHANNEL::getUsername);
        registry.add("CHANNEL_CONTEXT_DATASOURCE_PASSWORD", CHANNEL::getPassword);
        registry.add("DISCOVERY_DATASOURCE_URL", DISCOVERY::getJdbcUrl);
        registry.add("DISCOVERY_DATASOURCE_USERNAME", DISCOVERY::getUsername);
        registry.add("DISCOVERY_DATASOURCE_PASSWORD", DISCOVERY::getPassword);
    }

    @Test
    void startsAllElevenPersistenceUnitsOverFiveConfiguredPhysicalPools() throws Exception {
        Map<String, HikariDataSource> pools = context.getBeansOfType(HikariDataSource.class);
        assertThat(pools).hasSize(5);
        assertThat(context.getBeansOfType(EntityManagerFactory.class).keySet())
                .containsExactlyInAnyOrderElementsOf(PERSISTENCE_UNITS);
        Map<String, PlatformTransactionManager> transactionManagers = context.getBeansOfType(PlatformTransactionManager.class);
        assertThat(transactionManagers).hasSize(11);
        assertThat(transactionManagers.values().stream().collect(Collectors.toSet())).hasSize(11);

        assertPool(pools.get("productMerchandisingSharedDataSource"), "HikariPool-ProductMerchandising", 20, 5, PRODUCT);
        assertPool(pools.get("orderManagementSharedDataSource"), "HikariPool-OrderManagement", 20, 5, ORDERS);
        assertPool(pools.get("identityAccountSharedDataSource"), "HikariPool-IdentityAccount", 15, 3, IDENTITY);
        assertPool(pools.get("channelContextSharedDataSource"), "HikariPool-ChannelContext", 10, 2, CHANNEL);
        assertPool(pools.get("discoveryProductDataSource"), "HikariPool-DiscoveryProduct", 25, 5, DISCOVERY);
    }

    @Test
    void migratesEachImplementedSchemaWithoutPublicTablesOrCrossSchemaForeignKeys() throws Exception {
        assertSchemas("productMerchandisingSharedDataSource", PRODUCT, SCHEMAS_BY_DATABASE.get("product-merchandising"),
                List.of("catalog", "inventory", "pricing"));
        assertSchemas("orderManagementSharedDataSource", ORDERS, SCHEMAS_BY_DATABASE.get("order-management"),
                List.of("cart", "workflows"));
        assertSchemas("identityAccountSharedDataSource", IDENTITY, SCHEMAS_BY_DATABASE.get("identity-account"),
                List.of("identity", "merchant", "customer"));
        assertSchemas("channelContextSharedDataSource", CHANNEL, SCHEMAS_BY_DATABASE.get("channel-context"),
                List.of("saleschannel", "region"));
        assertSchemas("discoveryProductDataSource", DISCOVERY, SCHEMAS_BY_DATABASE.get("discovery"),
                List.of("discovery"));
    }

    @Test
    void logicalDataSourceConnectionsDoNotLeakSchemaStateIntoSharedPools() throws Exception {
        DataSource inventory = context.getBean("inventoryDataSource", DataSource.class);
        try (Connection connection = inventory.getConnection()) {
            connection.setSchema("inventory");
        }

        try (Connection connection = inventory.getConnection()) {
            assertThat(connection.getSchema()).isEqualTo("public");
        }
    }

    @Test
    void overlappingDomainTransactionsUseSeparateResourcesOverTheirSharedPhysicalPool() {
        DataSource catalogDataSource = context.getBean("catalogDataSource", DataSource.class);
        DataSource inventoryDataSource = context.getBean("inventoryDataSource", DataSource.class);
        var catalogTransaction = new TransactionTemplate(
                context.getBean("catalogTransactionManager", PlatformTransactionManager.class));
        var inventoryTransaction = new TransactionTemplate(
                context.getBean("inventoryTransactionManager", PlatformTransactionManager.class));

        catalogTransaction.execute(catalogStatus -> {
            var catalogConnection = DataSourceUtils.getConnection(catalogDataSource);
            inventoryTransaction.execute(inventoryStatus -> {
                var inventoryConnection = DataSourceUtils.getConnection(inventoryDataSource);
                try {
                    assertThat(catalogConnection.getAutoCommit()).isFalse();
                    assertThat(inventoryConnection.getAutoCommit()).isFalse();
                    assertThat(inventoryConnection).isNotSameAs(catalogConnection);
                    inventoryStatus.setRollbackOnly();
                    return null;
                } catch (Exception exception) {
                    throw new IllegalStateException(exception);
                }
            });
            try {
                assertThat(catalogConnection.isClosed()).isFalse();
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
            return null;
        });
    }

    @Test
    void rerunningMigrationsDoesNotApplyAnyVersionTwice() {
        Map<String, Flyway> flyways = context.getBeansOfType(Flyway.class);
        assertThat(flyways).hasSize(11);
        for (Flyway flyway : flyways.values()) {
            assertThat(flyway.migrate().migrationsExecuted).isZero();
        }
    }

    private void assertPool(HikariDataSource pool, String name, int maximum, int minimumIdle,
                            PostgreSQLContainer<?> database) throws Exception {
        assertThat(pool.getPoolName()).isEqualTo(name);
        assertThat(pool.getMaximumPoolSize()).isEqualTo(maximum);
        assertThat(pool.getMinimumIdle()).isEqualTo(minimumIdle);
        try (Connection connection = pool.getConnection()) {
            assertThat(connection.getCatalog()).isEqualTo(database.getDatabaseName());
        }
    }

    private void assertSchemas(String poolName, PostgreSQLContainer<?> database, List<String> schemas,
                               List<String> migratedSchemas) throws Exception {
        DataSource dataSource = context.getBean(poolName, DataSource.class);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for (String schema : schemas) {
                try (ResultSet result = statement.executeQuery("select exists (select 1 from information_schema.schemata where schema_name = '" + schema + "')")) {
                    result.next();
                    assertThat(result.getBoolean(1)).as("schema %s exists in %s", schema, database.getDatabaseName()).isTrue();
                }
            }

            try (ResultSet result = statement.executeQuery("""
                    select count(*) from information_schema.tables
                    where table_type = 'BASE TABLE'
                      and table_schema not in ('pg_catalog', 'information_schema')
                      and table_schema = 'public'
                    """)) {
                result.next();
                assertThat(result.getLong(1)).as("no application tables in public in %s", database.getDatabaseName()).isZero();
            }

            for (String schema : migratedSchemas) {
                String historyTable = "flyway_schema_history_" + schema;
                try (ResultSet result = statement.executeQuery("select exists (select 1 from information_schema.tables where table_schema = '" + schema + "' and table_name = '" + historyTable + "')")) {
                    result.next();
                    assertThat(result.getBoolean(1)).as("Flyway history for %s", schema).isTrue();
                }
                try (ResultSet result = statement.executeQuery("select count(*) from \"" + schema + "\".\"" + historyTable + "\" where success")) {
                    result.next();
                    assertThat(result.getLong(1)).as("successful Flyway records for %s", schema).isPositive();
                }
            }

            for (String schema : schemas) {
                if (migratedSchemas.contains(schema)) {
                    continue;
                }
                try (var reservedSchemaQuery = connection.prepareStatement("""
                        select count(*) from information_schema.tables
                        where table_schema = ? and table_type = 'BASE TABLE'
                        """)) {
                    reservedSchemaQuery.setString(1, schema);
                    try (ResultSet result = reservedSchemaQuery.executeQuery()) {
                    result.next();
                    assertThat(result.getLong(1)).as("reserved schema %s is empty", schema).isZero();
                    }
                }
            }

            try (ResultSet result = statement.executeQuery("""
                    select count(*)
                    from pg_constraint fk
                    join pg_class source_table on source_table.oid = fk.conrelid
                    join pg_namespace source_schema on source_schema.oid = source_table.relnamespace
                    join pg_class target_table on target_table.oid = fk.confrelid
                    join pg_namespace target_schema on target_schema.oid = target_table.relnamespace
                    where fk.contype = 'f' and source_schema.nspname <> target_schema.nspname
                    """)) {
                result.next();
                assertThat(result.getLong(1)).as("no cross-schema foreign keys in %s", database.getDatabaseName()).isZero();
            }
        }
    }

    private static void register(DynamicPropertyRegistry registry, String prefix, PostgreSQLContainer<?> database) {
        registry.add(prefix + "_DATASOURCE_URL", database::getJdbcUrl);
        registry.add(prefix + "_DATASOURCE_USERNAME", database::getUsername);
        registry.add(prefix + "_DATASOURCE_PASSWORD", database::getPassword);
    }

    private static PostgreSQLContainer<?> database() {
        return new PostgreSQLContainer<>("postgres:16-alpine");
    }
}
