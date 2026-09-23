package com.grab.store.region.internal.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.region.adapter.persistence.repository",
        entityManagerFactoryRef = "regionEntityManagerFactory",
        transactionManagerRef = "regionTransactionManager"
)
@EnableTransactionManagement
public class RegionModuleDataSourceConfig {

    private final Environment environment;

    public RegionModuleDataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean("regionDataSourceProperties")
    @ConfigurationProperties("region.datasource")
    public DataSourceProperties regionDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("regionDataSource")
    @ConfigurationProperties("region.datasource.hikari")
    public HikariDataSource regionDataSource(
            @Qualifier("regionDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean("regionEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean regionEntityManagerFactory(
            @Qualifier("regionDataSource") DataSource dataSource
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.region.adapter.persistence");
        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("region");
        factory.setJpaPropertyMap(hibernateProperties());
        return factory;
    }

    @Bean("regionTransactionManager")
    public PlatformTransactionManager regionTransactionManager(
            @Qualifier("regionEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean(initMethod = "migrate")
    @ConditionalOnProperty(prefix = "region.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Flyway regionFlyway(@Qualifier("regionDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/region")
                .baselineOnMigrate(true)
                .load();
    }

    private Map<String, Object> hibernateProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", environment.getProperty("region.jpa.hibernate.dialect"));
        properties.put("hibernate.hbm2ddl.auto", environment.getProperty("region.jpa.hibernate.hbm2ddl.auto"));
        properties.put("hibernate.show_sql", environment.getProperty("region.jpa.hibernate.show_sql"));
        properties.put("hibernate.format_sql", environment.getProperty("region.jpa.hibernate.format_sql"));
        return properties;
    }
}
