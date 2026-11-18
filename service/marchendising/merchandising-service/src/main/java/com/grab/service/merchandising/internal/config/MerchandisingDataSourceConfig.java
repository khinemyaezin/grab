package com.grab.service.merchandising.internal.config;

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
public class MerchandisingDataSourceConfig {

    @Bean("merchandisingDataSourceProperties")
    @ConfigurationProperties("merchandising.datasource")
    public DataSourceProperties merchandisingDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = {"merchandisingDataSource", "catalogDataSource"})
    @ConfigurationProperties("merchandising.datasource.hikari")
    public HikariDataSource merchandisingDataSource(
            @Qualifier("merchandisingDataSourceProperties") DataSourceProperties properties, Environment environment) {
        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            for (String key : new String[]{"MERCHANDISING_DB_URL", "MERCHANDISING_DB_USERNAME", "MERCHANDISING_DB_PASSWORD"}) {
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
