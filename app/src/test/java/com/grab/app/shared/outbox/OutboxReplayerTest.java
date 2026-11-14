package com.grab.app.shared.outbox;

import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxReplayerTest {

    @SuppressWarnings("unchecked")
    private final AbstractOutboxProcessor<?, Long> catalog = mock(AbstractOutboxProcessor.class);

    @Test
    void replaysThroughTheModuleProcessorWithBoundedLimit() {
        when(catalog.module()).thenReturn("catalog");
        when(catalog.replayDead(10L, 20L, OutboxReplayer.MAX_LIMIT)).thenReturn(3);
        OutboxReplayer replayer = new OutboxReplayer(List.of(catalog));

        int replayed = replayer.replayDead("catalog", 10L, 20L, 50_000);

        assertThat(replayed).isEqualTo(3);
        verify(catalog).replayDead(10L, 20L, OutboxReplayer.MAX_LIMIT);
    }

    @Test
    void rejectsUnknownModuleAndInvertedRange() {
        when(catalog.module()).thenReturn("catalog");
        OutboxReplayer replayer = new OutboxReplayer(List.of(catalog));

        assertThatThrownBy(() -> replayer.replayDead("nope", 1L, 2L, 10)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> replayer.replayDead("catalog", 5L, 2L, 10)).isInstanceOf(IllegalArgumentException.class);
    }
}
