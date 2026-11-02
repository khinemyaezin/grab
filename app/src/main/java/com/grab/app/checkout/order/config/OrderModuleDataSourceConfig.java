package com.grab.app.checkout.order.config;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "com.order.adapter.persistence.repository.jpa.OrderJpaRepository")
public class OrderModuleDataSourceConfig {

    @Bean(name = "orderFlyway", initMethod = "migrate")
    public Flyway orderFlyway(
            @Qualifier("orderDataSource") DataSource dataSource,
            @Value("${order.flyway.default-schema:\"order\"}") String schema,
            @Value("${order.flyway.locations:classpath:db/migration/order}") String locations,
            @Value("${order.flyway.table:flyway_schema_history}") String table
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

    @Bean("orderEntityManagerFactory")
    @DependsOn("orderFlyway")
    public LocalContainerEntityManagerFactoryBean orderEntityManagerFactory(
            @Qualifier("orderDataSource") DataSource dataSource,
            @Value("${order.jpa.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}") String dialect,
            @Value("${order.jpa.hibernate.hbm2ddl.auto:validate}") String hbm2ddlAuto,
            @Value("${order.jpa.hibernate.show_sql:false}") String showSql,
            @Value("${order.jpa.hibernate.format_sql:false}") String formatSql,
            @Value("${order.jpa.hibernate.default_schema:${order.flyway.default-schema:\"order\"}}") String defaultSchema
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.order.adapter.persistence.entity", "com.order.adapter.persistence.outbox");
        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("order");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.format_sql", formatSql);
        properties.put("hibernate.default_schema", defaultSchema);
        factory.setJpaPropertyMap(properties);

        return factory;
    }

    @Bean("orderTransactionManager")
    public PlatformTransactionManager orderTransactionManager(
            @Qualifier("orderEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableJpaRepositories(
            basePackages = "com.order.adapter.persistence.repository.jpa",
            entityManagerFactoryRef = "orderEntityManagerFactory",
            transactionManagerRef = "orderTransactionManager"
    )
    static class OrderRepositories {
    }
}
