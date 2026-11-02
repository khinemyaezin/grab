package com.grab.app.account.merchant.internal.config;

import com.merchant.adapter.persistence.config.MerchantPersistenceConfig;
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
        basePackages = "com.merchant.adapter.persistence.mapper",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = ".*MapperImpl"
        )
)
@EnableJpaRepositories(
        basePackages = "com.merchant.adapter.persistence.repository.jpa",
        entityManagerFactoryRef = "merchantEntityManagerFactory",
        transactionManagerRef = "merchantTransactionManager"
)
@EnableTransactionManagement
@Import({MerchantPersistenceConfig.class, MerchantModuleOutboxConfig.class})
public class MerchantModuleDataSourceConfig {

    @Bean(name = "merchantFlyway", initMethod = "migrate")
    public Flyway merchantFlyway(
            @Qualifier("accountDataSource") DataSource dataSource,
            @Value("${merchant.flyway.default-schema:merchant}") String schema,
            @Value("${merchant.flyway.locations:classpath:db/migration/merchant}") String locations,
            @Value("${merchant.flyway.table:flyway_schema_history}") String table
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

    @Bean("merchantEntityManagerFactory")
    @DependsOn("merchantFlyway")
    public LocalContainerEntityManagerFactoryBean merchantEntityManagerFactory(
            @Qualifier("accountDataSource") DataSource dataSource,
            @Value("${merchant.jpa.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}") String dialect,
            @Value("${merchant.jpa.hibernate.hbm2ddl.auto:validate}") String hbm2ddlAuto,
            @Value("${merchant.jpa.hibernate.default_schema:${merchant.flyway.default-schema:merchant}}") String defaultSchema
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.merchant.adapter.persistence.entity", "com.merchant.adapter.persistence.outbox");
        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("merchant");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.put("hibernate.default_schema", defaultSchema);
        factory.setJpaPropertyMap(properties);

        return factory;
    }

    @Bean("merchantTransactionManager")
    public PlatformTransactionManager merchantTransactionManager(
            @Qualifier("merchantEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

