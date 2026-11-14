package com.grab.scheduling.infrastructure;

import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.scheduling.infrastructure.contract.SingletonJobExecutorContract;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;
import java.util.Map;
import javax.sql.DataSource;

abstract class ShedLockSingletonJobExecutorPostgresTest extends SingletonJobExecutorContract {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource());
        jdbc.execute("""
                create table shedlock (
                    name varchar(64) not null primary key,
                    lock_until timestamp not null,
                    locked_at timestamp not null,
                    locked_by varchar(255) not null
                )
                """);
    }

    private SingletonJobExecutor owner;
    private SingletonJobExecutor competitor;

    @BeforeEach
    void createExecutors() {
        StandardEnvironment properties = new StandardEnvironment();
        properties.getPropertySources().addFirst(new MapPropertySource("contract",
                Map.of("contract.scheduling.lock-provider", provider())));
        owner = ShedLockSingletonJobExecutor.fromProperties(dataSource(), "", properties, "contract.scheduling", null);
        competitor = ShedLockSingletonJobExecutor.fromProperties(dataSource(), "", properties, "contract.scheduling", null);
    }

    protected abstract String provider();

    @Override
    protected SingletonJobExecutor owner() {
        return owner;
    }

    @Override
    protected SingletonJobExecutor competitor() {
        return competitor;
    }

    @Override
    protected void awaitLease(Duration duration) throws InterruptedException {
        Thread.sleep(duration.plusMillis(100));
    }

    private static DataSource dataSource() {
        return new DriverManagerDataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }
}

class JdbcSingletonJobExecutorPostgresTest extends ShedLockSingletonJobExecutorPostgresTest {

    @Override
    protected String provider() {
        return "jdbc";
    }
}

class JdbcAppTimeSingletonJobExecutorPostgresTest extends ShedLockSingletonJobExecutorPostgresTest {

    @Override
    protected String provider() {
        return "jdbc-app-time";
    }
}
