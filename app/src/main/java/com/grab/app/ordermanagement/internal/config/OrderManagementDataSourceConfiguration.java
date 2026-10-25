package com.grab.app.ordermanagement.internal.config;

import com.grab.app.shared.persistence.ClusterDataSourceFactory;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OrderManagementDataSourceConfiguration {
    @Bean("orderManagementDataSourceProperties")
    @ConfigurationProperties("order-management.datasource")
    DataSourceProperties orderManagementDataSourceProperties() { return new DataSourceProperties(); }

    @Bean("orderManagementSharedDataSource")
    @ConfigurationProperties("order-management.datasource.hikari")
    HikariDataSource orderManagementSharedDataSource(
            @Qualifier("orderManagementDataSourceProperties") DataSourceProperties properties) {
        return ClusterDataSourceFactory.create(properties);
    }

    @Bean("cartDedicatedDataSource")
    @ConditionalOnProperty(prefix = "cart.datasource", name = "dedicated", havingValue = "true")
    @ConfigurationProperties("cart.datasource.hikari")
    HikariDataSource cartDedicatedDataSource(@Qualifier("cartDataSourceProperties") DataSourceProperties properties) {
        return ClusterDataSourceFactory.create(properties);
    }

    @Bean("workflowsDedicatedDataSource")
    @ConditionalOnProperty(prefix = "workflows.datasource", name = "dedicated", havingValue = "true")
    @ConfigurationProperties("workflows.datasource.hikari")
    HikariDataSource workflowsDedicatedDataSource(@Qualifier("workflowsDataSourceProperties") DataSourceProperties properties) {
        return ClusterDataSourceFactory.create(properties);
    }
}
