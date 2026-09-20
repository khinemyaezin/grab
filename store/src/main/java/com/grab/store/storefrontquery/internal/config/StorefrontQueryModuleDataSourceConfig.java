package com.grab.store.storefrontquery.internal.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan(basePackages = "com.storefrontquery.infrastructure")
@EnableJpaRepositories(
        basePackages = "com.storefrontquery.infrastructure.repository.jpa",
        entityManagerFactoryRef = "storefrontQueryEntityManagerFactory",
        transactionManagerRef = "storefrontQueryTransactionManager"
)
@EnableTransactionManagement
public class StorefrontQueryModuleDataSourceConfig {

    private final Environment environment;

    public StorefrontQueryModuleDataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean("storefrontQueryDataSourceProperties")
    @ConfigurationProperties("storefrontquery.datasource")
    public DataSourceProperties storefrontQueryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("storefrontQueryDataSource")
    public DataSource storefrontQueryDataSource(
            @Qualifier("storefrontQueryDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean("storefrontQueryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean storefrontQueryEntityManagerFactory(
            @Qualifier("storefrontQueryDataSource") DataSource dataSource
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.storefrontquery.infrastructure");
        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("storefrontquery");
        factory.setJpaPropertyMap(hibernateProperties());
        return factory;
    }

    @Bean("storefrontQueryTransactionManager")
    public PlatformTransactionManager storefrontQueryTransactionManager(
            @Qualifier("storefrontQueryEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    private Map<String, Object> hibernateProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", environment.getProperty("storefrontquery.jpa.hibernate.dialect"));
        properties.put("hibernate.hbm2ddl.auto", environment.getProperty("storefrontquery.jpa.hibernate.hbm2ddl.auto"));
        properties.put("hibernate.show_sql", environment.getProperty("storefrontquery.jpa.hibernate.show_sql"));
        properties.put("hibernate.format_sql", environment.getProperty("storefrontquery.jpa.hibernate.format_sql"));
        return properties;
    }
}
