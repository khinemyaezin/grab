package com.cart.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.cart.adapter.persistence.exception.CartInfraError;
import com.cart.adapter.persistence.exception.CartInfraException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.function.Supplier;

public class CartPersistenceExecutor implements PersistenceExecutor {
    public <T> T query(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataAccessException exception) {
            throw new CartInfraException(
                    new CartInfraError.PersistenceInternal(resource),
                    "Cart persistence query failed",
                    exception
            );
        }
    }

    public <T> T command(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataIntegrityViolationException exception) {
            throw new CartInfraException(
                    new CartInfraError.PersistenceConflict(resource),
                    "Cart persistence conflict",
                    exception
            );
        } catch (DataAccessException exception) {
            throw new CartInfraException(
                    new CartInfraError.PersistenceInternal(resource),
                    "Cart persistence command failed",
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
