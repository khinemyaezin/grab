package com.grab.store.storefrontquery.internal.adapter;

import com.grab.discovery.application.port.outbound.SalesChannelPublishableKeyPort;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.saleschannel.port.PublishableKeyQuery;
import com.grab.store.storefrontquery.internal.adapter.mapper.SalesChannelPublishableKeyMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesChannelPublishableKeyAdapterTest {
    @Mock
    private PublishableKeyQuery publishableKeys;
    @Mock
    private SalesChannelPublishableKeyMapper mapper;

    @Test
    void resolve_activeKey_mapsIdAndPreservesChannelStatus() {
        PublishableKeyQuery.Resolution resolution = new PublishableKeyQuery.Resolution("channel-1", false, true);
        SalesChannelPublishableKeyPort.ResolvedChannel mapped = new SalesChannelPublishableKeyPort.ResolvedChannel(
                new CommonId("channel-1"), false);
        when(publishableKeys.resolve("pk_token")).thenReturn(Optional.of(resolution));
        when(mapper.toResolvedChannel(resolution)).thenReturn(mapped);

        SalesChannelPublishableKeyAdapter adapter = new SalesChannelPublishableKeyAdapter(publishableKeys, mapper);

        Optional<SalesChannelPublishableKeyPort.ResolvedChannel> result = adapter.resolve("pk_token");

        assertThat(result).contains(mapped);
        assertThat(result.orElseThrow().enabled()).isFalse();
        verify(publishableKeys).resolve("pk_token");
    }

    @Test
    void resolve_inactiveKey_returnsEmpty() {
        PublishableKeyQuery.Resolution resolution = new PublishableKeyQuery.Resolution("channel-1", true, false);
        when(publishableKeys.resolve("pk_token")).thenReturn(Optional.of(resolution));

        SalesChannelPublishableKeyAdapter adapter = new SalesChannelPublishableKeyAdapter(publishableKeys, mapper);

        assertThat(adapter.resolve("pk_token")).isEmpty();
    }
}
