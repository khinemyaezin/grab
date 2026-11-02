package com.grab.app.channel.discovery.internal.config;

import com.grab.discovery.adapter.persistence.config.DiscoveryPersistenceConfig;
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
        basePackages = "com.grab.discovery.adapter.persistence.mapper",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = ".*MapperImpl"
        )
)
@EnableJpaRepositories(
        basePackages = "com.grab.discovery.adapter.persistence.repository.jpa",
        entityManagerFactoryRef = "discoveryEntityManagerFactory",
        transactionManagerRef = "discoveryTransactionManager"
)
@EnableTransactionManagement
@Import(DiscoveryPersistenceConfig.class)
public class DiscoveryModuleDataSourceConfig {

    @Bean(name = "discoveryFlyway", initMethod = "migrate")
    public Flyway discoveryFlyway(
            @Qualifier("discoveryDataSource") DataSource dataSource,
            @Value("${storefrontquery.flyway.default-schema:${discovery.flyway.default-schema:discovery}}") String schema,
            @Value("${storefrontquery.flyway.locations:${discovery.flyway.locations:classpath:db/migration/discovery}}") String locations,
            @Value("${storefrontquery.flyway.table:${discovery.flyway.table:flyway_schema_history}}") String table
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

    @Bean("discoveryEntityManagerFactory")
    @DependsOn("discoveryFlyway")
    public LocalContainerEntityManagerFactoryBean discoveryEntityManagerFactory(
            @Qualifier("discoveryDataSource") DataSource dataSource,
            @Value("${storefrontquery.jpa.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}") String dialect,
            @Value("${storefrontquery.jpa.hibernate.hbm2ddl.auto:validate}") String hbm2ddlAuto,
            @Value("${storefrontquery.jpa.hibernate.show_sql:false}") String showSql,
            @Value("${storefrontquery.jpa.hibernate.format_sql:false}") String formatSql,
            @Value("${storefrontquery.jpa.hibernate.default_schema:${storefrontquery.flyway.default-schema:discovery}}") String defaultSchema
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.grab.discovery.adapter.persistence.entity");
        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("discovery");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.format_sql", formatSql);
        properties.put("hibernate.default_schema", defaultSchema);
        factory.setJpaPropertyMap(properties);

        return factory;
    }

    @Bean("discoveryTransactionManager")
    public PlatformTransactionManager discoveryTransactionManager(
            @Qualifier("discoveryEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

