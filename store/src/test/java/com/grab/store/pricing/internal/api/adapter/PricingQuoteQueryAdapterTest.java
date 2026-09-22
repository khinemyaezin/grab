package com.grab.store.pricing.internal.api.adapter;

import com.grab.store.pricing.internal.api.adapter.mapper.PricingQuoteQueryMapper;
import com.grab.store.pricing.port.PricingQuoteQuery.QuotedPrice;
import com.pricing.application.model.read.ListVariantIdsForPriceSetQuery;
import com.pricing.application.model.read.QuoteVariantPriceQuery;
import com.pricing.application.model.read.QuoteVariantPriceResult;
import com.pricing.application.port.inbound.ListVariantIdsForPriceSetUseCase;
import com.pricing.application.port.inbound.QuoteVariantPriceUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingQuoteQueryAdapterTest {

    @Mock
    private QuoteVariantPriceUseCase quoteVariantPriceUseCase;
    @Mock
    private ListVariantIdsForPriceSetUseCase listVariantIdsForPriceSetUseCase;

    private PricingQuoteQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PricingQuoteQueryAdapter(
                quoteVariantPriceUseCase,
                listVariantIdsForPriceSetUseCase,
                new PricingQuoteQueryMapper()
        );
    }

    @Test
    void quote_validQuery_returnsQuotedPrice() {
        QuoteVariantPriceResult result = new QuoteVariantPriceResult(new BigDecimal("19.99"), "USD", "ps-1");
        when(quoteVariantPriceUseCase.execute(any(QuoteVariantPriceQuery.class)))
                .thenReturn(Optional.of(result));

        Optional<QuotedPrice> quoted = adapter.quote("v1", "USD", 1, "sc1");

        assertThat(quoted).isPresent();
        assertThat(quoted.get().amount()).isEqualByComparingTo("19.99");
        assertThat(quoted.get().currencyCode()).isEqualTo("USD");
        assertThat(quoted.get().priceSetId()).isEqualTo("ps-1");
    }

    @Test
    void variantIdsForPriceSet_validPriceSetId_returnsVariantIdList() {
        when(listVariantIdsForPriceSetUseCase.execute(any(ListVariantIdsForPriceSetQuery.class)))
                .thenReturn(List.of("v1", "v2"));

        List<String> variantIds = adapter.variantIdsForPriceSet("ps-1");

        assertThat(variantIds).containsExactly("v1", "v2");
    }
}
