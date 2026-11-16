package com.inventory.adapter.persistence.outbox.claim;

import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.scheduling.infrastructure.ShedLockSingletonJobExecutor;
import com.grab.scheduling.infrastructure.contract.SingletonJobExecutorContract;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;

class InventorySingletonJobExecutorPostgresTest extends SingletonJobExecutorContract {

    @BeforeAll
    static void createLockTable() {
        JdbcTemplate jdbc = new JdbcTemplate(PostgresDatabase.dataSource());
        jdbc.execute("""
                create schema if not exists inventory;
                create table if not exists inventory.shedlock (
                    name varchar(64) not null primary key,
                    lock_until timestamp(3) not null,
                    locked_at timestamp(3) not null,
                    locked_by varchar(255) not null
                )
                """);
    }

    @Override
    protected SingletonJobExecutor owner() {
        return ShedLockSingletonJobExecutor.forSchema(PostgresDatabase.dataSource(), "inventory", null);
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
