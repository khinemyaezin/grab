package com.identity.infrastructure.repository.jpa.impl;

import com.identity.infrastructure.exception.IdentityInfraError;
import com.identity.infrastructure.exception.IdentityInfraException;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Objects;
import java.util.function.Supplier;

public class IdentityPersistenceExecutor implements PersistenceExecutor {

    private static final Logger log = Loggers.getLogger(IdentityPersistenceExecutor.class);

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

    private IdentityInfraException conflict(String resource, Exception ex) {
        return new IdentityInfraException(
                new IdentityInfraError.PersistenceConflict(resource, rootMessage(ex)),
                "Persistence conflict for " + resource + ".",
                ex
        );
    }

    private IdentityInfraException internal(String resource, Exception ex) {
        return new IdentityInfraException(
                new IdentityInfraError.PersistenceInternal(resource, rootMessage(ex)),
                "Persistence failure for " + resource + ".",
                ex
        );
    }

    private String rootMessage(Throwable t) {
        Throwable c = t;
        while (c.getCause() != null) c = c.getCause();
        return Objects.toString(c.getMessage(), "unknown");
    }
}
