package com.grab.store.product.internal.runner;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class SqlRunner implements CommandLineRunner {

    public SqlRunner(@Qualifier("productDataSource") DataSource productDataSource,
                     ResourceLoader resourceLoader) {
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
