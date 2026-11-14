package com.grab.app.shared.scheduling;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import javax.sql.DataSource;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ShedLockSingletonJobExecutorConfigTest {

    private final DataSource dataSource = mock(DataSource.class);

    @Test
    void lockProviderTypeParsing() {
        assertEquals(LockProviderType.JDBC, LockProviderType.parse("jdbc"));
        assertEquals(LockProviderType.JDBC_APP_TIME, LockProviderType.parse("JDBC_APP_TIME"));
        assertEquals(LockProviderType.JDBC_APP_TIME, LockProviderType.parse(" jdbc-app-time "));
    }

    @Test
    void fromProperties_acceptsDefaultsAndExplicitValues() {
        StandardEnvironment empty = environment(Map.of());
        StandardEnvironment explicit = environment(Map.of(
                "catalog.scheduling.lock-provider", "jdbc-app-time",
                "catalog.scheduling.lock-table", "catalog_db.shedlock"));

        assertNotNull(ShedLockSingletonJobExecutor.fromProperties(
                dataSource, "catalog", empty, "catalog.scheduling", new SimpleMeterRegistry()));
        assertNotNull(ShedLockSingletonJobExecutor.fromProperties(
                dataSource, "catalog", explicit, "catalog.scheduling", new SimpleMeterRegistry()));
    }

    @Test
    void fromProperties_rejectsUnknownProviderWithPropertyName() {
        StandardEnvironment invalid = environment(Map.of("catalog.scheduling.lock-provider", "zookeeper"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                ShedLockSingletonJobExecutor.fromProperties(
                        dataSource, "catalog", invalid, "catalog.scheduling", new SimpleMeterRegistry()));

        assertTrue(error.getMessage().contains("catalog.scheduling.lock-provider"));
        assertTrue(error.getMessage().contains("jdbc, jdbc-app-time"));
    }

    @Test
    void settingsRejectBlankTable() {
        assertThrows(IllegalArgumentException.class, () -> new LockProviderSettings(LockProviderType.JDBC, " "));
    }

    private static StandardEnvironment environment(Map<String, Object> values) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", values));
        return environment;
    }
}
