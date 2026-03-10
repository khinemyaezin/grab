package com.catalog.infrastructure.exception;

import com.catalog.infrastructure.repository.jpa.support.CatalogPersistenceExecutor;
import com.grab.framework.exception.ErrorCategory;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CatalogInfraErrorContractTest {

    @Test
    void persistenceErrors_shouldUseCatInfraCodes() {
        CatalogInfraError.PersistenceConflict conflict = new CatalogInfraError.PersistenceConflict("Product", "duplicate key");
        CatalogInfraError.PersistenceNotFound notFound = new CatalogInfraError.PersistenceNotFound("Category", "cat-1");
        CatalogInfraError.PersistenceInternal internal = new CatalogInfraError.PersistenceInternal("Product", "db down");

        assertEquals(ErrorCategory.CONFLICT, conflict.kind());
        assertEquals("cat.infra.persistence.conflict", conflict.code());

        assertEquals(ErrorCategory.NOT_FOUND, notFound.kind());
        assertEquals("cat.infra.persistence.not_found", notFound.code());

        assertEquals(ErrorCategory.INTERNAL, internal.kind());
        assertEquals("cat.infra.persistence.internal", internal.code());
    }

    @Test
    void persistenceExecutor_conflict_shouldTranslateToCatalogInfraException() {
        CatalogPersistenceExecutor executor = new CatalogPersistenceExecutor();

        CatalogInfraException exception = assertThrows(CatalogInfraException.class,
                () -> executor.command("Product", () -> {
                    throw new DataIntegrityViolationException("duplicate key");
                }));

        assertEquals("cat.infra.persistence.conflict", exception.getMessageSource().code());
        assertEquals("Product", exception.getMessageSource().args().get("resource"));
    }

    @Test
    void persistenceExecutor_internal_shouldTranslateToCatalogInfraException() {
        CatalogPersistenceExecutor executor = new CatalogPersistenceExecutor();

        CatalogInfraException exception = assertThrows(CatalogInfraException.class,
                () -> executor.query("Product", () -> {
                    throw new DataAccessResourceFailureException("db unavailable");
                }));

        assertEquals("cat.infra.persistence.internal", exception.getMessageSource().code());
        assertEquals("Product", exception.getMessageSource().args().get("resource"));
    }
}
