package com.grab.outbox.infrastructure.core.port.out;

import java.util.Collection;

public interface OutboxWriter<T> {

    void saveAll(Collection<T> events);

    void flush();
}
