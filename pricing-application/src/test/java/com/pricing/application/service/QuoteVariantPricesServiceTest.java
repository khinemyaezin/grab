package com.pricing.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.application.model.read.CalculatePricesQuery;
import com.pricing.application.model.read.CalculatedPriceSetResult;
import com.pricing.application.model.read.QuoteVariantPricesQuery;
import com.pricing.application.model.read.QuoteVariantPricesResult;
import com.pricing.application.model.read.VariantPriceSetLinkView;
import com.pricing.application.port.inbound.CalculatePricesUseCase;
import com.pricing.application.port.outbound.VariantPriceSetLinkQueryPort;
import com.pricing.application.policy.QuoteVariantPricesPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuoteVariantPricesServiceTest {
    private final IdGenerator ids = new UuidGenerator();

    @Test
    void emptyInputDoesNotReadOrCalculate() {
        AtomicInteger linkCalls = new AtomicInteger();
        AtomicInteger calculationCalls = new AtomicInteger();
        QuoteVariantPricesService service = service(List.of(), linkCalls, calculationCalls, new ArrayList<>());

        QuoteVariantPricesResult result = service.execute(query(List.of()));

        assertThat(result.quotes()).isEmpty();
        assertThat(linkCalls).hasValue(0);
        assertThat(calculationCalls).hasValue(0);
    }

    @Test
    void deduplicatesVariantsAndPriceSetsAndMapsShuffledResults() {
        AtomicInteger linkCalls = new AtomicInteger();
        AtomicInteger calculationCalls = new AtomicInteger();
        List<Collection<String>> requested = new ArrayList<>();
        List<VariantPriceSetLinkView> links = List.of(
                link("v1", "ps-1"),
                link("v2", "ps-1"),
                link("v3", "ps-2")
        );
        QuoteVariantPricesService service = service(links, linkCalls, calculationCalls, requested);

        QuoteVariantPricesResult result = service.execute(query(List.of(id("v1"), id("v2"), id("v1"), id("v3"))));

        assertThat(requested).containsExactly(List.of("v1", "v2", "v3"));
        assertThat(result.quotes()).containsOnlyKeys(id("v1"), id("v2"), id("v3"));
        assertThat(result.quotes().get(id("v1")).amount()).isEqualByComparingTo("12.00");
        assertThat(result.quotes().get(id("v2")).amount()).isEqualByComparingTo("12.00");
        assertThat(result.quotes().get(id("v3")).amount()).isNull();
        assertThat(result.quotes().get(id("v3")).priceSetId()).isEqualTo(id("ps-2"));
        assertThat(linkCalls).hasValue(1);
        assertThat(calculationCalls).hasValue(1);
    }

    @Test
    void allUnlinkedVariantsReturnOutcomesWithoutCalculation() {
        AtomicInteger linkCalls = new AtomicInteger();
        AtomicInteger calculationCalls = new AtomicInteger();
        QuoteVariantPricesService service = service(List.of(), linkCalls, calculationCalls, new ArrayList<>());

        QuoteVariantPricesResult result = service.execute(query(List.of(id("v1"), id("v2"))));

        assertThat(result.quotes()).hasSize(2);
        assertThat(result.quotes().values()).allSatisfy(quote -> {
            assertThat(quote.amount()).isNull();
            assertThat(quote.originalAmount()).isNull();
            assertThat(quote.priceSetId()).isNull();
        });
        assertThat(linkCalls).hasValue(1);
        assertThat(calculationCalls).hasValue(0);
    }

    @Test
    void configuredZeroIsPreserved() {
        AtomicInteger linkCalls = new AtomicInteger();
        AtomicInteger calculationCalls = new AtomicInteger();
        QuoteVariantPricesService service = service(
                List.of(link("v1", "ps-1")),
                linkCalls,
                calculationCalls,
                new ArrayList<>()
        );

        QuoteVariantPricesResult result = service.execute(query(List.of(id("v1"))));

        assertThat(result.quotes().get(id("v1")).amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void invalidContextIsRejectedBeforePersistence() {
        AtomicInteger linkCalls = new AtomicInteger();
        QuoteVariantPricesService service = service(List.of(), linkCalls, new AtomicInteger(), new ArrayList<>());

        assertThatThrownBy(() -> service.execute(new QuoteVariantPricesQuery(
                List.of(id("v1")),
                "USD",
                1,
                id("channel-1"),
                null
        ))).isInstanceOf(PricingServiceException.class)
                .hasMessageContaining("regionId");
        assertThat(linkCalls).hasValue(0);
    }

    private QuoteVariantPricesService service(
            List<VariantPriceSetLinkView> links,
            AtomicInteger linkCalls,
            AtomicInteger calculationCalls,
            List<Collection<String>> requested
    ) {
        VariantPriceSetLinkQueryPort linkPort = new VariantPriceSetLinkQueryPort() {
            @Override
            public List<VariantPriceSetLinkView> findByVariantIds(Collection<String> variantIds) {
                linkCalls.incrementAndGet();
                requested.add(List.copyOf(variantIds));
                return links;
            }

            @Override
            public List<VariantPriceSetLinkView> findByPriceSetId(String priceSetId) {
                return List.of();
            }
        };
        CalculatePricesUseCase calculate = query -> {
            calculationCalls.incrementAndGet();
            assertThat(query.attributes()).containsEntry("sales_channel_id", "channel-1")
                    .containsEntry("region_id", "region-1");
            return List.of(
                    calculated("ps-2", null, null),
                    calculated("ps-1", links.size() == 1 ? BigDecimal.ZERO : new BigDecimal("12.00"), BigDecimal.ZERO)
            );
        };
        return new QuoteVariantPricesService(linkPort, calculate, ids, new QuoteVariantPricesPolicy());
    }

    private QuoteVariantPricesQuery query(List<Id> variantIds) {
        return new QuoteVariantPricesQuery(variantIds, "USD", 1, id("channel-1"), id("region-1"));
    }

    private VariantPriceSetLinkView link(String variantId, String priceSetId) {
        return new VariantPriceSetLinkView(variantId, priceSetId, "product-1", "sku-1", "merchant-1", null, null);
    }

    private CalculatedPriceSetResult calculated(String priceSetId, BigDecimal amount, BigDecimal original) {
        return new CalculatedPriceSetResult(
                priceSetId,
                amount == null ? null : "usd",
                amount,
                false,
                false,
                null,
                original,
                false,
                false,
                null
        );
    }

    private Id id(String value) {
        return new CommonId(value);
    }
}
