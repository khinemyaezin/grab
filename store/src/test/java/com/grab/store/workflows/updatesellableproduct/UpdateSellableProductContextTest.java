package com.grab.store.workflows.updatesellableproduct;

import com.grab.store.workflows.internal.workflows.updatesellableproduct.UpdateSellableProductContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateSellableProductContextTest {

    @Test
    void withProductUpdated_whenPricingLineHasSkuOnly_shouldAssignVariantIdFromEvent() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                product(),
                List.of(),
                List.of(pricingLine("SKU-NEW", null))
        );

        UpdateSellableProductContext updated = context.withProductUpdated(
                "product-1",
                List.of(new UpdateSellableProductContext.VariantRef("variant-new", "SKU-NEW"))
        );

        assertThat(updated.pricingLines()).hasSize(1);
        assertThat(updated.pricingLines().getFirst().sku()).isEqualTo("SKU-NEW");
        assertThat(updated.pricingLines().getFirst().variantId()).isEqualTo("variant-new");
        assertThat(updated.variantRefs()).containsExactly(
                new UpdateSellableProductContext.VariantRef("variant-new", "SKU-NEW")
        );
        assertThat(updated.productUpdated()).isTrue();
        assertThat(context.productUpdated()).isFalse();
    }

    @Test
    void withProductUpdated_whenClientSentVariantId_shouldOverwriteFromCatalogEvent() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                product(),
                List.of(),
                List.of(pricingLine("SKU-1", "stale-variant"))
        );

        UpdateSellableProductContext updated = context.withProductUpdated(
                "product-1",
                List.of(new UpdateSellableProductContext.VariantRef("variant-1", "SKU-1"))
        );

        assertThat(updated.pricingLines().getFirst().variantId()).isEqualTo("variant-1");
    }

    @Test
    void withProductUpdated_whenNoMatchingSku_shouldLeavePricingLineUnchanged() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                product(),
                List.of(),
                List.of(pricingLine("SKU-1", null))
        );

        UpdateSellableProductContext updated = context.withProductUpdated(
                "product-1",
                List.of(new UpdateSellableProductContext.VariantRef("variant-2", "SKU-2"))
        );

        assertThat(updated.pricingLines().getFirst().variantId()).isNull();
        assertThat(updated.pricingLines().getFirst().sku()).isEqualTo("SKU-1");
    }

    @Test
    void withProductUpdated_whenPublicationLineHasSkuOnly_shouldAssignVariantIdFromEvent() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                product(),
                List.of(),
                List.of(),
                List.of(new UpdateSellableProductContext.PublicationLine("SKU-NEW", null, "web-1"))
        );

        UpdateSellableProductContext updated = context.withProductUpdated(
                "product-1",
                List.of(new UpdateSellableProductContext.VariantRef("variant-new", "SKU-NEW"))
        );

        assertThat(updated.publicationLines()).hasSize(1);
        assertThat(updated.publicationLines().getFirst().sku()).isEqualTo("SKU-NEW");
        assertThat(updated.publicationLines().getFirst().variantId()).isEqualTo("variant-new");
        assertThat(updated.publicationLines().getFirst().salesChannelId()).isEqualTo("web-1");
        assertThat(updated.shouldPublish()).isTrue();
        assertThat(context.shouldPublish()).isTrue();
    }

    @Test
    void withProductUpdated_whenUnpublishLineHasSkuOnly_shouldAssignVariantIdFromEvent() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                product(),
                List.of(),
                List.of(),
                List.of(),
                List.of(new UpdateSellableProductContext.PublicationLine("SKU-NEW", null, "web-1"))
        );

        UpdateSellableProductContext updated = context.withProductUpdated(
                "product-1",
                List.of(new UpdateSellableProductContext.VariantRef("variant-new", "SKU-NEW"))
        );

        assertThat(updated.unpublishLines()).hasSize(1);
        assertThat(updated.unpublishLines().getFirst().sku()).isEqualTo("SKU-NEW");
        assertThat(updated.unpublishLines().getFirst().variantId()).isEqualTo("variant-new");
        assertThat(updated.unpublishLines().getFirst().salesChannelId()).isEqualTo("web-1");
        assertThat(updated.shouldUnpublish()).isTrue();
        assertThat(context.shouldUnpublish()).isTrue();
        assertThat(updated.shouldPublish()).isFalse();
    }

    @Test
    void withProductUpdated_whenUnpublishSkuIsGone_shouldDropUnpublishLine() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                product(),
                List.of(),
                List.of(),
                List.of(),
                List.of(
                        new UpdateSellableProductContext.PublicationLine("SKU-STANDALONE", null, "web-1"),
                        new UpdateSellableProductContext.PublicationLine("SKU-M", null, "mkt-1")
                )
        );

        UpdateSellableProductContext updated = context.withProductUpdated(
                "product-1",
                List.of(
                        new UpdateSellableProductContext.VariantRef("variant-m", "SKU-M"),
                        new UpdateSellableProductContext.VariantRef("variant-l", "SKU-L")
                )
        );

        assertThat(updated.unpublishLines()).hasSize(1);
        assertThat(updated.unpublishLines().getFirst().sku()).isEqualTo("SKU-M");
        assertThat(updated.unpublishLines().getFirst().variantId()).isEqualTo("variant-m");
        assertThat(updated.unpublishLines().getFirst().salesChannelId()).isEqualTo("mkt-1");
        assertThat(updated.shouldUnpublish()).isTrue();
    }

    @Test
    void withProductUpdated_whenEveryUnpublishSkuIsGone_shouldSkipUnpublish() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                product(),
                List.of(),
                List.of(),
                List.of(),
                List.of(
                        new UpdateSellableProductContext.PublicationLine("SKU-STANDALONE", null, "web-1"),
                        new UpdateSellableProductContext.PublicationLine("SKU-STANDALONE", null, "mkt-1")
                )
        );

        UpdateSellableProductContext updated = context.withProductUpdated(
                "product-1",
                List.of(
                        new UpdateSellableProductContext.VariantRef("variant-m", "SKU-M"),
                        new UpdateSellableProductContext.VariantRef("variant-l", "SKU-L")
                )
        );

        assertThat(updated.unpublishLines()).isEmpty();
        assertThat(updated.shouldUnpublish()).isFalse();
    }

    @Test
    void allCreatedPriceSetsCompensated_shouldTrackOnlyCreatedSets() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                product(),
                List.of(),
                List.of(pricingLine("SKU-1", "variant-1"))
        ).withPricePair(new UpdateSellableProductContext.PricePair("variant-1", "SKU-1", "price-set-new"), true);

        assertThat(context.allCreatedPriceSetsCompensated()).isFalse();
        assertThat(context.withPriceSetCompensated("price-set-new").allCreatedPriceSetsCompensated()).isTrue();
        assertThat(context.withPricePair(
                new UpdateSellableProductContext.PricePair("variant-1", "SKU-1", "price-set-new"),
                true
        ).createdPriceSetIds()).hasSize(1);
    }

    private static UpdateSellableProductContext.Product product() {
        return new UpdateSellableProductContext.Product("Shirt", "cat-1", "NEW", "shirt", null);
    }

    private static UpdateSellableProductContext.PricingLine pricingLine(String sku, String variantId) {
        return new UpdateSellableProductContext.PricingLine(
                sku,
                variantId,
                "Base",
                "USD",
                new BigDecimal("19.99"),
                null,
                null,
                List.of()
        );
    }
}
