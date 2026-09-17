package com.grab.store.storefront.internal.api.rest.service;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.queries.GetProductBySlugResult;
import com.grab.store.catalog.queries.GetStorefrontProductBySlugQuery;
import com.grab.store.catalog.queries.SearchStorefrontProductsQuery;
import com.grab.store.catalog.queries.StorefrontProductSearchResult;
import com.grab.store.inventory.queries.ListSkuAvailabilityQuery;
import com.grab.store.inventory.queries.SkuAvailabilityResult;
import com.grab.store.pricing.queries.CalculatePricesQuery;
import com.grab.store.pricing.queries.CalculatedPriceSetResult;
import com.grab.store.pricing.queries.ListVariantPriceSetLinksQuery;
import com.grab.store.pricing.queries.VariantPriceSetLinkResult;
import com.grab.store.storefront.internal.api.rest.dto.request.StorefrontProductSearchRequest;
import com.grab.store.storefront.internal.api.rest.dto.response.StorefrontProductCard;
import com.grab.store.storefront.internal.api.rest.dto.response.StorefrontProductDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StorefrontBrowseServiceTest {

    private RecordingQueryBus queryBus;
    private StorefrontBrowseService service;

    @BeforeEach
    void setUp() {
        queryBus = new RecordingQueryBus();
        service = new StorefrontBrowseService(queryBus, new ConvertingIdGenerator());
    }

    @Test
    void searchAlwaysEnrichesPriceRangeAndStockIncludingUntrackedSkus() {
        queryBus.searchPage = new PageImpl<>(List.of(new StorefrontProductSearchResult(
                "prod-1",
                "Shirt",
                "shirt",
                "Apparel",
                "cat-1",
                "NEW",
                true,
                "merchant-1",
                new StorefrontProductSearchResult.Media("m-1", "hero.jpg", "http://cdn/hero.jpg", "image/jpeg", 0),
                List.of(
                        new StorefrontProductSearchResult.VariantRef("var-1", "SKU-1"),
                        new StorefrontProductSearchResult.VariantRef("var-2", "SKU-DIGITAL")
                )
        )), PageRequest.of(0, 20), 1);
        queryBus.links = List.of(
                new VariantPriceSetLinkResult("var-1", "ps-1", "prod-1", "SKU-1", "merchant-1")
        );
        queryBus.prices = List.of(new CalculatedPriceSetResult(
                "ps-1", "mmk", new BigDecimal("45900"), false, false, null,
                null, false, false, null
        ));
        queryBus.stock = List.of(
                new SkuAvailabilityResult("SKU-1", 0, false),
                new SkuAvailabilityResult("SKU-DIGITAL", 1, true)
        );

        Page<StorefrontProductCard> page = service.search(
                new StorefrontProductSearchRequest("shirt", "cat-1", "NEW", true),
                PageRequest.of(0, 20)
        );

        assertThat(queryBus.dispatched).hasSize(4);
        assertThat(queryBus.dispatched.get(0)).isInstanceOf(SearchStorefrontProductsQuery.class);
        SearchStorefrontProductsQuery searchQuery = (SearchStorefrontProductsQuery) queryBus.dispatched.get(0);
        assertThat(searchQuery.query()).isEqualTo("shirt");
        assertThat(searchQuery.categoryId()).isEqualTo("cat-1");
        assertThat(searchQuery.condition()).isEqualTo("NEW");
        assertThat(searchQuery.featured()).isTrue();

        StorefrontProductCard card = page.getContent().getFirst();
        assertThat(card.thumbnailUrl()).isEqualTo("http://cdn/hero.jpg");
        assertThat(card.priceRange().minAmount()).isEqualByComparingTo("45900");
        assertThat(card.priceRange().maxAmount()).isEqualByComparingTo("45900");
        assertThat(card.priceRange().currencyCode()).isEqualTo("mmk");
        assertThat(card.inStock()).isTrue();
        assertThat(card.sellerId()).isEqualTo("merchant-1");
    }

    @Test
    void bySlugEnrichesVariantPriceAndTreatsUntrackedAsInStock() {
        queryBus.slugResult = new GetProductBySlugResult(
                "prod-1",
                "Shirt",
                "cat-1",
                "merchant-1",
                "FIRST_PARTY_RETAILER",
                "NEW",
                "ACTIVE",
                "shirt",
                true,
                List.of(),
                List.of(),
                List.of(
                        new GetProductBySlugResult.Variant(
                                "var-1", "SKU-1", "ACTIVE", List.of(), true, List.of(), null),
                        new GetProductBySlugResult.Variant(
                                "var-2", "SKU-DIGITAL", "ACTIVE", List.of(), false, List.of(), null)
                ),
                List.of()
        );
        queryBus.links = List.of(
                new VariantPriceSetLinkResult("var-1", "ps-1", "prod-1", "SKU-1", "merchant-1")
        );
        queryBus.prices = List.of(new CalculatedPriceSetResult(
                "ps-1", "mmk", new BigDecimal("45900"), false, false, null,
                new BigDecimal("55900"), false, false, null
        ));
        queryBus.stock = List.of(
                new SkuAvailabilityResult("SKU-1", 42, true),
                new SkuAvailabilityResult("SKU-DIGITAL", 1, true)
        );

        StorefrontProductDetail detail = service.bySlug("shirt");

        assertThat(queryBus.dispatched.getFirst()).isInstanceOf(GetStorefrontProductBySlugQuery.class);
        assertThat(detail.sellerId()).isEqualTo("merchant-1");
        assertThat(detail.sellerType()).isEqualTo("FIRST_PARTY_RETAILER");
        assertThat(detail.offerEligible()).isFalse();
        assertThat(detail.variants().getFirst().price().amount()).isEqualByComparingTo("45900");
        assertThat(detail.variants().getFirst().price().originalAmount()).isEqualByComparingTo("55900");
        assertThat(detail.variants().getFirst().stock().availableQuantity()).isEqualTo(42);
        assertThat(detail.variants().get(1).stock().inStock()).isTrue();
    }

    private static final class RecordingQueryBus implements QueryBus {
        private final List<Query<?>> dispatched = new java.util.ArrayList<>();
        private Page<StorefrontProductSearchResult> searchPage = Page.empty();
        private GetProductBySlugResult slugResult;
        private List<VariantPriceSetLinkResult> links = List.of();
        private List<CalculatedPriceSetResult> prices = List.of();
        private List<SkuAvailabilityResult> stock = List.of();

        @Override
        @SuppressWarnings("unchecked")
        public <R> R dispatch(Query<R> query) {
            dispatched.add(query);
            if (query instanceof SearchStorefrontProductsQuery) {
                return (R) searchPage;
            }
            if (query instanceof GetStorefrontProductBySlugQuery) {
                return (R) slugResult;
            }
            if (query instanceof ListVariantPriceSetLinksQuery) {
                return (R) links;
            }
            if (query instanceof CalculatePricesQuery) {
                return (R) prices;
            }
            if (query instanceof ListSkuAvailabilityQuery) {
                return (R) stock;
            }
            throw new IllegalArgumentException(query.getClass().getName());
        }
    }

    private static final class ConvertingIdGenerator implements IdGenerator {
        @Override
        public Id generateId() {
            return new CommonId("generated");
        }

        @Override
        public Id convertIdFrom(String id) {
            return new CommonId(id);
        }
    }
}
