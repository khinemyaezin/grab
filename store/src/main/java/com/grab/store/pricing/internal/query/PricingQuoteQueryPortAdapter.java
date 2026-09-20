package com.grab.store.pricing.internal.query;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.IdGenerator;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.grab.store.pricing.query.PricingQuoteQueryPort;
import com.pricing.domain.valueobject.PricingAttributeKeys;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkQueryRepository;
import com.pricing.infrastructure.view.VariantPriceSetLinkView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PricingQuoteQueryPortAdapter implements PricingQuoteQueryPort {
    private final VariantPriceSetLinkQueryRepository variantPriceSetLinkQueryRepository;
    private final QueryBus queryBus;
    private final IdGenerator idGenerator;

    @Override
    @PricingReadTransactional
    public Optional<QuotedPrice> quote(String variantId, String currencyCode, int quantity, String salesChannelId) {
        List<VariantPriceSetLinkView> links = variantPriceSetLinkQueryRepository.findByVariantIds(List.of(variantId));
        if (links.isEmpty()) {
            return Optional.empty();
        }
        VariantPriceSetLinkView link = links.getFirst();
        List<CalculatedPriceSetResult> results = queryBus.dispatch(new CalculatePricesQuery(
                List.of(idGenerator.convertIdFrom(link.priceSetId())),
                currencyCode,
                quantity,
                Map.of(PricingAttributeKeys.SALES_CHANNEL_ID, salesChannelId)
        ));
        if (results.isEmpty() || results.getFirst().calculatedAmount() == null) {
            return Optional.empty();
        }
        CalculatedPriceSetResult result = results.getFirst();
        return Optional.of(new QuotedPrice(result.calculatedAmount(), result.currencyCode(), link.priceSetId()));
    }

    @Override
    @PricingReadTransactional
    public List<String> variantIdsForPriceSet(String priceSetId) {
        return variantPriceSetLinkQueryRepository.findByPriceSetId(priceSetId).stream()
                .map(VariantPriceSetLinkView::variantId)
                .toList();
    }
}
