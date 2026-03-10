package com.grab.framework.support;

import java.util.function.Supplier;

public interface PersistenceExecutor {
    <T> T query(String resource, Supplier<T> operation);
    <T> T command(String resource, Supplier<T> operation);
    void command(String resource, Runnable operation);
}
