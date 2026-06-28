package com.merchant.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.merchant.infrastructure.exception.MerchantInfraError;
import com.merchant.infrastructure.exception.MerchantInfraException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.function.Supplier;

public class MerchantPersistenceExecutor implements PersistenceExecutor {
    public <T> T query(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataAccessException exception) {
            throw new MerchantInfraException(
                    new MerchantInfraError.PersistenceInternal(resource),
                    "Merchant persistence query failed", exception
            );
        }
    }

    public <T> T command(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataIntegrityViolationException exception) {
            throw new MerchantInfraException(
                    new MerchantInfraError.PersistenceConflict(resource),
                    "Merchant persistence conflict", exception
            );
        } catch (DataAccessException exception) {
            throw new MerchantInfraException(
                    new MerchantInfraError.PersistenceInternal(resource),
                    "Merchant persistence command failed", exception
            );
        }
    }

    public void command(String resource, Runnable operation) {
        command(resource, () -> { operation.run(); return null; });
    }
}
