package com.grab.store.product.internal.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@ComponentScan(
        basePackages = "com.product.infrastructure.mapper",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = ".*MapperImpl"
        )
)
@EnableJpaRepositories(
        basePackages = {"com.product.infrastructure.repository"},
        entityManagerFactoryRef = "productEntityManagerFactory",
        transactionManagerRef = "productTransactionManager"
)
@EnableTransactionManagement
public class ProductModuleDataSourceConfig {

    @Primary
    @Bean("productDataSourceProperties")
    @ConfigurationProperties("product.datasource")
    public DataSourceProperties productDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("productDataSource")
    public DataSource productDataSource(@Qualifier("productDataSourceProperties")DataSourceProperties productDataSourceProperties) {
        return productDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean("productEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean productEntityManagerFactory(@Qualifier("productDataSource")DataSource productDataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.product.infrastructure");
        factory.setDataSource(productDataSource);
        factory.setJpaPropertyMap(hibernateProperties());

        return factory;
    }

    @Bean("productTransactionManager")
    @Primary
    public PlatformTransactionManager productTransactionManager( @Qualifier("productEntityManagerFactory") EntityManagerFactory productEntityManagerFactory) {
        return new JpaTransactionManager(productEntityManagerFactory);
    }

    private Map<String, Object> hibernateProperties() {
        return Map.of("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
                "hibernate.hbm2ddl.auto", "create",
                "hibernate.show_sql", "true",
                "hibernate.format_sql", "true");
    }
}
