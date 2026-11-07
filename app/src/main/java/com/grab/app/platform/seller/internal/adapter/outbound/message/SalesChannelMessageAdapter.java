package com.grab.app.platform.seller.internal.adapter.outbound.message;

import com.grab.app.channel.saleschannel.port.message.PublishableApiKeyPort;
import com.grab.app.channel.saleschannel.port.message.SalesChannelManagementPort;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.CreatePublishableKeyMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetSalesChannelMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.ListSalesChannelsMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.ProvisionSalesChannelMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.ResolvePublishableKeyMessageMapper;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort;
import com.grab.framework.id.Id;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SalesChannelMessageAdapter implements SellerSalesChannelOutboundPort {

    private final SalesChannelManagementPort salesChannelManagementPort;
    private final PublishableApiKeyPort publishableApiKeyPort;

    private final ProvisionSalesChannelMessageMapper provisionMapper;
    private final GetSalesChannelMessageMapper getChannelMapper;
    private final ListSalesChannelsMessageMapper listChannelsMapper;
    private final CreatePublishableKeyMessageMapper createKeyMapper;
    private final ResolvePublishableKeyMessageMapper resolveKeyMapper;

    public SalesChannelMessageAdapter(
            SalesChannelManagementPort salesChannelManagementPort,
            PublishableApiKeyPort publishableApiKeyPort,
            ProvisionSalesChannelMessageMapper provisionMapper,
            GetSalesChannelMessageMapper getChannelMapper,
            ListSalesChannelsMessageMapper listChannelsMapper,
            CreatePublishableKeyMessageMapper createKeyMapper,
            ResolvePublishableKeyMessageMapper resolveKeyMapper
    ) {
        this.salesChannelManagementPort = salesChannelManagementPort;
        this.publishableApiKeyPort = publishableApiKeyPort;
        this.provisionMapper = provisionMapper;
        this.getChannelMapper = getChannelMapper;
        this.listChannelsMapper = listChannelsMapper;
        this.createKeyMapper = createKeyMapper;
        this.resolveKeyMapper = resolveKeyMapper;
    }

    @Override
    public ProvisionOutboundResult provisionMerchantWebsiteChannel(Id merchantId, String countryCode) {
        SalesChannelManagementPort.ProvisionResult result = salesChannelManagementPort.provisionMerchantWebsiteChannel(merchantId, countryCode);
        return provisionMapper.toResult(result);
    }

    @Override
    public Optional<ChannelOutboundResult> findChannelById(String salesChannelId, String merchantId) {
        return salesChannelManagementPort.findChannelById(salesChannelId, merchantId).map(getChannelMapper::toResult);
    }

    @Override
    public List<ChannelOutboundResult> listChannels(String merchantId, int page, int size) {
        List<SalesChannelManagementPort.ChannelMessageResult> results = salesChannelManagementPort.listChannels(merchantId, page, size);
        return listChannelsMapper.toResultList(results);
    }

    @Override
    public PublishableKeyOutboundResult createPublishableKey(CreatePublishableKeyRecord record) {
        PublishableApiKeyPort.CreatePublishableKeyMessage message = createKeyMapper.toMessage(record);
        PublishableApiKeyPort.PublishableKeyResult result = publishableApiKeyPort.createPublishableKey(message);
        return createKeyMapper.toResult(result);
    }

    @Override
    public void revokePublishableKey(Id salesChannelId, Id keyId) {
        publishableApiKeyPort.revokePublishableKey(salesChannelId, keyId);
    }

    @Override
    public Optional<KeyResolutionOutboundResult> resolveKey(String rawToken) {
        return publishableApiKeyPort.resolveKey(rawToken).map(resolveKeyMapper::toResult);
    }
}
