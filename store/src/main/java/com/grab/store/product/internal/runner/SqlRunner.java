package com.grab.store.product.internal.runner;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class SqlRunner implements CommandLineRunner {
    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;

    public SqlRunner(@Qualifier("productDataSource") DataSource productDataSource,
                     ResourceLoader resourceLoader) {
        this.dataSource = productDataSource;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(String... args) throws Exception {
        // Load the SQL file from resources
        Resource category = resourceLoader.getResource("classpath:mock-category.sql");
        Resource option = resourceLoader.getResource("classpath:mock-variant-type-and-option.sql");
        // Execute the SQL script
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, category);
            ScriptUtils.executeSqlScript(connection, option);
            System.out.println("SQL script executed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to execute SQL script: " + e.getMessage());
        }
    }
}
