package com.grab.store.inventory.internal.config;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.domain.service.InventoryAllocationService;
import com.inventory.domain.service.ReorderService;
import com.inventory.domain.service.impl.DefaultInventoryAllocationService;
import com.inventory.domain.service.impl.DefaultReorderService;
import com.inventory.infrastructure.mapper.jpa.InventoryItemEntityMapper;
import com.inventory.infrastructure.mapper.jpa.InventoryItemMapper;
import com.inventory.infrastructure.mapper.jpa.InventoryJpaAssembler;
import com.inventory.infrastructure.mapper.jpa.impl.InventoryJpaAssemblerImpl;
import jakarta.persistence.EntityManagerFactory;
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

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan(basePackages = "com.inventory.infrastructure")
@EnableJpaRepositories(
        basePackages = "com.inventory.infrastructure.repository.jpa",
        entityManagerFactoryRef = "inventoryEntityManagerFactory",
        transactionManagerRef = "inventoryTransactionManager"
)
@EnableTransactionManagement
public class InventoryModuleDataSourceConfig {

    private final Environment environment;

    public InventoryModuleDataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean("inventoryDataSourceProperties")
    @ConfigurationProperties("inventory.datasource")
    public DataSourceProperties inventoryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("inventoryDataSource")
    public DataSource inventoryDataSource(@Qualifier("inventoryDataSourceProperties") DataSourceProperties inventoryDataSourceProperties) {
        return inventoryDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean("inventoryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean inventoryEntityManagerFactory(@Qualifier("inventoryDataSource") DataSource inventoryDataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.inventory.infrastructure");
        factory.setDataSource(inventoryDataSource);
        factory.setPersistenceUnitName("inventory");
        factory.setJpaPropertyMap(hibernateProperties());

        return factory;
    }

    @Bean("inventoryTransactionManager")
    public PlatformTransactionManager inventoryTransactionManager(
            @Qualifier("inventoryEntityManagerFactory") EntityManagerFactory inventoryEntityManagerFactory) {
        return new JpaTransactionManager(inventoryEntityManagerFactory);
    }

    private Map<String, Object> hibernateProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", environment.getProperty("inventory.jpa.hibernate.dialect"));
        properties.put("hibernate.hbm2ddl.auto", environment.getProperty("inventory.jpa.hibernate.hbm2ddl.auto"));
        properties.put("hibernate.show_sql", environment.getProperty("inventory.jpa.hibernate.show_sql"));
        properties.put("hibernate.format_sql", environment.getProperty("inventory.jpa.hibernate.format_sql"));
        return properties;
    }
}
