package com.grab.outbox.infrastructure.core.port.out;

import java.util.function.Supplier;

public interface OutboxTransactions {

    <R> R inNewTransaction(Supplier<R> work);

    <R> R readOnly(Supplier<R> work);
}
