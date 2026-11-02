package com.grab.app.merchandising;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            @Qualifier("merchandisingDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}
