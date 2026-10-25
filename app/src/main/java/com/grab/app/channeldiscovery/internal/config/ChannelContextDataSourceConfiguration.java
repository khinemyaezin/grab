package com.grab.app.channeldiscovery.internal.config;

import com.grab.app.shared.persistence.ClusterDataSourceFactory;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ChannelContextDataSourceConfiguration {
    @Bean("channelContextDataSourceProperties")
    @ConfigurationProperties("channel-context.datasource")
    DataSourceProperties channelContextDataSourceProperties() { return new DataSourceProperties(); }

    @Bean("channelContextSharedDataSource")
    @ConfigurationProperties("channel-context.datasource.hikari")
    HikariDataSource channelContextSharedDataSource(
            @Qualifier("channelContextDataSourceProperties") DataSourceProperties properties) {
        return ClusterDataSourceFactory.create(properties);
    }

    @Bean("salesChannelDedicatedDataSource")
    @ConditionalOnProperty(prefix = "saleschannel.datasource", name = "dedicated", havingValue = "true")
    @ConfigurationProperties("saleschannel.datasource.hikari")
    HikariDataSource salesChannelDedicatedDataSource(
            @Qualifier("salesChannelDataSourceProperties") DataSourceProperties properties) {
        return ClusterDataSourceFactory.create(properties);
    }

    @Bean("regionDedicatedDataSource")
    @ConditionalOnProperty(prefix = "region.datasource", name = "dedicated", havingValue = "true")
    @ConfigurationProperties("region.datasource.hikari")
    HikariDataSource regionDedicatedDataSource(@Qualifier("regionDataSourceProperties") DataSourceProperties properties) {
        return ClusterDataSourceFactory.create(properties);
    }
}
