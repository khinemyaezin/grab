package com.grab.store.cart.internal.adapter;

import com.grab.store.saleschannel.query.SalesChannelQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesChannelLookupAdapterTest {

    @Mock
    private SalesChannelQueryPort salesChannelQueryPort;

    @InjectMocks
    private SalesChannelLookupAdapter adapter;

    @Test
    void findEnabled_returnsEmptyWhenChannelMissing() {
        when(salesChannelQueryPort.find("web-1")).thenReturn(Optional.empty());

        assertThat(adapter.findEnabled("web-1")).isEmpty();
    }

    @Test
    void findEnabled_returnsEmptyWhenDisabled() {
        when(salesChannelQueryPort.find("web-1")).thenReturn(Optional.of(
                new SalesChannelQueryPort.SalesChannelSlice("web-1", "WEBSITE", "DISABLED", "m-1")
        ));

        assertThat(adapter.findEnabled("web-1")).isEmpty();
    }

    @Test
    void findEnabled_mapsEnabledChannel() {
        when(salesChannelQueryPort.find("web-1")).thenReturn(Optional.of(
                new SalesChannelQueryPort.SalesChannelSlice("web-1", "WEBSITE", "ENABLED", "m-1")
        ));

        assertThat(adapter.findEnabled("web-1")).hasValueSatisfying(snapshot -> {
            assertThat(snapshot.salesChannelId()).isEqualTo("web-1");
            assertThat(snapshot.type()).isEqualTo("WEBSITE");
        });
    }

    @Test
    void findEnabled_defaultsNullTypeToMarketplace() {
        when(salesChannelQueryPort.find("mp-1")).thenReturn(Optional.of(
                new SalesChannelQueryPort.SalesChannelSlice("mp-1", null, "ENABLED", null)
        ));

        assertThat(adapter.findEnabled("mp-1")).hasValueSatisfying(snapshot ->
                assertThat(snapshot.type()).isEqualTo("MARKETPLACE")
        );
    }
}
