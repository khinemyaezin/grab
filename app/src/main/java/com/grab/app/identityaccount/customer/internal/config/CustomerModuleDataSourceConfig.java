package com.grab.app.identityaccount.customer.internal.config;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@CustomerEnabled
@EnableJpaRepositories(
        basePackages = "com.customer.adapter.persistence.repository.jpa",
        entityManagerFactoryRef = "customerEntityManagerFactory",
        transactionManagerRef = "customerTransactionManager"
)
public class CustomerModuleDataSourceConfig {

    private final Environment environment;

    public CustomerModuleDataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean("customerDataSourceProperties")
    @ConfigurationProperties("customer.datasource")
    public DataSourceProperties customerDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("customerDataSource")
    public DataSource customerDataSource(
            @Qualifier("identityAccountSharedDataSource") DataSource sharedDataSource,
            @Qualifier("customerDedicatedDataSource") org.springframework.beans.factory.ObjectProvider<DataSource> dedicated) {
        return new DelegatingDataSource(dedicated.getIfAvailable(() -> sharedDataSource));
    }

    @Bean(name = "customerFlyway", initMethod = "migrate")
    public Flyway customerFlyway(@Qualifier("customerDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/customer")
                .schemas("customer").defaultSchema("customer")
                .table("flyway_schema_history_customer")
                .baselineOnMigrate(false)
                .load();
    }

    @Bean("customerEntityManagerFactory")
    @DependsOn("customerFlyway")
    public LocalContainerEntityManagerFactoryBean customerEntityManagerFactory(
            @Qualifier("customerDataSource") DataSource dataSource
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.customer.adapter.persistence");
        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("customer");
        factory.setJpaPropertyMap(hibernateProperties());
        return factory;
    }

    @Bean("customerTransactionManager")
    public PlatformTransactionManager customerTransactionManager(
            @Qualifier("customerEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    private Map<String, Object> hibernateProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", environment.getProperty("customer.jpa.hibernate.dialect"));
        properties.put("hibernate.hbm2ddl.auto", environment.getProperty("customer.jpa.hibernate.hbm2ddl.auto"));
        properties.put("hibernate.show_sql", environment.getProperty("customer.jpa.hibernate.show_sql"));
        properties.put("hibernate.format_sql", environment.getProperty("customer.jpa.hibernate.format_sql"));
        properties.put("hibernate.default_schema", "customer");
        return properties;
    }
}
