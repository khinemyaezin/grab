package com.grab.store.merchant.internal.config;

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

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "merchant", name = "enabled", havingValue = "true")
@ComponentScan("com.merchant.infrastructure")
@EnableJpaRepositories(
        basePackages = "com.merchant.infrastructure.repository.jpa",
        entityManagerFactoryRef = "merchantEntityManagerFactory",
        transactionManagerRef = "merchantTransactionManager"
)
public class MerchantModuleDataSourceConfig {
    @Bean("merchantDataSourceProperties")
    @ConfigurationProperties("merchant.datasource")
    DataSourceProperties properties() { return new DataSourceProperties(); }

    @Bean("merchantDataSource")
    DataSource dataSource(@Qualifier("merchantDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "merchantFlyway", initMethod = "migrate")
    Flyway flyway(@Qualifier("merchantDataSource") DataSource dataSource) {
        return Flyway.configure().dataSource(dataSource)
                .locations("classpath:db/migration/merchant")
                .baselineOnMigrate(true).load();
    }

    @Bean("merchantEntityManagerFactory")
    @DependsOn("merchantFlyway")
    LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("merchantDataSource") DataSource dataSource,
            Environment environment) {
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.merchant.infrastructure.entity", "com.merchant.infrastructure.outbox");
        factory.setPersistenceUnitName("merchant");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of(
                "hibernate.dialect", environment.getProperty(
                        "merchant.jpa.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"),
                "hibernate.hbm2ddl.auto", environment.getProperty("merchant.jpa.hibernate.hbm2ddl.auto", "validate")
        ));
        return factory;
    }

    @Bean("merchantTransactionManager")
    PlatformTransactionManager transactionManager(
            @Qualifier("merchantEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
