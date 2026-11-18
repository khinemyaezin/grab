package com.grab.service.merchandising.pricing.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.merchandising.pricing.PricingQuotePort.QuotedPriceResult;
import com.grab.service.merchandising.internal.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.pricing.application.model.read.QuoteVariantPriceQuery;
import com.pricing.application.model.read.QuoteVariantPriceResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class QuotePricePortMapper {

    public QuoteVariantPriceQuery toQuery(Id variantId, String currencyCode, int quantity, Id salesChannelId, Id regionId) {
        String variant = variantId.getValue();
        String channel = salesChannelId == null ? null : salesChannelId.getValue();
        return new QuoteVariantPriceQuery(variant, currencyCode, quantity, channel, regionId);
    }

    public abstract QuotedPriceResult toResponse(QuoteVariantPriceResult quote);
}

