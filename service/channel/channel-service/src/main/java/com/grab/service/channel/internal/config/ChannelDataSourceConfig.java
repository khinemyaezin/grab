package com.grab.service.channel.internal.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.util.StringUtils;

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
            @Qualifier("channelDataSourceProperties") DataSourceProperties properties, Environment environment) {
        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            for (String key : new String[]{"CHANNEL_DB_URL", "CHANNEL_DB_USERNAME", "CHANNEL_DB_PASSWORD"}) {
                if (!StringUtils.hasText(environment.getProperty(key))) {
                    throw new IllegalStateException("Production requires " + key);
                }
            }
        }
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}
