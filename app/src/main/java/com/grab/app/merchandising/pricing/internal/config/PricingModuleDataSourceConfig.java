package com.grab.app.merchandising.pricing.internal.config;

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
        basePackages = "com.pricing.adapter.persistence.mapper",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = ".*MapperImpl"
        )
)
@EnableJpaRepositories(
        basePackages = "com.pricing.adapter.persistence.repository.jpa",
        entityManagerFactoryRef = "pricingEntityManagerFactory",
        transactionManagerRef = "pricingTransactionManager"
)
@EnableTransactionManagement
@Import({com.pricing.adapter.persistence.config.PricingPersistenceConfig.class, PricingModuleOutboxConfig.class})
public class PricingModuleDataSourceConfig {

    @Bean(name = "pricingFlyway", initMethod = "migrate")
    public Flyway pricingFlyway(
            @Qualifier("catalogDataSource") DataSource pricingDataSource,
            @Value("${pricing.flyway.default-schema:pricing}") String schema,
            @Value("${pricing.flyway.locations:classpath:db/migration/pricing}") String locations,
            @Value("${pricing.flyway.table:flyway_schema_history}") String table
    ) {
        return Flyway.configure()
                .dataSource(pricingDataSource)
                .schemas(schema)
                .defaultSchema(schema)
                .table(table)
                .locations(locations)
                .baselineOnMigrate(false)
                .cleanDisabled(true)
                .load();
    }

    @Bean("pricingEntityManagerFactory")
    @DependsOn("pricingFlyway")
    public LocalContainerEntityManagerFactoryBean pricingEntityManagerFactory(
            @Qualifier("catalogDataSource") DataSource pricingDataSource,
            @Value("${pricing.jpa.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}") String dialect,
            @Value("${pricing.jpa.hibernate.hbm2ddl.auto:validate}") String hbm2ddlAuto,
            @Value("${pricing.jpa.hibernate.show_sql:false}") String showSql,
            @Value("${pricing.jpa.hibernate.format_sql:false}") String formatSql,
            @Value("${pricing.jpa.hibernate.default_schema:${pricing.flyway.default-schema:pricing}}") String defaultSchema
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.pricing.adapter.persistence.entity", "com.pricing.adapter.persistence.outbox");
        factory.setDataSource(pricingDataSource);
        factory.setPersistenceUnitName("pricing");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.format_sql", formatSql);
        properties.put("hibernate.default_schema", defaultSchema);
        factory.setJpaPropertyMap(properties);

        return factory;
    }

    @Bean("pricingTransactionManager")
    public PlatformTransactionManager pricingTransactionManager(
            @Qualifier("pricingEntityManagerFactory") EntityManagerFactory pricingEntityManagerFactory
    ) {
        return new JpaTransactionManager(pricingEntityManagerFactory);
    }
}

