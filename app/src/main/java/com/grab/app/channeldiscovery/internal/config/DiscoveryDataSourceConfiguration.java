package com.grab.app.channeldiscovery.internal.config;

import com.grab.app.shared.persistence.ClusterDataSourceFactory;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class DiscoveryDataSourceConfiguration {
    @Bean("discoveryProductDataSourceProperties")
    @ConfigurationProperties("discovery.datasource")
    DataSourceProperties discoveryProductDataSourceProperties() { return new DataSourceProperties(); }

    @Bean("discoveryProductDataSource")
    @ConfigurationProperties("discovery.datasource.hikari")
    HikariDataSource discoveryProductDataSource(
            @Qualifier("discoveryProductDataSourceProperties") DataSourceProperties properties) {
        return ClusterDataSourceFactory.create(properties);
    }
}
