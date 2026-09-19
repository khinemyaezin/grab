package com.grab.store.saleschannel.internal.config;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
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
@ComponentScan(basePackages = "com.saleschannel.infrastructure")
@EnableJpaRepositories(
        basePackages = "com.saleschannel.infrastructure.repository.jpa",
        entityManagerFactoryRef = "salesChannelEntityManagerFactory",
        transactionManagerRef = "salesChannelTransactionManager"
)
@EnableTransactionManagement
public class SalesChannelModuleDataSourceConfig {

    private final Environment environment;

    public SalesChannelModuleDataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean("salesChannelDataSourceProperties")
    @ConfigurationProperties("saleschannel.datasource")
    public DataSourceProperties salesChannelDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("salesChannelDataSource")
    public DataSource salesChannelDataSource(
            @Qualifier("salesChannelDataSourceProperties") DataSourceProperties salesChannelDataSourceProperties
    ) {
        return salesChannelDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean("salesChannelEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean salesChannelEntityManagerFactory(
            @Qualifier("salesChannelDataSource") DataSource salesChannelDataSource
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.saleschannel.infrastructure");
        factory.setDataSource(salesChannelDataSource);
        factory.setPersistenceUnitName("saleschannel");
        factory.setJpaPropertyMap(hibernateProperties());
        return factory;
    }

    @Bean("salesChannelTransactionManager")
    public PlatformTransactionManager salesChannelTransactionManager(
            @Qualifier("salesChannelEntityManagerFactory") EntityManagerFactory salesChannelEntityManagerFactory
    ) {
        return new JpaTransactionManager(salesChannelEntityManagerFactory);
    }

    @Bean(name = "salesChannelFlyway", initMethod = "migrate")
    @ConditionalOnProperty(prefix = "saleschannel.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Flyway salesChannelFlyway(@Qualifier("salesChannelDataSource") DataSource salesChannelDataSource) {
        return Flyway.configure()
                .dataSource(salesChannelDataSource)
                .locations("classpath:db/migration/saleschannel")
                .baselineOnMigrate(true)
                .load();
    }

    private Map<String, Object> hibernateProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", environment.getProperty("saleschannel.jpa.hibernate.dialect"));
        properties.put("hibernate.hbm2ddl.auto", environment.getProperty("saleschannel.jpa.hibernate.hbm2ddl.auto"));
        properties.put("hibernate.show_sql", environment.getProperty("saleschannel.jpa.hibernate.show_sql"));
        properties.put("hibernate.format_sql", environment.getProperty("saleschannel.jpa.hibernate.format_sql"));
        return properties;
    }
}
