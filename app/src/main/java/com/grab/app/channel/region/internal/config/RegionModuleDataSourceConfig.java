package com.grab.app.channel.region.internal.config;

import com.region.adapter.persistence.config.RegionPersistenceConfig;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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
@EnableJpaRepositories(
        basePackages = "com.region.adapter.persistence.repository",
        entityManagerFactoryRef = "regionEntityManagerFactory",
        transactionManagerRef = "regionTransactionManager"
)
@EnableTransactionManagement
@Import({RegionPersistenceConfig.class, RegionModuleOutboxConfig.class})
public class RegionModuleDataSourceConfig {

    @Bean(name = "regionFlyway", initMethod = "migrate")
    public Flyway regionFlyway(
            @Qualifier("channelDataSource") DataSource dataSource,
            @Value("${region.flyway.default-schema:region}") String schema,
            @Value("${region.flyway.locations:classpath:db/migration/region}") String locations,
            @Value("${region.flyway.table:flyway_schema_history}") String table
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

    @Bean("regionEntityManagerFactory")
    @DependsOn("regionFlyway")
    public LocalContainerEntityManagerFactoryBean regionEntityManagerFactory(
            @Qualifier("channelDataSource") DataSource dataSource,
            @Value("${region.jpa.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}") String dialect,
            @Value("${region.jpa.hibernate.hbm2ddl.auto:validate}") String hbm2ddlAuto,
            @Value("${region.jpa.hibernate.show_sql:false}") String showSql,
            @Value("${region.jpa.hibernate.format_sql:false}") String formatSql,
            @Value("${region.jpa.hibernate.default_schema:${region.flyway.default-schema:region}}") String defaultSchema
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan(
                "com.region.adapter.persistence.entity",
                "com.region.adapter.persistence.outbox"
        );
        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("region");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.format_sql", formatSql);
        properties.put("hibernate.default_schema", defaultSchema);
        factory.setJpaPropertyMap(properties);

        return factory;
    }

    @Bean("regionTransactionManager")
    public PlatformTransactionManager regionTransactionManager(
            @Qualifier("regionEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

