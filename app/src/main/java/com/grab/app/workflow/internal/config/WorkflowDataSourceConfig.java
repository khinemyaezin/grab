package com.grab.app.workflow.internal.config;

import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.scheduling.infrastructure.ShedLockSingletonJobExecutor;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
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

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "com.grab.workflow.infrastructure")
@EnableJpaRepositories(
        basePackages = "com.grab.workflow.infrastructure.repository.jpa",
        entityManagerFactoryRef = "workflowsEntityManagerFactory",
        transactionManagerRef = "workflowsTransactionManager"
)
@EnableTransactionManagement
public class WorkflowDataSourceConfig {

    private final Environment environment;

    public WorkflowDataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean("workflowsDataSourceProperties")
    @ConfigurationProperties("workflows.datasource")
    public DataSourceProperties workflowsDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("workflowsDataSource")
    @ConfigurationProperties("workflows.datasource.hikari")
    public HikariDataSource workflowsDataSource(
            @Qualifier("workflowsDataSourceProperties") DataSourceProperties workflowsDataSourceProperties
    ) {
        return workflowsDataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean("workflowsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean workflowsEntityManagerFactory(
            @Qualifier("workflowsDataSource") DataSource workflowsDataSource
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.grab.workflow.infrastructure");
        factory.setDataSource(workflowsDataSource);
        factory.setPersistenceUnitName("workflows");
        factory.setJpaPropertyMap(hibernateProperties());
        return factory;
    }

    @Bean("workflowsTransactionManager")
    public PlatformTransactionManager workflowsTransactionManager(
            @Qualifier("workflowsEntityManagerFactory") EntityManagerFactory workflowsEntityManagerFactory
    ) {
        return new JpaTransactionManager(workflowsEntityManagerFactory);
    }

    private Map<String, Object> hibernateProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", environment.getProperty("workflows.jpa.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"));
        properties.put("hibernate.hbm2ddl.auto", environment.getProperty("workflows.jpa.hibernate.hbm2ddl.auto", "validate"));
        properties.put("hibernate.show_sql", environment.getProperty("workflows.jpa.hibernate.show_sql", "false"));
        properties.put("hibernate.format_sql", environment.getProperty("workflows.jpa.hibernate.format_sql", "false"));
        return properties;
    }

    @Bean("workflowsSingletonJobExecutor")
    public SingletonJobExecutor workflowsSingletonJobExecutor(
            @Qualifier("workflowsDataSource") DataSource dataSource,
            ObjectProvider<MeterRegistry> meters
    ) {
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource,
                environment.getProperty("workflows.shedlock.schema", ""),
                environment,
                "workflows.scheduling",
                meters.getIfAvailable()
        );
    }

    @Bean(name = "workflowsFlyway", initMethod = "migrate")
    public Flyway workflowsFlyway(@Qualifier("workflowsDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/workflows")
                .baselineOnMigrate(true)
                .load();
    }
}

