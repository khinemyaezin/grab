package com.grab.store.workflows.internal.config;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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
@ComponentScan(basePackages = "com.grab.workflow.infrastructure")
@EnableJpaRepositories(
        basePackages = "com.grab.workflow.infrastructure.repository.jpa",
        entityManagerFactoryRef = "workflowsEntityManagerFactory",
        transactionManagerRef = "workflowsTransactionManager"
)
@EnableTransactionManagement
public class WorkflowsModuleDataSourceConfig {

    private final Environment environment;

    public WorkflowsModuleDataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean("workflowsDataSourceProperties")
    @ConfigurationProperties("workflows.datasource")
    public DataSourceProperties workflowsDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("workflowsDataSource")
    public DataSource workflowsDataSource(
            @Qualifier("workflowsDataSourceProperties") DataSourceProperties workflowsDataSourceProperties
    ) {
        return workflowsDataSourceProperties.initializeDataSourceBuilder().build();
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

    @Bean(name = "workflowsFlyway", initMethod = "migrate")
    @ConditionalOnProperty(prefix = "workflows.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Flyway workflowsFlyway(@Qualifier("workflowsDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/workflows")
                .baselineOnMigrate(true)
                .load();
    }
}
