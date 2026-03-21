package com.catalog.infrastructure.repository.jpa.impl;

import com.catalog.infrastructure.exception.CatalogInfraError;
import com.catalog.infrastructure.exception.CatalogInfraException;
import com.grab.framework.support.PersistenceExecutor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Objects;
import java.util.function.Supplier;

public class CatalogPersistenceExecutor implements PersistenceExecutor {

    @Override
    public <T> T query(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataAccessException ex) {
            throw internal(resource, ex);
        }
    }

    @Override
    public <T> T command(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataIntegrityViolationException ex) {
            throw conflict(resource, ex);
        } catch (DataAccessException ex) {
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

    private CatalogInfraException conflict(String resource, Exception ex) {
        return new CatalogInfraException(
                new CatalogInfraError.PersistenceConflict(resource, rootMessage(ex)),
                "Persistence conflict for " + resource + "."
        );
    }

    private CatalogInfraException internal(String resource, Exception ex) {
        return new CatalogInfraException(
                new CatalogInfraError.PersistenceInternal(resource, rootMessage(ex)),
                "Persistence failure for " + resource + "."
        );
    }

    private String rootMessage(Throwable t) {
        Throwable c = t;
        while (c.getCause() != null) c = c.getCause();
        return Objects.toString(c.getMessage(), "unknown");
    }
}
