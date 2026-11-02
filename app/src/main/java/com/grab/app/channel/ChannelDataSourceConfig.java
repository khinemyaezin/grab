package com.grab.app.channel;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ChannelDataSourceConfig {

    @Bean("channelDataSourceProperties")
    @ConfigurationProperties("channel.datasource")
    public DataSourceProperties channelDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = {"channelDataSource", "discoveryDataSource"})
    @ConfigurationProperties("channel.datasource.hikari")
    public HikariDataSource channelDataSource(
            @Qualifier("channelDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}
