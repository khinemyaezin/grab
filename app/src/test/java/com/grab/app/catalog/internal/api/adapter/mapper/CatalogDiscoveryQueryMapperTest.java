package com.grab.app.catalog.catalog.internal.adapter.outbound.mapper;

import com.catalog.application.model.read.GetProductResult;
import com.grab.discovery.application.port.outbound.CatalogPort;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.mapper.IdMapper;
import com.grab.app.catalog.catalog.port.CatalogDiscoveryQuery;
import com.grab.app.channel.discovery.internal.adapter.mapper.CatalogDiscoveryMapper;
import com.grab.app.channel.discovery.internal.adapter.mapper.CatalogDiscoveryMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogDiscoveryQueryMapperTest {
    private CatalogDiscoveryQueryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CatalogDiscoveryQueryMapperImpl();
        UuidGenerator idGenerator = new UuidGenerator();
        IdMapper idMapper = new IdMapper(idGenerator);
        ReflectionTestUtils.setField(mapper, "idMapper", idMapper);
    }

    @Test
    void toFacts_completeSource_preservesPresentationAndVariantFacts() {
        GetProductResult.Variation variation = new GetProductResult.Variation("option-1", "Blue", "type-1", "Color");
        GetProductResult.Publication publication = new GetProductResult.Publication("channel-1");
        GetProductResult.Variant variant = new GetProductResult.Variant(
                "variant-1", "SKU1", "ACTIVE", "Color: Blue", List.of(variation), true,
                List.of(), null, List.of(publication)
        );
        GetProductResult.VariantOption option = new GetProductResult.VariantOption("option-1", "Blue");
        GetProductResult.VariantType type = new GetProductResult.VariantType("type-1", "Color", List.of(option));
        GetProductResult.Description later = new GetProductResult.Description("description-2", null, null, "Later");
        GetProductResult.Description first = new GetProductResult.Description("description-1", null, null, "First description");
        GetProductResult.Description blank = new GetProductResult.Description("description-0", null, null, "  ");
        GetProductResult.Media rankTwo = new GetProductResult.Media("media-0", null, "https://example.com/rank-two.jpg", null, 2);
        GetProductResult.Media rankOneLater = new GetProductResult.Media("media-2", null, "https://example.com/later.jpg", null, 1);
        GetProductResult.Media rankOneFirst = new GetProductResult.Media("media-1", null, "https://example.com/first.jpg", null, 1);
        GetProductResult.Media unusable = new GetProductResult.Media("media-0", null, " ", null, 0);
        GetProductResult.Category category = new GetProductResult.Category("category-1", "Category");
        GetProductResult source = new GetProductResult(
                "product-1", "Product", category, "NEW", "ACTIVE", "product-slug",
                List.of(later, blank, first), List.of(rankTwo, rankOneLater, unusable, rankOneFirst),
                List.of(variant), List.of(type)
        );

        CatalogDiscoveryQuery.ProductFacts provider = mapper.toFacts("product-1", "merchant-1", source);
        CatalogDiscoveryMapper consumerMapper = new CatalogDiscoveryMapperImpl();
        CatalogPort.ProductFacts consumer = consumerMapper.toProductFacts(provider);

        CommonId productId = new CommonId("product-1");
        CommonId merchantId = new CommonId("merchant-1");
        CommonId categoryId = new CommonId("category-1");
        CommonId variantId = new CommonId("variant-1");
        CommonId channelId = new CommonId("channel-1");
        assertThat(consumer.productId()).isEqualTo(productId);
        assertThat(consumer.merchantId()).isEqualTo(merchantId);
        assertThat(consumer.categoryId()).isEqualTo(categoryId);
        assertThat(consumer.title()).isEqualTo("Product");
        assertThat(consumer.slug()).isEqualTo("product-slug");
        assertThat(consumer.status()).isEqualTo("ACTIVE");
        assertThat(consumer.description()).isEqualTo("First description");
        assertThat(consumer.thumbnail()).isEqualTo("https://example.com/first.jpg");
        assertThat(consumer.options().getFirst().values()).containsExactly("Blue");
        CatalogPort.VariantFacts mappedVariant = consumer.variants().getFirst();
        assertThat(mappedVariant.variantId()).isEqualTo(variantId);
        assertThat(mappedVariant.productId()).isEqualTo(productId);
        assertThat(mappedVariant.sku()).isEqualTo("SKU1");
        assertThat(mappedVariant.title()).isEqualTo("Color: Blue");
        assertThat(mappedVariant.status()).isEqualTo("ACTIVE");
        assertThat(mappedVariant.manageInventory()).isTrue();
        assertThat(mappedVariant.optionValues()).containsEntry("Color", "Blue");
        assertThat(mappedVariant.salesChannelIds()).containsExactly(channelId);
    }

    @Test
    void toFacts_missingPresentation_keepsPresentationAbsent() {
        GetProductResult source = new GetProductResult(
                "product-1", "Product", null, "NEW", "ACTIVE", "product-slug",
                List.of(), List.of(), List.of(), List.of()
        );

        CatalogDiscoveryQuery.ProductFacts result = mapper.toFacts("product-1", "merchant-1", source);

        assertThat(result.description()).isNull();
        assertThat(result.thumbnail()).isNull();
        assertThat(result.categoryId()).isNull();
        assertThat(result.options()).isEmpty();
        assertThat(result.variants()).isEmpty();
    }
}
