package com.grab.app.merchandising.catalog.internal.config;

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
        basePackages = "com.catalog.adapter.persistence.mapper",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = ".*MapperImpl"
        )
)
@EnableJpaRepositories(
        basePackages = {"com.catalog.adapter.persistence.repository"},
        entityManagerFactoryRef = "catalogEntityManagerFactory",
        transactionManagerRef = "catalogTransactionManager"
)
@EnableTransactionManagement
@Import({com.catalog.adapter.persistence.config.CatalogPersistenceConfig.class, CatalogModuleOutboxConfig.class})
public class CatalogModuleDataSourceConfig {

    @Bean(name = "catalogFlyway", initMethod = "migrate")
    public Flyway catalogFlyway(
            @Qualifier("catalogDataSource") DataSource dataSource,
            @Value("${catalog.flyway.default-schema:catalog}") String schema,
            @Value("${catalog.flyway.locations:classpath:db/migration/catalog}") String locations,
            @Value("${catalog.flyway.table:flyway_schema_history}") String table
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

    @Bean("catalogEntityManagerFactory")
    @DependsOn("catalogFlyway")
    public LocalContainerEntityManagerFactoryBean catalogEntityManagerFactory(
            @Qualifier("catalogDataSource") DataSource catalogDataSource,
            @Value("${catalog.jpa.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}") String dialect,
            @Value("${catalog.jpa.hibernate.hbm2ddl.auto:update}") String hbm2ddlAuto,
            @Value("${catalog.jpa.hibernate.show_sql:true}") String showSql,
            @Value("${catalog.jpa.hibernate.format_sql:true}") String formatSql,
            @Value("${catalog.jpa.hibernate.default_schema:${catalog.flyway.default-schema:catalog}}") String defaultSchema
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.catalog.adapter.persistence.entity", "com.catalog.adapter.persistence.outbox");
        factory.setDataSource(catalogDataSource);
        factory.setPersistenceUnitName("catalog");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.format_sql", formatSql);
        properties.put("hibernate.default_schema", defaultSchema);
        factory.setJpaPropertyMap(properties);

        return factory;
    }

    @Bean("catalogTransactionManager")
    public PlatformTransactionManager catalogTransactionManager(
            @Qualifier("catalogEntityManagerFactory") EntityManagerFactory catalogEntityManagerFactory
    ) {
        return new JpaTransactionManager(catalogEntityManagerFactory);
    }
}

