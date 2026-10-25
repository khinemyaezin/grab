package com.grab.app.productmerchandising.internal.config;

import com.grab.app.shared.persistence.ClusterDataSourceFactory;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ProductMerchandisingDataSourceConfiguration {
    @Bean("productMerchandisingDataSourceProperties")
    @ConfigurationProperties("product-merchandising.datasource")
    DataSourceProperties productMerchandisingDataSourceProperties() { return new DataSourceProperties(); }

    @Bean("productMerchandisingSharedDataSource")
    @ConfigurationProperties("product-merchandising.datasource.hikari")
    HikariDataSource productMerchandisingSharedDataSource(
            @Qualifier("productMerchandisingDataSourceProperties") DataSourceProperties properties) {
        return ClusterDataSourceFactory.create(properties);
    }

    @Bean("catalogDedicatedDataSource")
    @ConditionalOnProperty(prefix = "catalog.datasource", name = "dedicated", havingValue = "true")
    @ConfigurationProperties("catalog.datasource.hikari")
    HikariDataSource catalogDedicatedDataSource(@Qualifier("catalogDataSourceProperties") DataSourceProperties properties) {
        return ClusterDataSourceFactory.create(properties);
    }

    @Bean("inventoryDedicatedDataSource")
    @ConditionalOnProperty(prefix = "inventory.datasource", name = "dedicated", havingValue = "true")
    @ConfigurationProperties("inventory.datasource.hikari")
    HikariDataSource inventoryDedicatedDataSource(@Qualifier("inventoryDataSourceProperties") DataSourceProperties properties) {
        return ClusterDataSourceFactory.create(properties);
    }

    @Bean("pricingDedicatedDataSource")
    @ConditionalOnProperty(prefix = "pricing.datasource", name = "dedicated", havingValue = "true")
    @ConfigurationProperties("pricing.datasource.hikari")
    HikariDataSource pricingDedicatedDataSource(@Qualifier("pricingDataSourceProperties") DataSourceProperties properties) {
        return ClusterDataSourceFactory.create(properties);
    }
}
