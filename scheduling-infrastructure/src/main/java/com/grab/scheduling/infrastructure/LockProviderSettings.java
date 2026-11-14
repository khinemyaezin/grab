package com.grab.scheduling.infrastructure;

import java.util.Objects;

/**
 * @param type  which lock provider backs the module's singleton jobs and bootstrap locks
 * @param table qualified ShedLock table, e.g. {@code catalog.shedlock} (PostgreSQL schema) or
 *              {@code catalog_db.shedlock} (MySQL database)
 */
public record LockProviderSettings(LockProviderType type, String table) {

    public LockProviderSettings {
        Objects.requireNonNull(type, "type");
        if (table == null || table.isBlank()) {
            throw new IllegalArgumentException("table must not be blank");
        }
    }
}
