package com.grab.app.channel.discovery.internal.adapter;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.app.channel.saleschannel.port.ChannelRegionQueryPort;
import com.grab.app.channel.discovery.internal.adapter.mapper.SalesChannelRegionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesChannelRegionAdapterTest {
    private static final Id CHANNEL_ID = new CommonId("channel-1");
    private static final Id REGION_ID = new CommonId("region-1");

    @Mock
    private ChannelRegionQueryPort channelRegions;

    @Mock
    private SalesChannelRegionMapper mapper;

    @InjectMocks
    private SalesChannelRegionAdapter adapter;

    @Test
    void findDefaultRegion_delegatesToPortAndMapper() {
        Optional<Id> portResult = Optional.of(REGION_ID);
        when(channelRegions.findDefaultRegion(CHANNEL_ID)).thenReturn(portResult);
        when(mapper.toDefaultRegion(portResult)).thenReturn(portResult);

        Optional<Id> result = adapter.findDefaultRegion(CHANNEL_ID);

        assertThat(result).isEqualTo(portResult);
        verify(channelRegions).findDefaultRegion(CHANNEL_ID);
        verify(mapper).toDefaultRegion(portResult);
    }

    @Test
    void servesRegion_delegatesToPort() {
        when(channelRegions.servesRegion(CHANNEL_ID, REGION_ID)).thenReturn(true);

        boolean result = adapter.servesRegion(CHANNEL_ID, REGION_ID);

        assertThat(result).isTrue();
        verify(channelRegions).servesRegion(CHANNEL_ID, REGION_ID);
    }
}
