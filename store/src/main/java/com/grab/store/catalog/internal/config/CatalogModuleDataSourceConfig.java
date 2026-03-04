package com.grab.store.catalog.internal.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
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
@ComponentScan(
        basePackages = "com.catalog.infrastructure.mapper",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = ".*MapperImpl"
        )
)
@EnableJpaRepositories(
        basePackages = {"com.catalog.infrastructure.repository.jpa"},
        entityManagerFactoryRef = "catalogEntityManagerFactory",
        transactionManagerRef = "catalogTransactionManager"
)
@EnableTransactionManagement
public class CatalogModuleDataSourceConfig {
    private final Environment environment;

    public CatalogModuleDataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean("catalogDataSourceProperties")
    @ConfigurationProperties("catalog.datasource")
    public DataSourceProperties catalogDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("catalogDataSource")
    public DataSource catalogDataSource(@Qualifier("catalogDataSourceProperties")DataSourceProperties catalogDataSourceProperties) {
        return catalogDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean("catalogEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean catalogEntityManagerFactory(@Qualifier("catalogDataSource")DataSource catalogDataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.catalog.infrastructure");
        factory.setDataSource(catalogDataSource);
        factory.setPersistenceUnitName("catalog");
        factory.setJpaPropertyMap(hibernateProperties());

        return factory;
    }

    @Bean("catalogTransactionManager")
    public PlatformTransactionManager catalogTransactionManager(
            @Qualifier("catalogEntityManagerFactory") EntityManagerFactory catalogEntityManagerFactory) {
        return new JpaTransactionManager(catalogEntityManagerFactory);
    }

    private Map<String, Object> hibernateProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", environment.getProperty("catalog.jpa.hibernate.dialect"));
        properties.put("hibernate.hbm2ddl.auto", environment.getProperty("catalog.jpa.hibernate.hbm2ddl.auto"));
        properties.put("hibernate.show_sql", environment.getProperty("catalog.jpa.hibernate.show_sql"));
        properties.put("hibernate.format_sql", environment.getProperty("catalog.jpa.hibernate.format_sql"));
        return properties;
    }
}
