package com.grab.service.channel.saleschannel.internal.adapter.inbound.message;

import com.grab.framework.id.impl.CommonId;
import com.grab.service.channel.TestSupport;
import com.grab.service.channel.saleschannel.internal.adapter.inbound.message.mapper.GetSalesChannelPortMapper;
import com.grab.service.channel.saleschannel.internal.adapter.inbound.message.mapper.ListSalesChannelsPortMapper;
import com.grab.service.channel.saleschannel.internal.adapter.inbound.message.mapper.ProvisionSalesChannelPortMapper;
import com.saleschannel.application.model.read.GetSalesChannelQuery;
import com.saleschannel.application.model.write.ProvisionMerchantWebsiteChannelCommand;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SalesChannelManagementPortAdapterTest {

    @Test
    void dispatchesReadAndWriteOperationsOnChannelBusesAndPreservesNullableResults() {
        TestSupport.RecordingCommandBus commandBus = new TestSupport.RecordingCommandBus();
        TestSupport.RecordingQueryBus queryBus = new TestSupport.RecordingQueryBus();
        SalesChannelManagementPortAdapter adapter = new SalesChannelManagementPortAdapter(
                commandBus,
                queryBus,
                TestSupport.mapper(ProvisionSalesChannelPortMapper.class),
                TestSupport.mapper(GetSalesChannelPortMapper.class),
                TestSupport.mapper(ListSalesChannelsPortMapper.class));

        adapter.provisionMerchantWebsiteChannel(new CommonId("merchant-1"), "VN");
        assertThat(commandBus.dispatchedCommand()).isInstanceOf(ProvisionMerchantWebsiteChannelCommand.class);

        assertThat(adapter.findChannelById("channel-1", "merchant-1")).isNull();
        assertThat(queryBus.dispatchedQuery()).isInstanceOf(GetSalesChannelQuery.class);
    }
}
