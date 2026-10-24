package com.grab.discovery.adapter.persistence.adapter;

import com.grab.discovery.adapter.persistence.exception.DiscoveryInfraError;
import com.grab.discovery.adapter.persistence.exception.DiscoveryInfraException;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Objects;
import java.util.function.Supplier;

public class DiscoveryPersistenceExecutor implements PersistenceExecutor {

    private static final Logger log = Loggers.getLogger(DiscoveryPersistenceExecutor.class);

    @Override
    public <T> T query(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataAccessException ex) {
            String message = rootMessage(ex);
            log.error("Persistence query failure for resource={}: {}", resource, message, ex);
            throw internal(resource, ex);
        }
    }

    @Override
    public <T> T command(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataIntegrityViolationException | OptimisticLockingFailureException ex) {
            String message = rootMessage(ex);
            log.warn("Persistence conflict for resource={}: {}", resource, message, ex);
            throw conflict(resource, ex);
        } catch (DataAccessException ex) {
            String message = rootMessage(ex);
            log.error("Persistence command failure for resource={}: {}", resource, message, ex);
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

    private DiscoveryInfraException conflict(String resource, Exception ex) {
        String message = rootMessage(ex);
        DiscoveryInfraError.PersistenceConflict error = new DiscoveryInfraError.PersistenceConflict(resource, message);
        String defaultMessage = "Persistence conflict for " + resource + ".";
        return new DiscoveryInfraException(error, defaultMessage, ex);
    }

    private DiscoveryInfraException internal(String resource, Exception ex) {
        String message = rootMessage(ex);
        DiscoveryInfraError.PersistenceInternal error = new DiscoveryInfraError.PersistenceInternal(resource, message);
        String defaultMessage = "Persistence failure for " + resource + ".";
        return new DiscoveryInfraException(error, defaultMessage, ex);
    }

    private String rootMessage(Throwable t) {
        Throwable current = t;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return Objects.toString(current.getMessage(), "unknown");
    }
}
