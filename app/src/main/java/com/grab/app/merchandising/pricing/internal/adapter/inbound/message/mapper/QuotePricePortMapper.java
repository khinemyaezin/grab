package com.grab.app.merchandising.pricing.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.pricing.port.message.PricingQuotePort.QuotedPriceResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.pricing.application.model.read.QuoteVariantPricesQuery;
import com.pricing.application.model.read.QuoteVariantPricesResult;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class QuotePricePortMapper {

    public QuoteVariantPricesQuery toQuery(Id variantId, String currencyCode, int quantity, Id salesChannelId, Id regionId) {
        return new QuoteVariantPricesQuery(List.of(variantId), currencyCode, quantity, salesChannelId, regionId);
    }

    public abstract QuotedPriceResult toResponse(QuoteVariantPricesResult.VariantQuote quote);
}

