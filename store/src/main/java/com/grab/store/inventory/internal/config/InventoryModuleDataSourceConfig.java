package com.grab.store.inventory.internal.config;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
        basePackages = "com.inventory.adapter.persistence.mapper",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = ".*MapperImpl"
        )
)
@EnableJpaRepositories(
        basePackages = "com.inventory.adapter.persistence.repository.jpa",
        entityManagerFactoryRef = "inventoryEntityManagerFactory",
        transactionManagerRef = "inventoryTransactionManager"
)
@EnableTransactionManagement
public class InventoryModuleDataSourceConfig {

    private final Environment environment;

    public InventoryModuleDataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean("inventoryDataSourceProperties")
    @ConfigurationProperties("inventory.datasource")
    public DataSourceProperties inventoryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("inventoryDataSource")
    public DataSource inventoryDataSource(@Qualifier("inventoryDataSourceProperties") DataSourceProperties inventoryDataSourceProperties) {
        return inventoryDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean("inventoryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean inventoryEntityManagerFactory(@Qualifier("inventoryDataSource") DataSource inventoryDataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.inventory.adapter.persistence");
        factory.setDataSource(inventoryDataSource);
        factory.setPersistenceUnitName("inventory");
        factory.setJpaPropertyMap(hibernateProperties());

        return factory;
    }

    @Bean("inventoryTransactionManager")
    public PlatformTransactionManager inventoryTransactionManager(
            @Qualifier("inventoryEntityManagerFactory") EntityManagerFactory inventoryEntityManagerFactory) {
        return new JpaTransactionManager(inventoryEntityManagerFactory);
    }

    @Bean(name = "inventoryFlyway", initMethod = "migrate")
    @ConditionalOnProperty(prefix = "inventory.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Flyway inventoryFlyway(@Qualifier("inventoryDataSource") DataSource inventoryDataSource) {
        return Flyway.configure()
                .dataSource(inventoryDataSource)
                .locations("classpath:db/migration/inventory")
                .baselineOnMigrate(true)
                .load();
    }

    private Map<String, Object> hibernateProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", environment.getProperty("inventory.jpa.hibernate.dialect"));
        properties.put("hibernate.hbm2ddl.auto", environment.getProperty("inventory.jpa.hibernate.hbm2ddl.auto"));
        properties.put("hibernate.show_sql", environment.getProperty("inventory.jpa.hibernate.show_sql"));
        properties.put("hibernate.format_sql", environment.getProperty("inventory.jpa.hibernate.format_sql"));
        return properties;
    }
}
