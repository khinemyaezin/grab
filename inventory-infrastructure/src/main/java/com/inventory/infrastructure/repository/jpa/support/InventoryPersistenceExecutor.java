package com.inventory.infrastructure.repository.jpa.support;

import com.grab.framework.support.PersistenceExecutor;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.infrastructure.exception.InventoryInfraError;
import com.inventory.infrastructure.exception.InventoryInfraException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Objects;
import java.util.function.Supplier;

public class InventoryPersistenceExecutor implements PersistenceExecutor {

    private static final Logger log = Loggers.getLogger(InventoryPersistenceExecutor.class);

    @Override
    public <T> T query(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataAccessException ex) {
            log.error("Persistence query failure for resource={}: {}", resource, rootMessage(ex), ex);
            throw internal(resource, ex);
        }
    }

    @Override
    public <T> T command(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataIntegrityViolationException ex) {
            log.warn("Persistence conflict for resource={}: {}", resource, rootMessage(ex), ex);
            throw conflict(resource, ex);
        } catch (DataAccessException ex) {
            log.error("Persistence command failure for resource={}: {}", resource, rootMessage(ex), ex);
            throw internal(resource, ex);
        }
    }

    @Override
    public void command(String resource, Runnable operation) {
        command(resource, () -> {
            operation.run();
            return null;
        });
    }

    /**
     * Handles persistence conflicts such as unique constraint violations.
     */
    private InventoryInfraException conflict(String resource, Exception ex) {
        return new InventoryInfraException(
                new InventoryInfraError.PersistenceConflict(resource, rootMessage(ex)),
                "Persistence conflict for " + resource + ".",
                ex
        );
    }

    /**
     * Handles internal persistence errors such as connection issues.
     */
    private InventoryInfraException internal(String resource, Exception ex) {
        return new InventoryInfraException(
                new InventoryInfraError.PersistenceInternal(resource, rootMessage(ex)),
                "Persistence failure for " + resource + ".",
                ex
        );
    }

    /**
     * Extracts the root cause message from an exception chain.
     */
    private String rootMessage(Throwable t) {
        Throwable c = t;
        while (c.getCause() != null) c = c.getCause();
        return Objects.toString(c.getMessage(), "unknown");
    }
}
