package com.catalog.adapter.persistence.outbox.claim;

import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.scheduling.infrastructure.ShedLockSingletonJobExecutor;
import com.grab.scheduling.infrastructure.contract.SingletonJobExecutorContract;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;

class CatalogSingletonJobExecutorPostgresTest extends SingletonJobExecutorContract {

    @BeforeAll
    static void createLockTable() {
        JdbcTemplate jdbc = new JdbcTemplate(PostgresDatabase.dataSource());
        jdbc.execute("""
                create schema if not exists catalog;
                create table if not exists catalog.shedlock (
                    name varchar(64) not null primary key,
                    lock_until timestamp(3) not null,
                    locked_at timestamp(3) not null,
                    locked_by varchar(255) not null
                )
                """);
    }

    @Override
    protected SingletonJobExecutor owner() {
        return ShedLockSingletonJobExecutor.forSchema(PostgresDatabase.dataSource(), "catalog", null);
    }

    @Override
    protected SingletonJobExecutor competitor() {
        return owner();
    }

    @Override
    protected void awaitLease(Duration duration) throws InterruptedException {
        Thread.sleep(duration.plusMillis(100));
    }
}
