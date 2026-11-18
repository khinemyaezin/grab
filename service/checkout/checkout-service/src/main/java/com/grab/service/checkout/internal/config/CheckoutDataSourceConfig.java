package com.grab.service.checkout.internal.config;

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
public class CheckoutDataSourceConfig {

    @Bean("checkoutDataSourceProperties")
    @ConfigurationProperties("checkout.datasource")
    public DataSourceProperties checkoutDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = {"checkoutDataSource", "orderDataSource"})
    @ConfigurationProperties("checkout.datasource.hikari")
    public HikariDataSource checkoutDataSource(
            @Qualifier("checkoutDataSourceProperties") DataSourceProperties properties, Environment environment) {
        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            for (String key : new String[]{"CHECKOUT_DB_URL", "CHECKOUT_DB_USERNAME", "CHECKOUT_DB_PASSWORD"}) {
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
