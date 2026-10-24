package com.grab.store.storefrontquery.internal.adapter;

import com.grab.discovery.application.port.outbound.PricingPort;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.pricing.port.PricingQuoteQuery;
import com.grab.store.storefrontquery.internal.adapter.mapper.PricingQuoteMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingQuoteAdapterTest {
    private static final Id FIRST_VARIANT = new CommonId("v1");
    private static final Id SECOND_VARIANT = new CommonId("v2");
    private static final Id CHANNEL = new CommonId("channel-1");
    private static final Id REGION = new CommonId("region-1");
    @Mock
    private PricingQuoteQuery pricing;
    @Mock
    private PricingQuoteMapper mapper;
    @Captor
    private ArgumentCaptor<PricingQuoteQuery.BatchQuoteRequest> requestCaptor;
    @InjectMocks
    private PricingQuoteAdapter adapter;

    @Test
    void quoteBatch_completeProviderResult_preservesAllOutcomesAndContext() {
        BigDecimal amount = new BigDecimal("4.00");
        PricingQuoteQuery.QuotedPrice first = new PricingQuoteQuery.QuotedPrice(amount, null, "usd", "ps-1");
        PricingQuoteQuery.QuotedPrice second = new PricingQuoteQuery.QuotedPrice(null, null, "usd", "ps-2");
        PricingQuoteQuery.BatchQuoteResult batch = new PricingQuoteQuery.BatchQuoteResult(Map.of("v1", first, "v2", second));
        Id firstPriceSet = new CommonId("ps-1");
        Id secondPriceSet = new CommonId("ps-2");
        PricingPort.QuotedPrice priced = new PricingPort.QuotedPrice(amount, null, "usd", firstPriceSet);
        PricingPort.QuotedPrice unpriced = new PricingPort.QuotedPrice(null, null, "usd", secondPriceSet);
        when(pricing.quoteBatch(any())).thenReturn(batch);
        when(mapper.toQuotedPrice(first)).thenReturn(priced);
        when(mapper.toQuotedPrice(second)).thenReturn(unpriced);
        List<Id> variants = List.of(FIRST_VARIANT, SECOND_VARIANT);

        Map<Id, PricingPort.QuotedPrice> result = adapter.quoteBatch(variants, "USD", 1, CHANNEL, REGION);

        assertThat(result).containsEntry(FIRST_VARIANT, priced).containsEntry(SECOND_VARIANT, unpriced);
        assertThat(result.get(SECOND_VARIANT).amount()).isNull();
        verify(pricing).quoteBatch(requestCaptor.capture());
        PricingQuoteQuery.BatchQuoteRequest request = requestCaptor.getValue();
        assertThat(request.variantIds()).containsExactly("v1", "v2");
        assertThat(request.salesChannelId()).isEqualTo("channel-1");
        assertThat(request.regionId()).isEqualTo("region-1");
        assertThat(request.currencyCode()).isEqualTo("USD");
        assertThat(request.quantity()).isEqualTo(1);
    }

    @Test
    void quoteBatch_incompleteProviderResult_throwsWithoutFabricatingOutcome() {
        PricingQuoteQuery.BatchQuoteResult batch = new PricingQuoteQuery.BatchQuoteResult(Map.of());
        when(pricing.quoteBatch(any())).thenReturn(batch);
        List<Id> variants = List.of(FIRST_VARIANT);

        assertThatThrownBy(() -> adapter.quoteBatch(variants, "USD", 1, CHANNEL, REGION))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(mapper);
    }

    @Test
    void quote_unpricedVariant_preservesEmptyOutcomeAndRegion() {
        when(pricing.quote("v1", "USD", 2, "channel-1", "region-1")).thenReturn(Optional.empty());

        Optional<PricingPort.QuotedPrice> result = adapter.quote(FIRST_VARIANT, "USD", 2, CHANNEL, REGION);

        assertThat(result).isEmpty();
        verifyNoInteractions(mapper);
    }
}
