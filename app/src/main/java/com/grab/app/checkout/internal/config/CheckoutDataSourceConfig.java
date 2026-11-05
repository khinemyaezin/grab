package com.grab.app.checkout.internal.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CheckoutDataSourceConfig {

    @Bean("checkoutDataSourceProperties")
    @ConfigurationProperties("checkout.datasource")
    public DataSourceProperties checkoutDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = {"checkoutDataSource", "orderDataSource"})
    @ConfigurationProperties("checkout.datasource.hikari")
    public HikariDataSource checkoutDataSource(
            @Qualifier("checkoutDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}
