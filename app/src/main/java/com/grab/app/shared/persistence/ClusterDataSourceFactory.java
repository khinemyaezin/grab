package com.grab.app.shared.persistence;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

/** Shared construction helper for physical cluster and optional dedicated pools. */
public final class ClusterDataSourceFactory {
    private ClusterDataSourceFactory() { }

    public static HikariDataSource create(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}
