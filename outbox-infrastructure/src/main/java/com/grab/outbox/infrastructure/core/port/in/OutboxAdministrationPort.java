package com.grab.outbox.infrastructure.core.port.in;

public interface OutboxAdministrationPort {

    int replayDead(long fromId, long toId, int limit);

    OutboxBacklog backlog();
}
