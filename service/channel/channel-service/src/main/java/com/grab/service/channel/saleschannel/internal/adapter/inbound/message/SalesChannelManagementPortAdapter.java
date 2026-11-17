package com.grab.service.channel.saleschannel.internal.adapter.inbound.message;

import com.grab.service.channel.saleschannel.internal.adapter.inbound.message.mapper.GetSalesChannelPortMapper;
import com.grab.service.channel.saleschannel.internal.adapter.inbound.message.mapper.ListSalesChannelsPortMapper;
import com.grab.service.channel.saleschannel.internal.adapter.inbound.message.mapper.ProvisionSalesChannelPortMapper;
import com.grab.service.contract.message.channel.saleschannel.SalesChannelManagementPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.saleschannel.application.model.read.GetSalesChannelQuery;
import com.saleschannel.application.model.read.ListSalesChannelsQuery;
import com.saleschannel.application.model.read.SalesChannelResult;
import com.saleschannel.application.model.write.ProvisionMerchantWebsiteChannelCommand;
import com.saleschannel.application.model.write.ProvisionSalesChannelResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import com.grab.rpc.adapter.RmiExport;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RmiExport
public class SalesChannelManagementPortAdapter implements SalesChannelManagementPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final ProvisionSalesChannelPortMapper provisionMapper;
    private final GetSalesChannelPortMapper getChannelMapper;
    private final ListSalesChannelsPortMapper listChannelsMapper;

    public SalesChannelManagementPortAdapter(
            @Qualifier("channelCommandBus") CommandBus commandBus,
            @Qualifier("channelQueryBus") QueryBus queryBus,
            ProvisionSalesChannelPortMapper provisionMapper,
            GetSalesChannelPortMapper getChannelMapper,
            ListSalesChannelsPortMapper listChannelsMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.provisionMapper = provisionMapper;
        this.getChannelMapper = getChannelMapper;
        this.listChannelsMapper = listChannelsMapper;
    }

    @Override
    public ProvisionResult provisionMerchantWebsiteChannel(Id merchantId, String countryCode) {
        ProvisionMerchantWebsiteChannelCommand command = provisionMapper.toCommand(merchantId, countryCode);
        ProvisionSalesChannelResult result = commandBus.dispatch(command);
        return provisionMapper.toResponse(result);
    }

    @Override
    public ChannelMessageResult findChannelById(String salesChannelId, String merchantId) {
        GetSalesChannelQuery query = getChannelMapper.toQuery(salesChannelId, merchantId);
        SalesChannelResult result = queryBus.dispatch(query);
        return getChannelMapper.toResponse(result);
    }

    @Override
    public List<ChannelMessageResult> listChannels(String merchantId, int page, int size) {
        ListSalesChannelsQuery query = listChannelsMapper.toQuery(merchantId, page, size);
        Page<SalesChannelResult> pageResult = queryBus.dispatch(query);
        if (pageResult == null || pageResult.isEmpty()) {
            return Collections.emptyList();
        }
        return pageResult.map(listChannelsMapper::toResponse).getContent();
    }
}
