package com.grab.app.identityaccount.identity.internal.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
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
        transactionManagerRef = "identityTransactionManager")
public class IdentityDataSourceConfig {

    @Bean("identityDataSourceProperties")
    @ConfigurationProperties("identity.datasource")
    DataSourceProperties properties() {
        return new DataSourceProperties();
    }

    @Bean("identityDataSource")
    DataSource dataSource(
            @Qualifier("identityAccountSharedDataSource") DataSource sharedDataSource,
            @Qualifier("identityDedicatedDataSource") org.springframework.beans.factory.ObjectProvider<DataSource> dedicated) {
        return new DelegatingDataSource(dedicated.getIfAvailable(() -> sharedDataSource));
    }

    @Bean("identityEntityManagerFactory")
    @DependsOn("identityFlyway")
    LocalContainerEntityManagerFactoryBean emf(@Qualifier("identityDataSource") DataSource ds, Environment env) {
        var f = new LocalContainerEntityManagerFactoryBean();
        f.setDataSource(ds);
        f.setPackagesToScan("com.identity.adapter.persistence.entity", "com.identity.adapter.persistence.outbox");
        f.setPersistenceUnitName("identity");
        f.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        f.setJpaPropertyMap(
                Map.of("hibernate.dialect", env.getProperty("identity.jpa.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"),
                        "hibernate.hbm2ddl.auto", env.getProperty("identity.jpa.hibernate.hbm2ddl.auto", "validate"),
                        "hibernate.default_schema", "identity"));
        return f;
    }

    @Bean("identityTransactionManager")
    PlatformTransactionManager tx(@Qualifier("identityEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(initMethod = "migrate")
    public Flyway identityFlyway(@Qualifier("identityDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/identity")
                .schemas("identity").defaultSchema("identity")
                .table("flyway_schema_history_identity")
                .baselineOnMigrate(false)
                .load();
    }

}
