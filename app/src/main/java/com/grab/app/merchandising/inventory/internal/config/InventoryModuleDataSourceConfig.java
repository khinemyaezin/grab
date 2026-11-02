package com.grab.app.merchandising.inventory.internal.config;

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
@Import({com.inventory.adapter.persistence.config.InventoryPersistenceConfig.class, InventoryModuleOutboxConfig.class})
public class InventoryModuleDataSourceConfig {

    @Bean(name = "inventoryFlyway", initMethod = "migrate")
    public Flyway inventoryFlyway(
            @Qualifier("catalogDataSource") DataSource inventoryDataSource,
            @Value("${inventory.flyway.default-schema:inventory}") String schema,
            @Value("${inventory.flyway.locations:classpath:db/migration/inventory}") String locations,
            @Value("${inventory.flyway.table:flyway_schema_history}") String table
    ) {
        return Flyway.configure()
                .dataSource(inventoryDataSource)
                .schemas(schema)
                .defaultSchema(schema)
                .table(table)
                .locations(locations)
                .baselineOnMigrate(false)
                .cleanDisabled(true)
                .load();
    }

    @Bean("inventoryEntityManagerFactory")
    @DependsOn("inventoryFlyway")
    public LocalContainerEntityManagerFactoryBean inventoryEntityManagerFactory(
            @Qualifier("catalogDataSource") DataSource inventoryDataSource,
            @Value("${inventory.jpa.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}") String dialect,
            @Value("${inventory.jpa.hibernate.hbm2ddl.auto:update}") String hbm2ddlAuto,
            @Value("${inventory.jpa.hibernate.show_sql:true}") String showSql,
            @Value("${inventory.jpa.hibernate.format_sql:true}") String formatSql,
            @Value("${inventory.jpa.hibernate.default_schema:${inventory.flyway.default-schema:inventory}}") String defaultSchema
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.inventory.adapter.persistence.entity", "com.inventory.adapter.persistence.outbox");
        factory.setDataSource(inventoryDataSource);
        factory.setPersistenceUnitName("inventory");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.format_sql", formatSql);
        properties.put("hibernate.default_schema", defaultSchema);
        factory.setJpaPropertyMap(properties);

        return factory;
    }

    @Bean("inventoryTransactionManager")
    public PlatformTransactionManager inventoryTransactionManager(
            @Qualifier("inventoryEntityManagerFactory") EntityManagerFactory inventoryEntityManagerFactory
    ) {
        return new JpaTransactionManager(inventoryEntityManagerFactory);
    }
}

