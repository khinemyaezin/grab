package com.grab.app.identityaccount.merchant.internal.config;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "merchant", name = "enabled", havingValue = "true")
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
public class MerchantModuleDataSourceConfig {
    @Bean("merchantDataSourceProperties")
    @ConfigurationProperties("merchant.datasource")
    DataSourceProperties properties() { return new DataSourceProperties(); }

    @Bean("merchantDataSource")
    DataSource dataSource(
            @Qualifier("identityAccountSharedDataSource") DataSource sharedDataSource,
            @Qualifier("merchantDedicatedDataSource") org.springframework.beans.factory.ObjectProvider<DataSource> dedicated) {
        return new DelegatingDataSource(dedicated.getIfAvailable(() -> sharedDataSource));
    }

    @Bean(name = "merchantFlyway", initMethod = "migrate")
    Flyway flyway(@Qualifier("merchantDataSource") DataSource dataSource) {
        return Flyway.configure().dataSource(dataSource)
                .locations("classpath:db/migration/merchant")
                .schemas("merchant").defaultSchema("merchant")
                .table("flyway_schema_history_merchant")
                .baselineOnMigrate(false).load();
    }

    @Bean("merchantEntityManagerFactory")
    @DependsOn("merchantFlyway")
    LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("merchantDataSource") DataSource dataSource,
            Environment environment) {
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.merchant.adapter.persistence.entity", "com.merchant.adapter.persistence.outbox");
        factory.setPersistenceUnitName("merchant");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of(
                "hibernate.dialect", environment.getProperty(
                        "merchant.jpa.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"),
                "hibernate.hbm2ddl.auto", environment.getProperty("merchant.jpa.hibernate.hbm2ddl.auto", "validate"),
                "hibernate.default_schema", "merchant"
        ));
        return factory;
    }

    @Bean("merchantTransactionManager")
    PlatformTransactionManager transactionManager(
            @Qualifier("merchantEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
