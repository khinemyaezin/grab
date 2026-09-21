package com.region.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.region.application.exception.RegionServiceException;
import org.springframework.dao.DataAccessException;

import java.util.function.Supplier;

public class RegionPersistenceExecutor implements PersistenceExecutor {

    @Override
    public <T> T query(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataAccessException exception) {
            throw new RegionServiceException("Region persistence query failed for " + resource, exception);
        }
    }

    @Override
    public <T> T command(String resource, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataAccessException exception) {
            throw new RegionServiceException("Region persistence command failed for " + resource, exception);
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
