package com.grab.app.account.identity.internal.config;

import com.identity.adapter.persistence.configuration.IdentityPersistenceConfig;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
        basePackages = "com.identity.adapter.persistence.mapper",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = ".*MapperImpl"
        )
)
@EnableJpaRepositories(
        basePackages = "com.identity.adapter.persistence.repository.jpa",
        entityManagerFactoryRef = "identityEntityManagerFactory",
        transactionManagerRef = "identityTransactionManager"
)
@EnableTransactionManagement
@Import({IdentityPersistenceConfig.class, IdentityModuleOutboxConfig.class})
public class IdentityDataSourceConfig {

    @Bean(name = "identityFlyway", initMethod = "migrate")
    @ConditionalOnProperty(prefix = "identity.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Flyway identityFlyway(
            @Qualifier("accountDataSource") DataSource dataSource,
            @Value("${identity.flyway.default-schema:identity}") String schema,
            @Value("${identity.flyway.locations:classpath:db/migration/identity}") String locations,
            @Value("${identity.flyway.table:flyway_schema_history}") String table
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

    @Bean("identityEntityManagerFactory")
    @DependsOn("identityFlyway")
    public LocalContainerEntityManagerFactoryBean identityEntityManagerFactory(
            @Qualifier("accountDataSource") DataSource dataSource,
            @Value("${identity.jpa.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}") String dialect,
            @Value("${identity.jpa.hibernate.hbm2ddl.auto:update}") String hbm2ddlAuto,
            @Value("${identity.jpa.hibernate.default_schema:${identity.flyway.default-schema:identity}}") String defaultSchema
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.identity.adapter.persistence.entity", "com.identity.adapter.persistence.outbox");
        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("identity");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.put("hibernate.default_schema", defaultSchema);
        factory.setJpaPropertyMap(properties);

        return factory;
    }

    @Bean("identityTransactionManager")
    public PlatformTransactionManager identityTransactionManager(
            @Qualifier("identityEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
