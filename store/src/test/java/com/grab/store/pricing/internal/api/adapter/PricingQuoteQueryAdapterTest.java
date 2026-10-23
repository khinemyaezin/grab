package com.grab.store.pricing.internal.api.adapter;

import com.grab.framework.id.impl.UuidGenerator;
import com.grab.store.pricing.internal.api.adapter.mapper.PricingQuoteQueryMapper;
import com.grab.store.pricing.port.PricingQuoteQuery.QuotedPrice;
import com.pricing.application.model.read.ListVariantIdsForPriceSetQuery;
import com.pricing.application.model.read.QuoteVariantPriceQuery;
import com.pricing.application.model.read.QuoteVariantPriceResult;
import com.pricing.application.port.inbound.ListVariantIdsForPriceSetUseCase;
import com.pricing.application.port.inbound.QuoteVariantPriceUseCase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PricingQuoteQueryAdapterTest {
    @Test
    void quote_validQuery_returnsQuotedPrice() {
        QuoteVariantPriceResult result = new QuoteVariantPriceResult(new BigDecimal("19.99"), null, "USD", "ps-1");
        PricingQuoteQueryAdapter adapter = adapter(query -> Optional.of(result), query -> List.of());

        Optional<QuotedPrice> quoted = adapter.quote("v1", "USD", 1, "sc1");

        assertThat(quoted).isPresent();
        assertThat(quoted.get().amount()).isEqualByComparingTo("19.99");
        assertThat(quoted.get().currencyCode()).isEqualTo("USD");
        assertThat(quoted.get().priceSetId()).isEqualTo("ps-1");
    }

    @Test
    void variantIdsForPriceSet_validPriceSetId_returnsVariantIdList() {
        PricingQuoteQueryAdapter adapter = adapter(query -> Optional.empty(), query -> List.of("v1", "v2"));

        List<String> variantIds = adapter.variantIdsForPriceSet("ps-1");

        assertThat(variantIds).containsExactly("v1", "v2");
    }

    @Test
    void quote_withRegion_shouldPassRegionalIdToUseCase() {
        QuoteVariantPriceQuery[] captured = new QuoteVariantPriceQuery[1];
        QuoteVariantPriceUseCase quoteUseCase = query -> {
            captured[0] = query;
            return Optional.empty();
        };
        PricingQuoteQueryAdapter adapter = adapter(quoteUseCase, query -> List.of());

        adapter.quote("v1", "USD", 1, "sc1", "reg-1");

        assertThat(captured[0].regionId().getValue()).isEqualTo("reg-1");
    }

    private PricingQuoteQueryAdapter adapter(
            QuoteVariantPriceUseCase quoteUseCase,
            ListVariantIdsForPriceSetUseCase listUseCase
    ) {
        return new PricingQuoteQueryAdapter(quoteUseCase, listUseCase, new PricingQuoteQueryMapper(), new UuidGenerator());
    }
}
