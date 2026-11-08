package com.grab.app.account.customer.internal.config;

import com.customer.adapter.persistence.config.CustomerPersistenceConfig;
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

@ComponentScan(
        basePackages = "com.customer.adapter.persistence.mapper",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = ".*MapperImpl"
        )
)
@EnableJpaRepositories(
        basePackages = "com.customer.adapter.persistence.repository.jpa",
        entityManagerFactoryRef = "customerEntityManagerFactory",
        transactionManagerRef = "customerTransactionManager"
)
@EnableTransactionManagement
@Import({CustomerPersistenceConfig.class, CustomerModuleOutboxConfig.class})
public class CustomerModuleDataSourceConfig {

    @Bean(name = "customerFlyway", initMethod = "migrate")
    public Flyway customerFlyway(
            @Qualifier("accountDataSource") DataSource dataSource,
            @Value("${customer.flyway.default-schema:customer}") String schema,
            @Value("${customer.flyway.locations:classpath:db/migration/customer}") String locations,
            @Value("${customer.flyway.table:flyway_schema_history}") String table
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

    @Bean("customerEntityManagerFactory")
    @DependsOn("customerFlyway")
    public LocalContainerEntityManagerFactoryBean customerEntityManagerFactory(
            @Qualifier("accountDataSource") DataSource dataSource,
            @Value("${customer.jpa.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}") String dialect,
            @Value("${customer.jpa.hibernate.hbm2ddl.auto:validate}") String hbm2ddlAuto,
            @Value("${customer.jpa.hibernate.show_sql:false}") String showSql,
            @Value("${customer.jpa.hibernate.format_sql:false}") String formatSql,
            @Value("${customer.jpa.hibernate.default_schema:${customer.flyway.default-schema:customer}}") String defaultSchema
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.customer.adapter.persistence.entity", "com.customer.adapter.persistence.outbox");
        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("customer");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.format_sql", formatSql);
        properties.put("hibernate.default_schema", defaultSchema);
        factory.setJpaPropertyMap(properties);

        return factory;
    }

    @Bean("customerTransactionManager")
    public PlatformTransactionManager customerTransactionManager(
            @Qualifier("customerEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

