package com.grab.outbox.infrastructure.contract;

import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.outbox.infrastructure.adapter.out.jpa.JpaOutboxEventEntity;
import com.grab.outbox.infrastructure.core.port.in.OutboxBacklog;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class OutboxStoreContract<T extends JpaOutboxEventEntity> extends OutboxClaimStrategyContract<T> {

    @Test
    void flushMakesIdsAvailableAndRetentionDeletesOnlyExpiredPublishedRowsInBatches() {
        Instant cutoff = Instant.parse("2026-11-14T10:00:00Z");
        List<T> expired = List.of(newEvent(), newEvent(), newEvent());
        expired.forEach(row -> row.markPublished(cutoff.minusSeconds(1)));
        T recent = newEvent();
        recent.markPublished(cutoff);
        T pending = newEvent();
        tx.executeWithoutResult(status -> {
            store.saveAll(expired);
            store.saveAll(List.of(recent, pending));
            store.flush();
            assertThat(expired).allSatisfy(row -> assertThat(row.getId()).isNotNull());
        });

        int firstBatch = tx.execute(status -> store.deletePublishedOlderThan(cutoff, 2));
        int lastBatch = tx.execute(status -> store.deletePublishedOlderThan(cutoff, 2));
        OutboxBacklog backlog = tx.execute(status -> store.backlog(List.of(OutboxStatus.NEW)));
        assertThat(firstBatch).isEqualTo(2);
        assertThat(lastBatch).isEqualTo(1);
        assertThat(backlog).isEqualTo(new OutboxBacklog(1, pending.getOccurredAt()));
        boolean recentSurvived = tx.execute(status -> store.findById(recent.getId()).isPresent());
        assertThat(recentSurvived).isTrue();
    }

    @Test
    void replayReturnsOnlyDeadRowsInRangeAndCommitsARequeuedRow() {
        Instant now = Instant.parse("2026-11-14T10:00:00Z");
        T first = newEvent();
        T second = newEvent();
        T pending = newEvent();
        first.markDead(now, "first failed");
        second.markDead(now, "second failed");
        tx.executeWithoutResult(status -> store.saveAll(List.of(first, second, pending)));

        int replayed = tx.execute(status -> {
            List<T> rows = store.findDeadForReplay(first.getId(), pending.getId(), 1);
            assertThat(rows).extracting(OutboxEntry::getId).containsExactly(first.getId());
            rows.forEach(row -> row.requeue(now));
            return rows.size();
        });

        T requeued = tx.execute(status -> store.findById(first.getId()).orElseThrow());
        assertThat(replayed).isEqualTo(1);
        assertThat(requeued.getStatus()).isEqualTo(OutboxStatus.NEW);
        assertThat(requeued.getAttemptCount()).isZero();
        assertThat(requeued.getLastError()).isNull();
        List<T> remaining = tx.execute(status -> store.findDeadForReplay(first.getId(), pending.getId(), 10));
        assertThat(remaining).extracting(OutboxEntry::getId).containsExactly(second.getId());
    }
}
