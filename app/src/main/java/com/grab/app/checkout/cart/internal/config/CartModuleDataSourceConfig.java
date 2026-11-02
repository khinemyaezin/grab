package com.grab.app.checkout.cart.internal.config;

import com.cart.adapter.persistence.config.CartPersistenceConfig;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@ComponentScan(
        basePackages = "com.cart.adapter.persistence.mapper",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = ".*MapperImpl"
        )
)
@EnableJpaRepositories(
        basePackages = "com.cart.adapter.persistence.repository.jpa",
        entityManagerFactoryRef = "cartEntityManagerFactory",
        transactionManagerRef = "cartTransactionManager"
)
@EnableTransactionManagement
@Import({CartPersistenceConfig.class, CartModuleOutboxConfig.class})
public class CartModuleDataSourceConfig {

    @Bean(name = "cartFlyway", initMethod = "migrate")
    public Flyway cartFlyway(
            @Qualifier("orderDataSource") DataSource dataSource,
            @Value("${cart.flyway.default-schema:cart}") String schema,
            @Value("${cart.flyway.locations:classpath:db/migration/cart}") String locations,
            @Value("${cart.flyway.table:flyway_schema_history}") String table
    ) {
        return Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .defaultSchema(schema)
                .table(table)
                .locations(locations)
                .baselineOnMigrate(false)
                .cleanDisabled(true)
                .load();
    }

    @Bean("cartEntityManagerFactory")
    @DependsOn("cartFlyway")
    public LocalContainerEntityManagerFactoryBean cartEntityManagerFactory(
            @Qualifier("orderDataSource") DataSource dataSource,
            @Value("${cart.jpa.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}") String dialect,
            @Value("${cart.jpa.hibernate.hbm2ddl.auto:validate}") String hbm2ddlAuto,
            @Value("${cart.jpa.hibernate.show_sql:false}") String showSql,
            @Value("${cart.jpa.hibernate.format_sql:false}") String formatSql,
            @Value("${cart.jpa.hibernate.default_schema:${cart.flyway.default-schema:cart}}") String defaultSchema
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.cart.adapter.persistence.entity", "com.cart.adapter.persistence.outbox");
        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("cart");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.format_sql", formatSql);
        properties.put("hibernate.default_schema", defaultSchema);
        factory.setJpaPropertyMap(properties);

        return factory;
    }

    @Bean("cartTransactionManager")
    public PlatformTransactionManager cartTransactionManager(
            @Qualifier("cartEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

