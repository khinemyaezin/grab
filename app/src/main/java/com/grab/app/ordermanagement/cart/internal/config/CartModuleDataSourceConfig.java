package com.grab.app.ordermanagement.cart.internal.config;

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
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.cart.adapter.persistence.repository.jpa",
        entityManagerFactoryRef = "cartEntityManagerFactory",
        transactionManagerRef = "cartTransactionManager"
)
@EnableTransactionManagement
public class CartModuleDataSourceConfig {

    private final Environment environment;

    public CartModuleDataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean("cartDataSourceProperties")
    @ConfigurationProperties("cart.datasource")
    public DataSourceProperties cartDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("cartDataSource")
    public DataSource cartDataSource(
            @Qualifier("orderManagementSharedDataSource") DataSource sharedDataSource,
            @Qualifier("cartDedicatedDataSource") org.springframework.beans.factory.ObjectProvider<DataSource> dedicated) {
        return new DelegatingDataSource(dedicated.getIfAvailable(() -> sharedDataSource));
    }

    @Bean("cartEntityManagerFactory")
    @DependsOn("cartFlyway")
    public LocalContainerEntityManagerFactoryBean cartEntityManagerFactory(
            @Qualifier("cartDataSource") DataSource dataSource
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.cart.adapter.persistence");
        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("cart");
        factory.setJpaPropertyMap(hibernateProperties());
        return factory;
    }

    @Bean("cartTransactionManager")
    public PlatformTransactionManager cartTransactionManager(
            @Qualifier("cartEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    private Map<String, Object> hibernateProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", environment.getProperty("cart.jpa.hibernate.dialect"));
        properties.put("hibernate.hbm2ddl.auto", environment.getProperty("cart.jpa.hibernate.hbm2ddl.auto"));
        properties.put("hibernate.show_sql", environment.getProperty("cart.jpa.hibernate.show_sql"));
        properties.put("hibernate.format_sql", environment.getProperty("cart.jpa.hibernate.format_sql"));
        properties.put("hibernate.default_schema", "cart");
        return properties;
    }

    @Bean(initMethod = "migrate")
    public Flyway cartFlyway(@Qualifier("cartDataSource") DataSource dataSource, Environment env) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/cart")
                .schemas("cart").defaultSchema("cart")
                .table("flyway_schema_history_cart")
                .baselineOnMigrate(false)
                .load();
    }
}
