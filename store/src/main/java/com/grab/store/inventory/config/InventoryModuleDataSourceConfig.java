package com.grab.store.inventory.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/*@Configuration
@EnableJpaRepositories(
        basePackages = "com.inventory.infrastructure.repository",
        entityManagerFactoryRef = "inventoryEntityManagerFactory",
        transactionManagerRef = "inventoryTransactionManager"
)*/
public class InventoryModuleDataSourceConfig {

   /* @Bean("inventoryDataSourceProperties")
    @ConfigurationProperties("datasource.inventory")
    public DataSourceProperties inventoryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("inventoryDataSource")
    public DataSource inventoryDataSource(@Qualifier("inventoryDataSourceProperties") DataSourceProperties inventoryDataSourceProperties) {
        return inventoryDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean("inventoryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean inventoryEntityManagerFactory(@Qualifier("inventoryDataSource") DataSource inventoryDataSource) {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(inventoryDataSource);
        em.setPackagesToScan("com.inventory.infrastructure.entity");
        em.setPersistenceUnitName("inventoryPersistenceUnit");

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaPropertyMap(hibernateProperties());

        return em;
    }

    @Bean
    public PlatformTransactionManager inventoryTransactionManager(@Qualifier("inventoryEntityManagerFactory") EntityManagerFactory productEntityManagerFactory) {
        return new JpaTransactionManager(productEntityManagerFactory);
    }

    private Map<String, Object> hibernateProperties() {
        return Map.of("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
                "hibernate.hbm2ddl.auto", "update",
                "hibernate.show_sql", "true",
                "hibernate.format_sql", "true");
    }*/
}
