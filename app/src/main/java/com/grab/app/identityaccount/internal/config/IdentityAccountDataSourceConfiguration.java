package com.grab.app.identityaccount.internal.config;

import com.grab.app.shared.persistence.ClusterDataSourceFactory;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class IdentityAccountDataSourceConfiguration {
    @Bean("identityAccountDataSourceProperties")
    @ConfigurationProperties("identity-account.datasource")
    DataSourceProperties identityAccountDataSourceProperties() { return new DataSourceProperties(); }

    @Bean("identityAccountSharedDataSource")
    @ConfigurationProperties("identity-account.datasource.hikari")
    HikariDataSource identityAccountSharedDataSource(
            @Qualifier("identityAccountDataSourceProperties") DataSourceProperties properties) {
        return ClusterDataSourceFactory.create(properties);
    }

    @Bean("identityDedicatedDataSource")
    @ConditionalOnProperty(prefix = "identity.datasource", name = "dedicated", havingValue = "true")
    @ConfigurationProperties("identity.datasource.hikari")
    HikariDataSource identityDedicatedDataSource(@Qualifier("identityDataSourceProperties") DataSourceProperties properties) {
        return ClusterDataSourceFactory.create(properties);
    }

    @Bean("merchantDedicatedDataSource")
    @ConditionalOnProperty(prefix = "merchant.datasource", name = "dedicated", havingValue = "true")
    @ConfigurationProperties("merchant.datasource.hikari")
    HikariDataSource merchantDedicatedDataSource(@Qualifier("merchantDataSourceProperties") DataSourceProperties properties) {
        return ClusterDataSourceFactory.create(properties);
    }

    @Bean("customerDedicatedDataSource")
    @ConditionalOnProperty(prefix = "customer.datasource", name = "dedicated", havingValue = "true")
    @ConfigurationProperties("customer.datasource.hikari")
    HikariDataSource customerDedicatedDataSource(@Qualifier("customerDataSourceProperties") DataSourceProperties properties) {
        return ClusterDataSourceFactory.create(properties);
    }
}
