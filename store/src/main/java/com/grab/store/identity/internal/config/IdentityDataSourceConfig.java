package com.grab.store.identity.internal.config;

import jakarta.persistence.EntityManagerFactory;
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

import org.flywaydb.core.Flyway;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;
import java.util.Map;
import java.util.HashMap;

@Configuration
@ComponentScan("com.identity.infrastructure")
@EnableJpaRepositories(
        basePackages = "com.identity.infrastructure.repository.jpa",
        entityManagerFactoryRef = "identityEntityManagerFactory",
        transactionManagerRef = "identityTransactionManager")
public class IdentityDataSourceConfig {

    @Bean("identityDataSourceProperties")
    @ConfigurationProperties("identity.datasource")
    DataSourceProperties properties() {
        return new DataSourceProperties();
    }

    @Bean("identityDataSource")
    DataSource dataSource(@Qualifier("identityDataSourceProperties") DataSourceProperties p) {
        return p.initializeDataSourceBuilder().build();
    }

    @Bean("identityEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean emf(@Qualifier("identityDataSource") DataSource ds, Environment env) {
        var f = new LocalContainerEntityManagerFactoryBean();
        f.setDataSource(ds);
        f.setPackagesToScan("com.identity.infrastructure.entity", "com.identity.infrastructure.outbox");
        f.setPersistenceUnitName("identity");
        f.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        f.setJpaPropertyMap(
                Map.of("hibernate.dialect", env.getProperty("identity.jpa.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"),
                        "hibernate.hbm2ddl.auto", env.getProperty("identity.jpa.hibernate.hbm2ddl.auto", "update")));
        return f;
    }

    @Bean("identityTransactionManager")
    PlatformTransactionManager tx(@Qualifier("identityEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(initMethod = "migrate")
    @ConditionalOnProperty(prefix = "identity.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Flyway identityFlyway(@Qualifier("identityDataSource") DataSource dataSource, Environment env) {
        Map<String, String> placeholders = new HashMap<>();
        String adminEmail = env.getProperty("identity.seed.admin-email");
        String adminPassword = env.getProperty("identity.seed.admin-password");
        
        if (adminEmail != null && !adminEmail.isBlank() && adminPassword != null && !adminPassword.isBlank()) {
            placeholders.put("adminEmail", adminEmail.toLowerCase());
            placeholders.put("adminPasswordHash", new BCryptPasswordEncoder().encode(adminPassword));
            placeholders.put("seedAdmin", "true");
        } else {
            placeholders.put("seedAdmin", "false");
        }

        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/identity")
                .baselineOnMigrate(true)
                .placeholders(placeholders)
                .load();
    }
}
