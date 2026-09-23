package com.customer.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.customer.adapter.persistence.exception.CustomerInfraError;
import com.customer.adapter.persistence.exception.CustomerInfraException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.function.Supplier;

public class CustomerPersistenceExecutor implements PersistenceExecutor {
    public <T> T query(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataAccessException exception) {
            throw new CustomerInfraException(
                    new CustomerInfraError.PersistenceInternal(resource),
                    "Customer persistence query failed",
                    exception
            );
        }
    }

    public <T> T command(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataIntegrityViolationException exception) {
            throw new CustomerInfraException(
                    new CustomerInfraError.PersistenceConflict(resource),
                    "Customer persistence conflict",
                    exception
            );
        } catch (DataAccessException exception) {
            throw new CustomerInfraException(
                    new CustomerInfraError.PersistenceInternal(resource),
                    "Customer persistence command failed",
                    exception
            );
        }
    }

    public void command(String resource, Runnable operation) {
        command(resource, () -> {
            operation.run();
            return null;
        });
    }
}
