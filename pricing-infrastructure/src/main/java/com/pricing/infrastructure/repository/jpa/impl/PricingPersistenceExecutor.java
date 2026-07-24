package com.pricing.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.pricing.infrastructure.exception.PricingInfraError;
import com.pricing.infrastructure.exception.PricingInfraException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.function.Supplier;

public class PricingPersistenceExecutor implements PersistenceExecutor {
    @Override
    public <T> T query(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataAccessException exception) {
            throw new PricingInfraException(
                    new PricingInfraError.PersistenceInternal(resource),
                    "Pricing persistence query failed",
                    exception
            );
        }
    }

    @Override
    public <T> T command(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataIntegrityViolationException exception) {
            throw new PricingInfraException(
                    new PricingInfraError.PersistenceConflict(resource),
                    "Pricing persistence conflict",
                    exception
            );
        } catch (DataAccessException exception) {
            throw new PricingInfraException(
                    new PricingInfraError.PersistenceInternal(resource),
                    "Pricing persistence command failed",
                    exception
            );
        }
    }

    @Override
    public void command(String resource, Runnable operation) {
        command(resource, () -> {
            operation.run();
            return null;
        });
    }
}
