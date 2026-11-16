package com.grab.outbox.infrastructure.inbox;

import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.outbox.OutboxDeliveryContext;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AbstractIntegrationEventListenerTest {

    private final PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
    private final ProcessedEventRepository inbox = mock(ProcessedEventRepository.class);
    private final TestListener listener = new TestListener(manager, inbox);

    @Test
    void missingContextFailsBeforeStartingTransaction() {
        Runnable action = mock(Runnable.class);
        assertThrows(IllegalStateException.class, () -> listener.receive(action));
        verifyNoInteractions(manager, inbox, action);
    }

    @Test
    void nestedDeliveryRestoresContextAfterProcessingFails() {
        UUID outer = UUID.randomUUID();
        UUID inner = UUID.randomUUID();
        TransactionStatus status = mock(TransactionStatus.class);
        when(manager.getTransaction(isA(TransactionTemplate.class))).thenReturn(status);
        when(inbox.markProcessed("test.consumer", inner)).thenReturn(true);

        OutboxDeliveryContext.run(outer, () -> {
            assertThrows(IllegalStateException.class, () -> OutboxDeliveryContext.run(inner,
                    () -> listener.receive(() -> {
                        assertEquals(inner, OutboxDeliveryContext.currentEventId().orElseThrow());
                        throw new IllegalStateException("command failed");
                    })));
            assertEquals(outer, OutboxDeliveryContext.currentEventId().orElseThrow());
        });
        assertTrue(OutboxDeliveryContext.currentEventId().isEmpty());
    }

    private static class TestListener extends AbstractIntegrationEventListener {
        TestListener(PlatformTransactionManager manager, ProcessedEventRepository inbox) {
            super(manager, inbox);
        }

        void receive(Runnable action) {
            processDelivery("test.consumer", action);
        }
    }
}
