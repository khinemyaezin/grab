package com.grab.store.storefrontquery.internal.adapter;

import com.grab.discovery.application.port.outbound.PricingPort;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.store.pricing.port.PricingQuoteQuery;
import com.grab.store.storefrontquery.internal.adapter.mapper.PricingQuoteMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PricingQuoteAdapterTest {
    @Test
    void quoteBatchMapsEveryProviderOutcomeByVariantId() {
        PricingQuoteQuery pricing = new PricingQuoteQuery() {
            @Override
            public Optional<QuotedPrice> quote(
                    String variantId,
                    String currencyCode,
                    int quantity,
                    String salesChannelId,
                    String regionId
            ) {
                return Optional.empty();
            }

            @Override
            public BatchQuoteResult quoteBatch(BatchQuoteRequest request) {
                return new BatchQuoteResult(Map.of(
                        "v1", new QuotedPrice(new BigDecimal("4.00"), null, "usd", "ps-1"),
                        "v2", new QuotedPrice(null, null, null, "ps-2")
                ));
            }

            @Override
            public List<String> variantIdsForPriceSet(String priceSetId) {
                return List.of();
            }
        };
        PricingQuoteMapper mapper = new PricingQuoteMapper() {
            @Override
            public PricingPort.QuotedPrice toQuotedPrice(PricingQuoteQuery.QuotedPrice quote) {
                if (quote == null) {
                    return null;
                }
                Id priceSetId = quote.priceSetId() == null ? null : new CommonId(quote.priceSetId());
                return new PricingPort.QuotedPrice(
                        quote.amount(),
                        quote.originalAmount(),
                        quote.currencyCode(),
                        priceSetId
                );
            }
        };
        PricingQuoteAdapter adapter = new PricingQuoteAdapter(pricing, new UuidGenerator(), mapper);

        Map<Id, PricingPort.QuotedPrice> result = adapter.quoteBatch(
                List.of(new CommonId("v1"), new CommonId("v2")),
                "USD",
                1,
                new CommonId("channel-1"),
                new CommonId("region-1")
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(new CommonId("v1")).amount()).isEqualByComparingTo("4.00");
        assertThat(result.get(new CommonId("v2")).amount()).isNull();
        assertThat(result.get(new CommonId("v2")).priceSetId()).isEqualTo(new CommonId("ps-2"));
    }
}
