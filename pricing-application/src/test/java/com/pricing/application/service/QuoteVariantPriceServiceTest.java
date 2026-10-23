package com.pricing.application.service;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import com.pricing.application.model.read.CalculatePricesQuery;
import com.pricing.application.model.read.CalculatedPriceSetResult;
import com.pricing.application.model.read.QuoteVariantPriceQuery;
import com.pricing.application.model.read.VariantPriceSetLinkView;
import com.pricing.application.port.inbound.CalculatePricesUseCase;
import com.pricing.application.port.outbound.VariantPriceSetLinkQueryPort;
import com.pricing.domain.valueobject.PricingAttributeKeys;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteVariantPriceServiceTest {
    private final IdGenerator ids = new UuidGenerator();

    @Test
    void execute_withRegion_shouldPassChannelAndRegionAttributesToPriceCalculation() {
        CalculatePricesQuery[] captured = new CalculatePricesQuery[1];
        QuoteVariantPriceService service = service(captured);

        Optional<?> result = service.execute(new QuoteVariantPriceQuery(
                "variant-1", "USD", 1, "channel-1", new CommonId("region-1")
        ));

        assertThat(captured[0].attributes())
                .containsEntry(PricingAttributeKeys.SALES_CHANNEL_ID, "channel-1")
                .containsEntry(PricingAttributeKeys.REGION_ID, "region-1");
        assertThat(result).isPresent();
    }

    @Test
    void execute_withoutRegion_shouldKeepLegacyChannelOnlyContext() {
        CalculatePricesQuery[] captured = new CalculatePricesQuery[1];
        QuoteVariantPriceService service = service(captured);

        service.execute(new QuoteVariantPriceQuery("variant-1", "USD", 1, "channel-1", null));

        assertThat(captured[0].attributes())
                .containsEntry(PricingAttributeKeys.SALES_CHANNEL_ID, "channel-1")
                .doesNotContainKey(PricingAttributeKeys.REGION_ID);
    }

    private QuoteVariantPriceService service(CalculatePricesQuery[] captured) {
        VariantPriceSetLinkQueryPort links = new VariantPriceSetLinkQueryPort() {
            @Override
            public List<VariantPriceSetLinkView> findByVariantIds(java.util.Collection<String> variantIds) {
                return List.of(link());
            }

            @Override
            public List<VariantPriceSetLinkView> findByPriceSetId(String priceSetId) {
                return List.of();
            }
        };
        CalculatePricesUseCase calculatePrices = query -> {
            captured[0] = query;
            return List.of(calculatedPrice());
        };
        return new QuoteVariantPriceService(links, calculatePrices, ids);
    }

    private VariantPriceSetLinkView link() {
        return new VariantPriceSetLinkView("variant-1", "price-set-1", "product-1", "sku-1", "merchant-1", null, null);
    }

    private CalculatedPriceSetResult calculatedPrice() {
        return new CalculatedPriceSetResult(
                "price-set-1",
                "USD",
                new BigDecimal("12.50"),
                false,
                false,
                new CalculatedPriceSetResult.SelectedPriceResult("price-1", null, null, null, null),
                null,
                false,
                false,
                new CalculatedPriceSetResult.SelectedPriceResult(null, null, null, null, null)
        );
    }
}
