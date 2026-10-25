package com.grab.app.shared.persistence;

import com.grab.app.channeldiscovery.internal.config.ChannelContextDataSourceConfiguration;
import com.grab.app.channeldiscovery.internal.config.DiscoveryDataSourceConfiguration;
import com.grab.app.productmerchandising.catalog.internal.config.CatalogModuleDataSourceConfig;
import com.grab.app.productmerchandising.inventory.internal.config.InventoryModuleDataSourceConfig;
import com.grab.app.productmerchandising.internal.config.ProductMerchandisingDataSourceConfiguration;
import com.grab.app.productmerchandising.pricing.internal.config.PricingModuleDataSourceConfig;
import com.grab.app.ordermanagement.internal.config.OrderManagementDataSourceConfiguration;
import com.grab.app.identityaccount.internal.config.IdentityAccountDataSourceConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class DedicatedDataSourceOverrideIntegrationTest {
    @Container
    private static final PostgreSQLContainer<?> PRODUCT = database();
    @Container
    private static final PostgreSQLContainer<?> ORDERS = database();
    @Container
    private static final PostgreSQLContainer<?> IDENTITY = database();
    @Container
    private static final PostgreSQLContainer<?> CHANNEL = database();
    @Container
    private static final PostgreSQLContainer<?> DISCOVERY = database();
    @Container
    private static final PostgreSQLContainer<?> INVENTORY = database();

    @Test
    void defaultTopologyUsesFivePoolsAndMigratesInventoryInsideItsSchema() throws Exception {
        try (var context = startContext(false)) {
            assertThat(context.getBeansOfType(HikariDataSource.class)).hasSize(5);
            assertThat(poolNames(context)).containsExactlyInAnyOrder(
                    "HikariPool-ProductMerchandising", "HikariPool-OrderManagement",
                    "HikariPool-IdentityAccount", "HikariPool-ChannelContext", "HikariPool-DiscoveryProduct");

            DataSource inventoryDataSource = context.getBean("inventoryDataSource", DataSource.class);
            DataSource catalogDataSource = context.getBean("catalogDataSource", DataSource.class);
            DataSource pricingDataSource = context.getBean("pricingDataSource", DataSource.class);
            HikariDataSource productPool = context.getBean("productMerchandisingSharedDataSource", HikariDataSource.class);
            assertThat(((DelegatingDataSource) inventoryDataSource).getTargetDataSource()).isSameAs(productPool);
            assertThat(((DelegatingDataSource) catalogDataSource).getTargetDataSource()).isSameAs(productPool);
            assertThat(((DelegatingDataSource) pricingDataSource).getTargetDataSource()).isSameAs(productPool);
            assertThat(databaseName(inventoryDataSource)).isEqualTo(PRODUCT.getDatabaseName());
            assertInventoryHistory(inventoryDataSource);
        }
    }

    @Test
    void dedicatedInventoryOverrideAddsOnePoolAndRoutesOnlyInventoryToItsDatabase() throws Exception {
        try (var context = startContext(true)) {
            assertThat(context.getBeansOfType(HikariDataSource.class)).hasSize(6);
            assertThat(context.getBean("productMerchandisingSharedDataSource", HikariDataSource.class).getPoolName())
                    .isEqualTo("HikariPool-ProductMerchandising");
            assertThat(context.getBean("inventoryDedicatedDataSource", HikariDataSource.class).getPoolName())
                    .isEqualTo("HikariPool-Inventory");

            DataSource inventoryDataSource = context.getBean("inventoryDataSource", DataSource.class);
            DataSource catalogCluster = context.getBean("productMerchandisingSharedDataSource", DataSource.class);
            DataSource catalogDataSource = context.getBean("catalogDataSource", DataSource.class);
            DataSource pricingDataSource = context.getBean("pricingDataSource", DataSource.class);
            assertThat(((DelegatingDataSource) catalogDataSource).getTargetDataSource()).isSameAs(catalogCluster);
            assertThat(((DelegatingDataSource) pricingDataSource).getTargetDataSource()).isSameAs(catalogCluster);
            assertThat(((DelegatingDataSource) inventoryDataSource).getTargetDataSource())
                    .isSameAs(context.getBean("inventoryDedicatedDataSource", HikariDataSource.class));
            assertThat(databaseName(inventoryDataSource)).isEqualTo(INVENTORY.getDatabaseName());
            assertThat(databaseName(catalogCluster)).isEqualTo(PRODUCT.getDatabaseName());
            assertInventoryHistory(inventoryDataSource);
        }
    }

    private AnnotationConfigApplicationContext startContext(boolean inventoryDedicated) throws Exception {
        var context = new AnnotationConfigApplicationContext();
        var yamlLoader = new YamlPropertySourceLoader();
        for (PropertySource<?> source : yamlLoader.load("application", new ClassPathResource("application.yml"))) {
            context.getEnvironment().getPropertySources().addLast(source);
        }

        Map<String, Object> properties = new HashMap<>();
        addCluster(properties, "product-merchandising.datasource", PRODUCT);
        addCluster(properties, "order-management.datasource", ORDERS);
        addCluster(properties, "identity-account.datasource", IDENTITY);
        addCluster(properties, "channel-context.datasource", CHANNEL);
        addCluster(properties, "discovery.datasource", DISCOVERY);
        properties.put("inventory.datasource.url", inventoryDedicated ? INVENTORY.getJdbcUrl() : PRODUCT.getJdbcUrl());
        properties.put("inventory.datasource.username", inventoryDedicated ? INVENTORY.getUsername() : PRODUCT.getUsername());
        properties.put("inventory.datasource.password", inventoryDedicated ? INVENTORY.getPassword() : PRODUCT.getPassword());
        properties.put("inventory.datasource.driver-class-name", "org.postgresql.Driver");
        properties.put("inventory.datasource.hikari.pool-name", "HikariPool-Inventory");
        properties.put("inventory.datasource.hikari.maximum-pool-size", 10);
        properties.put("inventory.datasource.hikari.minimum-idle", 2);
        properties.put("inventory.datasource.dedicated", inventoryDedicated);
        properties.put("inventory.jpa.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("inventory.jpa.hibernate.hbm2ddl.auto", "validate");
        properties.put("inventory.jpa.hibernate.show_sql", false);
        properties.put("inventory.jpa.hibernate.format_sql", false);
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test-databases", properties));
        context.register(TestConfiguration.class);
        context.refresh();
        return context;
    }

    private void addCluster(Map<String, Object> properties, String prefix, PostgreSQLContainer<?> database) {
        properties.put(prefix + ".url", database.getJdbcUrl());
        properties.put(prefix + ".username", database.getUsername());
        properties.put(prefix + ".password", database.getPassword());
        properties.put(prefix + ".driver-class-name", "org.postgresql.Driver");
    }

    private String databaseName(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getCatalog();
        }
    }

    private Set<String> poolNames(AnnotationConfigApplicationContext context) {
        return context.getBeansOfType(HikariDataSource.class).values().stream()
                .map(HikariDataSource::getPoolName)
                .collect(Collectors.toSet());
    }

    private void assertInventoryHistory(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("""
                     select exists (
                         select 1 from information_schema.tables
                         where table_schema = 'inventory'
                           and table_name = 'flyway_schema_history_inventory'
                     )
                     """)) {
            result.next();
            assertThat(result.getBoolean(1)).isTrue();
        }
    }

    private static PostgreSQLContainer<?> database() {
        return new PostgreSQLContainer<>("postgres:16-alpine");
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties
    @Import({ProductMerchandisingDataSourceConfiguration.class, OrderManagementDataSourceConfiguration.class,
            IdentityAccountDataSourceConfiguration.class, ChannelContextDataSourceConfiguration.class,
            DiscoveryDataSourceConfiguration.class, CatalogModuleDataSourceConfig.class,
            InventoryModuleDataSourceConfig.class, PricingModuleDataSourceConfig.class})
    static class TestConfiguration { }
}
