package com.grab.app.catalog.pricing.internal.adapter.outbound.mapper;

import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.mapper.CentralMapperConfig;
import com.grab.app.catalog.pricing.port.PricingQuoteQuery.BatchQuoteRequest;
import com.grab.app.catalog.pricing.port.PricingQuoteQuery.BatchQuoteResult;
import com.grab.app.catalog.pricing.port.PricingQuoteQuery.QuotedPrice;
import com.pricing.application.model.read.QuoteVariantPriceQuery;
import com.pricing.application.model.read.QuoteVariantPriceResult;
import com.pricing.application.model.read.QuoteVariantPricesQuery;
import com.pricing.application.model.read.QuoteVariantPricesResult;
import org.mapstruct.Mapper;

import java.util.LinkedHashMap;
import java.util.Map;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class PricingQuoteQueryMapper {

    public abstract QuoteVariantPriceQuery toQuery(
            String variantId,
            String currencyCode,
            int quantity,
            String salesChannelId,
            String regionId
    );

    public abstract QuoteVariantPricesQuery toBatchQuery(BatchQuoteRequest request);

    public abstract QuotedPrice toQuotedPrice(QuoteVariantPriceResult quoted);

    public abstract QuotedPrice toQuotedPrice(QuoteVariantPricesResult.VariantQuote quoted);

    public BatchQuoteResult toBatchResult(QuoteVariantPricesResult result) {
        Map<String, QuotedPrice> quotes = new LinkedHashMap<>();
        if (result != null) {
            for (Map.Entry<Id, QuoteVariantPricesResult.VariantQuote> entry
                    : result.quotes().entrySet()) {
                String variantValue = entry.getKey() == null ? null : entry.getKey().getValue();
                QuotedPrice quotedPrice = toQuotedPrice(entry.getValue());
                quotes.put(variantValue, quotedPrice);
            }
        }
        return new BatchQuoteResult(quotes);
    }
}
