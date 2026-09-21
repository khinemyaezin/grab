package com.pricing.application.service;

import com.grab.framework.id.IdGenerator;
import com.pricing.application.model.read.CalculatePricesQuery;
import com.pricing.application.model.read.CalculatedPriceSetResult;
import com.pricing.application.model.read.QuoteVariantPriceQuery;
import com.pricing.application.model.read.QuoteVariantPriceResult;
import com.pricing.application.model.read.VariantPriceSetLinkView;
import com.pricing.application.port.inbound.CalculatePricesUseCase;
import com.pricing.application.port.inbound.QuoteVariantPriceUseCase;
import com.pricing.application.port.outbound.VariantPriceSetLinkQueryPort;
import com.pricing.domain.valueobject.PricingAttributeKeys;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class QuoteVariantPriceService implements QuoteVariantPriceUseCase {

    private final VariantPriceSetLinkQueryPort variantPriceSetLinkQueryPort;
    private final CalculatePricesUseCase calculatePricesUseCase;
    private final IdGenerator idGenerator;

    @Override
    public Optional<QuoteVariantPriceResult> execute(QuoteVariantPriceQuery query) {
        List<VariantPriceSetLinkView> links = variantPriceSetLinkQueryPort.findByVariantIds(List.of(query.variantId()));
        if (links.isEmpty()) {
            return Optional.empty();
        }
        VariantPriceSetLinkView link = links.getFirst();
        List<CalculatedPriceSetResult> results = calculatePricesUseCase.execute(new CalculatePricesQuery(
                List.of(idGenerator.convertIdFrom(link.priceSetId())),
                query.currencyCode(),
                query.quantity(),
                Map.of(PricingAttributeKeys.SALES_CHANNEL_ID, query.salesChannelId())
        ));
        if (results.isEmpty() || results.getFirst().calculatedAmount() == null) {
            return Optional.empty();
        }
        CalculatedPriceSetResult result = results.getFirst();
        return Optional.of(new QuoteVariantPriceResult(
                result.calculatedAmount(),
                result.currencyCode(),
                link.priceSetId()
        ));
    }
}
