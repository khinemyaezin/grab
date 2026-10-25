package com.grab.app.ordermanagement.cart.internal.adapter;

import com.grab.app.channeldiscovery.saleschannel.port.SalesChannelPort;
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
    private SalesChannelPort salesChannelPort;

    @InjectMocks
    private SalesChannelLookupAdapter adapter;

    @Test
    void findEnabled_channelMissing_returnsEmpty() {
        when(salesChannelPort.find("web-1")).thenReturn(Optional.empty());

        assertThat(adapter.findEnabled("web-1")).isEmpty();
    }

    @Test
    void findEnabled_channelDisabled_returnsEmpty() {
        when(salesChannelPort.find("web-1")).thenReturn(Optional.of(
                new SalesChannelPort.SalesChannelSlice("web-1", "WEBSITE", "DISABLED", "m-1")
        ));

        assertThat(adapter.findEnabled("web-1")).isEmpty();
    }

    @Test
    void findEnabled_channelEnabled_returnsChannelSnapshot() {
        when(salesChannelPort.find("web-1")).thenReturn(Optional.of(
                new SalesChannelPort.SalesChannelSlice("web-1", "WEBSITE", "ENABLED", "m-1")
        ));

        assertThat(adapter.findEnabled("web-1")).hasValueSatisfying(snapshot -> {
            assertThat(snapshot.salesChannelId()).isEqualTo("web-1");
            assertThat(snapshot.type()).isEqualTo("WEBSITE");
        });
    }

    @Test
    void findEnabled_nullType_defaultsToMarketplace() {
        when(salesChannelPort.find("mp-1")).thenReturn(Optional.of(
                new SalesChannelPort.SalesChannelSlice("mp-1", null, "ENABLED", null)
        ));

        assertThat(adapter.findEnabled("mp-1")).hasValueSatisfying(snapshot ->
                assertThat(snapshot.type()).isEqualTo("MARKETPLACE")
        );
    }
}
