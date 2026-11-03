package com.grab.app.merchandising.pricing.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.pricing.port.message.PricingQuotePort.BatchQuoteRequest;
import com.grab.app.merchandising.pricing.port.message.PricingQuotePort.BatchQuoteResult;
import com.grab.app.merchandising.pricing.port.message.PricingQuotePort.QuotedPriceResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.pricing.application.model.read.QuoteVariantPricesQuery;
import com.pricing.application.model.read.QuoteVariantPricesResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class QuoteBatchPricesPortMapper {

    public QuoteVariantPricesQuery toQuery(BatchQuoteRequest message) {
        if (message == null) {
            return null;
        }
        return new QuoteVariantPricesQuery(
                message.variantIds(),
                message.currencyCode(),
                message.quantity(),
                message.salesChannelId(),
                message.regionId()
        );
    }

    public abstract BatchQuoteResult toResponse(QuoteVariantPricesResult result);

    protected abstract QuotedPriceResult toVariantResponse(QuoteVariantPricesResult.VariantQuote quote);
}

