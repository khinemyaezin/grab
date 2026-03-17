package com.grab.store.catalog.internal.runner;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "catalog.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SqlRunner implements CommandLineRunner {

    private static final Logger log = Loggers.getLogger(SqlRunner.class);

    private final DataSource catalogDataSource;

    public SqlRunner(@Qualifier("catalogDataSource") DataSource catalogDataSource) {
        this.catalogDataSource = catalogDataSource;
    }

    @Override
    public void run(String... args) {
        log.info("Running catalog seed data");
        seedReferenceData();
        log.info("Catalog seed data completed");
    }

    private void seedReferenceData() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("sql/catalog-seed-reference-data.sql")
        );
        populator.execute(catalogDataSource);
    }
}
