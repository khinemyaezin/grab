package com.grab.app.shared.seed;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Executes local development SQL seeds against module databases when running in the 'dev' profile.
 * Scripts are isolated from Flyway production migrations to prevent test/mock data from leaking into production.
 */
@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DevSqlDataSeeder implements ApplicationRunner {

    private static final Logger log = Loggers.getLogger(DevSqlDataSeeder.class);

    private final ObjectProvider<DataSource> identityDataSource;
    private final ObjectProvider<DataSource> merchantDataSource;
    private final ObjectProvider<DataSource> salesChannelDataSource;
    private final ObjectProvider<DataSource> catalogDataSource;
    private final ObjectProvider<DataSource> pricingDataSource;
    private final ObjectProvider<DataSource> inventoryDataSource;
    private final ObjectProvider<DataSource> storefrontQueryDataSource;
    private final ObjectProvider<DataSource> regionDataSource;

    public DevSqlDataSeeder(
            @Qualifier("identityDataSource") ObjectProvider<DataSource> identityDataSource,
            @Qualifier("merchantDataSource") ObjectProvider<DataSource> merchantDataSource,
            @Qualifier("salesChannelDataSource") ObjectProvider<DataSource> salesChannelDataSource,
            @Qualifier("catalogDataSource") ObjectProvider<DataSource> catalogDataSource,
            @Qualifier("pricingDataSource") ObjectProvider<DataSource> pricingDataSource,
            @Qualifier("inventoryDataSource") ObjectProvider<DataSource> inventoryDataSource,
            @Qualifier("storefrontQueryDataSource") ObjectProvider<DataSource> storefrontQueryDataSource,
            @Qualifier("regionDataSource") ObjectProvider<DataSource> regionDataSource
    ) {
        this.identityDataSource = identityDataSource;
        this.merchantDataSource = merchantDataSource;
        this.salesChannelDataSource = salesChannelDataSource;
        this.catalogDataSource = catalogDataSource;
        this.pricingDataSource = pricingDataSource;
        this.inventoryDataSource = inventoryDataSource;
        this.storefrontQueryDataSource = storefrontQueryDataSource;
        this.regionDataSource = regionDataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting local development SQL seeding...");

        executeScript(identityDataSource.getIfAvailable(), "db/dev-seed/01_identity.sql", "identity", "identity");
        executeScript(merchantDataSource.getIfAvailable(), "db/dev-seed/02_merchant.sql", "merchant", "merchant");
        executeScript(salesChannelDataSource.getIfAvailable(), "db/dev-seed/03_saleschannel.sql", "saleschannel", "saleschannel");
        executeScript(catalogDataSource.getIfAvailable(), "db/dev-seed/04_catalog.sql", "catalog", "catalog");
        executeScript(pricingDataSource.getIfAvailable(), "db/dev-seed/05_pricing.sql", "pricing", "pricing");
        executeScript(inventoryDataSource.getIfAvailable(), "db/dev-seed/06_inventory.sql", "inventory", "inventory");
        executeScript(storefrontQueryDataSource.getIfAvailable(), "db/dev-seed/07_storefrontquery.sql", "discovery", "discovery");
        executeScript(regionDataSource.getIfAvailable(), "db/dev-seed/08_region.sql", "region", "region");

        log.info("Local development SQL seeding completed successfully.");
    }

    private void executeScript(DataSource dataSource, String scriptPath, String moduleName, String schema) {
        if (dataSource == null) {
            log.info("DataSource for module '{}' is not present. Skipping dev seed for '{}'.", moduleName, scriptPath);
            return;
        }

        try {
            var resource = new ClassPathResource(scriptPath);
            if (!resource.exists()) {
                log.warn("Dev seed script '{}' not found for module '{}'.", scriptPath, moduleName);
                return;
            }

            var populator = new ResourceDatabasePopulator();
            populator.addScript(resource);
            populator.setContinueOnError(false);
            try (var connection = dataSource.getConnection()) {
                String previousSchema = connection.getSchema();
                try {
                    connection.setSchema(schema);
                    populator.populate(connection);
                } finally {
                    connection.setSchema(previousSchema);
                }
            }
            log.info("Successfully executed dev SQL seed script '{}' on module '{}'.", scriptPath, moduleName);
        } catch (Exception ex) {
            log.warn("Failed executing dev seed script '{}' on module '{}': {}", scriptPath, moduleName, ex.getMessage());
        }
    }
}
