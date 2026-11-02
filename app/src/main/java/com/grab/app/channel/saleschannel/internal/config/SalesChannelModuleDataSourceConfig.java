package com.grab.app.channel.saleschannel.internal.config;

import com.saleschannel.adapter.persistence.config.SalesChannelPersistenceConfig;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.FilterType;
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
@ComponentScan(
        basePackages = "com.saleschannel.adapter.persistence.mapper",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = ".*MapperImpl"
        )
)
@EnableJpaRepositories(
        basePackages = "com.saleschannel.adapter.persistence.repository",
        entityManagerFactoryRef = "salesChannelEntityManagerFactory",
        transactionManagerRef = "salesChannelTransactionManager"
)
@EnableTransactionManagement
@Import({SalesChannelPersistenceConfig.class, SalesChannelModuleOutboxConfig.class})
public class SalesChannelModuleDataSourceConfig {

    @Bean(name = "salesChannelFlyway", initMethod = "migrate")
    public Flyway salesChannelFlyway(
            @Qualifier("channelDataSource") DataSource dataSource,
            @Value("${saleschannel.flyway.default-schema:saleschannel}") String schema,
            @Value("${saleschannel.flyway.locations:classpath:db/migration/saleschannel}") String locations,
            @Value("${saleschannel.flyway.table:flyway_schema_history}") String table
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

    @Bean("salesChannelEntityManagerFactory")
    @DependsOn("salesChannelFlyway")
    public LocalContainerEntityManagerFactoryBean salesChannelEntityManagerFactory(
            @Qualifier("channelDataSource") DataSource dataSource,
            @Value("${saleschannel.jpa.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}") String dialect,
            @Value("${saleschannel.jpa.hibernate.hbm2ddl.auto:validate}") String hbm2ddlAuto,
            @Value("${saleschannel.jpa.hibernate.show_sql:false}") String showSql,
            @Value("${saleschannel.jpa.hibernate.format_sql:false}") String formatSql,
            @Value("${saleschannel.jpa.hibernate.default_schema:${saleschannel.flyway.default-schema:saleschannel}}") String defaultSchema
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan(
                "com.saleschannel.adapter.persistence.entity",
                "com.saleschannel.adapter.persistence.outbox"
        );
        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("saleschannel");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.format_sql", formatSql);
        properties.put("hibernate.default_schema", defaultSchema);
        factory.setJpaPropertyMap(properties);

        return factory;
    }

    @Bean("salesChannelTransactionManager")
    public PlatformTransactionManager salesChannelTransactionManager(
            @Qualifier("salesChannelEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

