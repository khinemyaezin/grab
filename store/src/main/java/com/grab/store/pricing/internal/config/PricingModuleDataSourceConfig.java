package com.grab.store.pricing.internal.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "pricing", name = "enabled", havingValue = "true")
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
public class PricingModuleDataSourceConfig {

    @Bean("pricingDataSourceProperties")
    @ConfigurationProperties("pricing.datasource")
    DataSourceProperties properties() {
        return new DataSourceProperties();
    }

    @Bean("pricingDataSource")
    @ConfigurationProperties("pricing.datasource.hikari")
    HikariDataSource dataSource(@Qualifier("pricingDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "pricingFlyway", initMethod = "migrate")
    Flyway flyway(@Qualifier("pricingDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/pricing")
                .baselineOnMigrate(true)
                .load();
    }

    @Bean("pricingEntityManagerFactory")
    @DependsOn("pricingFlyway")
    LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("pricingDataSource") DataSource dataSource,
            Environment environment) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.pricing.adapter.persistence.entity", "com.pricing.adapter.persistence.outbox");
        factory.setPersistenceUnitName("pricing");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of(
                "hibernate.dialect", environment.getProperty(
                        "pricing.jpa.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"),
                "hibernate.hbm2ddl.auto", environment.getProperty("pricing.jpa.hibernate.hbm2ddl.auto", "validate"),
                "hibernate.show_sql", environment.getProperty("pricing.jpa.hibernate.show_sql", "false"),
                "hibernate.format_sql", environment.getProperty("pricing.jpa.hibernate.format_sql", "false")
        ));
        return factory;
    }

    @Bean("pricingTransactionManager")
    PlatformTransactionManager transactionManager(
            @Qualifier("pricingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
