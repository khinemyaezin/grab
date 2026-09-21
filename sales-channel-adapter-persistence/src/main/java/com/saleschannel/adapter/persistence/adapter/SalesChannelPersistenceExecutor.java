package com.saleschannel.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.saleschannel.adapter.persistence.exception.SalesChannelInfraError;
import com.saleschannel.adapter.persistence.exception.SalesChannelInfraException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.function.Supplier;

public class SalesChannelPersistenceExecutor implements PersistenceExecutor {
    public <T> T query(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataAccessException exception) {
            throw new SalesChannelInfraException(
                    new SalesChannelInfraError.PersistenceInternal(resource),
                    "Sales channel persistence query failed",
                    exception
            );
        }
    }

    public <T> T command(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataIntegrityViolationException exception) {
            throw new SalesChannelInfraException(
                    new SalesChannelInfraError.PersistenceConflict(resource),
                    "Sales channel persistence conflict",
                    exception
            );
        } catch (DataAccessException exception) {
            throw new SalesChannelInfraException(
                    new SalesChannelInfraError.PersistenceInternal(resource),
                    "Sales channel persistence command failed",
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
