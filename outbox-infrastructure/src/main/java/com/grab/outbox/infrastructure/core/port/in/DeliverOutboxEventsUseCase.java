package com.grab.outbox.infrastructure.core.port.in;

import com.grab.framework.outbox.ClaimedOutboxEvent;

public interface DeliverOutboxEventsUseCase<ID> {

    void pollOnce();

    void deliverHot(ID id);

    void deliverClaimed(ClaimedOutboxEvent<ID> claim);

    void cleanupPublished();
}
