package com.grab.app.shared.persistence;

import com.grab.app.channeldiscovery.internal.config.ChannelContextDataSourceConfiguration;
import com.grab.app.channeldiscovery.internal.config.DiscoveryDataSourceConfiguration;
import com.grab.app.identityaccount.internal.config.IdentityAccountDataSourceConfiguration;
import com.grab.app.ordermanagement.internal.config.OrderManagementDataSourceConfiguration;
import com.grab.app.productmerchandising.internal.config.ProductMerchandisingDataSourceConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterDataSourceConfigurationTest {
    private static final Map<String, String> DEDICATED_POOLS = Map.of(
            "catalog", "catalogDedicatedDataSource",
            "inventory", "inventoryDedicatedDataSource",
            "pricing", "pricingDedicatedDataSource",
            "cart", "cartDedicatedDataSource",
            "workflows", "workflowsDedicatedDataSource",
            "identity", "identityDedicatedDataSource",
            "merchant", "merchantDedicatedDataSource",
            "customer", "customerDedicatedDataSource",
            "saleschannel", "salesChannelDedicatedDataSource",
            "region", "regionDedicatedDataSource");

    @Test
    void bindsDedicatedPoolPropertiesForEveryImplementedDomain() throws Exception {
        var context = new AnnotationConfigApplicationContext();
        var yamlLoader = new YamlPropertySourceLoader();
        for (PropertySource<?> source : yamlLoader.load("application", new ClassPathResource("application.yml"))) {
            context.getEnvironment().getPropertySources().addLast(source);
        }

        Map<String, Object> properties = new HashMap<>();
        for (var entry : DEDICATED_POOLS.entrySet()) {
            String domain = entry.getKey();
            String prefix = domain + ".datasource.";
            properties.put(prefix + "dedicated", true);
            properties.put(prefix + "url", "jdbc:postgresql://dedicated-host:5432/" + domain + "_override");
            properties.put(prefix + "username", domain + "_user");
            properties.put(prefix + "password", domain + "_password");
            properties.put(prefix + "driver-class-name", "org.postgresql.Driver");
            properties.put(prefix + "hikari.pool-name", "HikariPool-" + domain);
            properties.put(prefix + "hikari.maximum-pool-size", 7);
            properties.put(prefix + "hikari.minimum-idle", 1);
        }
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("dedicated-domains", properties));
        context.register(TestConfiguration.class);
        context.refresh();

        try (context) {
            assertThat(context.getBeansOfType(HikariDataSource.class)).hasSize(15);
            for (var entry : DEDICATED_POOLS.entrySet()) {
                String domain = entry.getKey();
                HikariDataSource dataSource = context.getBean(entry.getValue(), HikariDataSource.class);
                assertThat(dataSource.getJdbcUrl()).isEqualTo("jdbc:postgresql://dedicated-host:5432/" + domain + "_override");
                assertThat(dataSource.getUsername()).isEqualTo(domain + "_user");
                assertThat(dataSource.getPassword()).isEqualTo(domain + "_password");
                assertThat(dataSource.getPoolName()).isEqualTo("HikariPool-" + domain);
                assertThat(dataSource.getMaximumPoolSize()).isEqualTo(7);
                assertThat(dataSource.getMinimumIdle()).isEqualTo(1);
            }
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties
    @Import({ProductMerchandisingDataSourceConfiguration.class, OrderManagementDataSourceConfiguration.class,
            IdentityAccountDataSourceConfiguration.class, ChannelContextDataSourceConfiguration.class,
            DiscoveryDataSourceConfiguration.class, DedicatedDomainProperties.class})
    static class TestConfiguration { }

    @Configuration(proxyBeanMethods = false)
    static class DedicatedDomainProperties {
        @Bean("catalogDataSourceProperties")
        @ConfigurationProperties("catalog.datasource")
        DataSourceProperties catalogDataSourceProperties() { return new DataSourceProperties(); }

        @Bean("inventoryDataSourceProperties")
        @ConfigurationProperties("inventory.datasource")
        DataSourceProperties inventoryDataSourceProperties() { return new DataSourceProperties(); }

        @Bean("pricingDataSourceProperties")
        @ConfigurationProperties("pricing.datasource")
        DataSourceProperties pricingDataSourceProperties() { return new DataSourceProperties(); }

        @Bean("cartDataSourceProperties")
        @ConfigurationProperties("cart.datasource")
        DataSourceProperties cartDataSourceProperties() { return new DataSourceProperties(); }

        @Bean("workflowsDataSourceProperties")
        @ConfigurationProperties("workflows.datasource")
        DataSourceProperties workflowsDataSourceProperties() { return new DataSourceProperties(); }

        @Bean("identityDataSourceProperties")
        @ConfigurationProperties("identity.datasource")
        DataSourceProperties identityDataSourceProperties() { return new DataSourceProperties(); }

        @Bean("merchantDataSourceProperties")
        @ConfigurationProperties("merchant.datasource")
        DataSourceProperties merchantDataSourceProperties() { return new DataSourceProperties(); }

        @Bean("customerDataSourceProperties")
        @ConfigurationProperties("customer.datasource")
        DataSourceProperties customerDataSourceProperties() { return new DataSourceProperties(); }

        @Bean("salesChannelDataSourceProperties")
        @ConfigurationProperties("saleschannel.datasource")
        DataSourceProperties salesChannelDataSourceProperties() { return new DataSourceProperties(); }

        @Bean("regionDataSourceProperties")
        @ConfigurationProperties("region.datasource")
        DataSourceProperties regionDataSourceProperties() { return new DataSourceProperties(); }
    }
}
