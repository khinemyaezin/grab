package com.grab.store.storefront.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.catalog.queries.GetProductBySlugResult;
import com.grab.store.catalog.queries.GetStorefrontCategoryChildrenQuery;
import com.grab.store.catalog.queries.GetStorefrontCategoryTreeQuery;
import com.grab.store.catalog.queries.GetStorefrontProductBySlugQuery;
import com.grab.store.catalog.queries.ListFeaturedStorefrontProductsQuery;
import com.grab.store.catalog.queries.ListNewArrivalStorefrontProductsQuery;
import com.grab.store.catalog.queries.SearchStorefrontProductsQuery;
import com.grab.store.catalog.queries.StorefrontCategoryChild;
import com.grab.store.catalog.queries.StorefrontCategoryChildren;
import com.grab.store.catalog.queries.StorefrontCategoryNode;
import com.grab.store.catalog.queries.StorefrontProductSearchResult;
import com.grab.store.inventory.queries.ListSkuAvailabilityQuery;
import com.grab.store.inventory.queries.SkuAvailabilityResult;
import com.grab.store.pricing.queries.CalculatePricesQuery;
import com.grab.store.pricing.queries.CalculatedPriceSetResult;
import com.grab.store.pricing.queries.ListVariantPriceSetLinksQuery;
import com.grab.store.pricing.queries.VariantPriceSetLinkResult;
import com.grab.store.storefront.internal.api.rest.dto.request.StorefrontProductSearchRequest;
import com.grab.store.storefront.internal.api.rest.dto.response.StorefrontCategoryChildrenResponse;
import com.grab.store.storefront.internal.api.rest.dto.response.StorefrontCategoryNodeResponse;
import com.grab.store.storefront.internal.api.rest.dto.response.StorefrontProductCard;
import com.grab.store.storefront.internal.api.rest.dto.response.StorefrontProductDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StorefrontBrowseService {
    static final String DEFAULT_CURRENCY = "mmk";

    private final QueryBus queryBus;
    private final IdGenerator idGenerator;

    public List<StorefrontCategoryNodeResponse> categoryTree(String rootId) {
        List<StorefrontCategoryNode> trees = queryBus.dispatch(new GetStorefrontCategoryTreeQuery(rootId));
        return trees.stream().map(this::toCategoryNode).toList();
    }

    public StorefrontCategoryChildrenResponse categoryChildren(String categoryId) {
        StorefrontCategoryChildren children = queryBus.dispatch(new GetStorefrontCategoryChildrenQuery(categoryId));
        return new StorefrontCategoryChildrenResponse(
                children.parentId(),
                children.children().stream().map(this::toCategoryChild).toList()
        );
    }

    public Page<StorefrontProductCard> search(StorefrontProductSearchRequest request, Pageable pageable) {
        StorefrontProductSearchRequest body = request == null
                ? new StorefrontProductSearchRequest(null, null, null, null)
                : request;
        Page<StorefrontProductSearchResult> page = queryBus.dispatch(new SearchStorefrontProductsQuery(
                body.query(),
                body.categoryId(),
                body.condition(),
                body.featured(),
                pageable
        ));
        return toCards(page);
    }

    public Page<StorefrontProductCard> featured(Pageable pageable) {
        return toCards(queryBus.dispatch(new ListFeaturedStorefrontProductsQuery(pageable)));
    }

    public Page<StorefrontProductCard> newArrivals(Pageable pageable) {
        return toCards(queryBus.dispatch(new ListNewArrivalStorefrontProductsQuery(pageable)));
    }

    public StorefrontProductDetail bySlug(String slug) {
        GetProductBySlugResult product = queryBus.dispatch(new GetStorefrontProductBySlugQuery(slug));
        PriceStockIndex index = loadIndex(product.variants().stream()
                .map(variant -> new VariantRef(variant.id(), variant.sku()))
                .toList());
        return new StorefrontProductDetail(
                product.id(),
                product.name(),
                product.categoryId(),
                product.merchantId(),
                product.merchantType(),
                product.condition(),
                false,
                product.status(),
                product.slug(),
                product.featured(),
                product.descriptions().stream()
                        .map(description -> new StorefrontProductDetail.Description(
                                description.id(),
                                description.name(),
                                description.title(),
                                description.description()
                        ))
                        .toList(),
                product.medias().stream()
                        .map(media -> new StorefrontProductDetail.Media(
                                media.id(),
                                media.storageKey(),
                                media.url(),
                                media.contentType(),
                                media.rank()
                        ))
                        .toList(),
                product.variants().stream()
                        .map(variant -> toDetailVariant(variant, index))
                        .toList(),
                product.variantTypes().stream()
                        .map(type -> new StorefrontProductDetail.VariantType(
                                type.typeId(),
                                type.typeName(),
                                type.options().stream()
                                        .map(option -> new StorefrontProductDetail.VariantOption(
                                                option.optionId(), option.optionName()))
                                        .toList()
                        ))
                        .toList()
        );
    }

    private Page<StorefrontProductCard> toCards(Page<StorefrontProductSearchResult> page) {
        PriceStockIndex index = loadIndex(page.getContent().stream()
                .flatMap(product -> product.variants().stream()
                        .map(variant -> new VariantRef(variant.variantId(), variant.sku())))
                .toList());
        return page.map(product -> toCard(product, index));
    }

    private StorefrontProductCard toCard(StorefrontProductSearchResult product, PriceStockIndex index) {
        List<BigDecimal> amounts = new ArrayList<>();
        boolean inStock = false;
        for (StorefrontProductSearchResult.VariantRef variant : product.variants()) {
            CalculatedPriceSetResult price = index.priceForVariant(variant.variantId());
            if (price != null && price.calculatedAmount() != null) {
                amounts.add(price.calculatedAmount());
            }
            SkuAvailabilityResult stock = index.stockBySku.get(variant.sku());
            if (stock != null && stock.inStock()) {
                inStock = true;
            }
        }
        BigDecimal min = amounts.stream().min(BigDecimal::compareTo).orElse(null);
        BigDecimal max = amounts.stream().max(BigDecimal::compareTo).orElse(null);
        String thumbnailUrl = product.thumbnail() == null ? null : product.thumbnail().url();
        return new StorefrontProductCard(
                product.productId(),
                product.productName(),
                product.slug(),
                product.categoryName(),
                product.categoryId(),
                product.condition(),
                product.featured(),
                thumbnailUrl,
                new StorefrontProductCard.PriceRange(min, max, DEFAULT_CURRENCY),
                inStock,
                product.merchantId()
        );
    }

    private StorefrontProductDetail.Variant toDetailVariant(
            GetProductBySlugResult.Variant variant,
            PriceStockIndex index
    ) {
        CalculatedPriceSetResult calculated = index.priceForVariant(variant.id());
        SkuAvailabilityResult stock = index.stockBySku.get(variant.sku());
        boolean untracked = !variant.manageInventory();
        int available = stock == null ? 0 : stock.availableQuantity();
        boolean inStock = untracked || (stock != null && stock.inStock());
        if (untracked && stock == null) {
            available = 1;
        }
        StorefrontProductDetail.Price price = calculated == null
                ? null
                : new StorefrontProductDetail.Price(
                        calculated.calculatedAmount(),
                        calculated.originalAmount(),
                        calculated.currencyCode() == null ? DEFAULT_CURRENCY : calculated.currencyCode()
                );
        return new StorefrontProductDetail.Variant(
                variant.id(),
                variant.sku(),
                variant.status(),
                variant.variations().stream()
                        .map(variation -> new StorefrontProductDetail.Variation(
                                variation.optionId(),
                                variation.optionName(),
                                variation.typeId(),
                                variation.typeName()
                        ))
                        .toList(),
                price,
                new StorefrontProductDetail.Stock(available, inStock)
        );
    }

    private PriceStockIndex loadIndex(Collection<VariantRef> variants) {
        List<String> variantIds = variants.stream().map(VariantRef::variantId).filter(Objects::nonNull).distinct().toList();
        List<String> skus = variants.stream().map(VariantRef::sku).filter(Objects::nonNull).distinct().toList();

        List<VariantPriceSetLinkResult> links = variantIds.isEmpty()
                ? List.of()
                : queryBus.dispatch(new ListVariantPriceSetLinksQuery(variantIds));
        Map<String, String> priceSetByVariantId = links.stream()
                .collect(Collectors.toMap(
                        VariantPriceSetLinkResult::variantId,
                        VariantPriceSetLinkResult::priceSetId,
                        (first, ignored) -> first
                ));
        List<Id> priceSetIds = priceSetByVariantId.values().stream()
                .distinct()
                .map(idGenerator::convertIdFrom)
                .toList();
        Map<String, CalculatedPriceSetResult> pricesBySetId = priceSetIds.isEmpty()
                ? Map.of()
                : queryBus.dispatch(new CalculatePricesQuery(priceSetIds, DEFAULT_CURRENCY, 1, Map.of())).stream()
                .collect(Collectors.toMap(CalculatedPriceSetResult::id, Function.identity(), (first, ignored) -> first));
        Map<String, SkuAvailabilityResult> stockBySku = skus.isEmpty()
                ? Map.of()
                : queryBus.dispatch(new ListSkuAvailabilityQuery(skus)).stream()
                .collect(Collectors.toMap(SkuAvailabilityResult::sku, Function.identity(), (first, ignored) -> first));
        return new PriceStockIndex(pricesBySetId, priceSetByVariantId, stockBySku);
    }

    private StorefrontCategoryNodeResponse toCategoryNode(StorefrontCategoryNode node) {
        return new StorefrontCategoryNodeResponse(
                node.id(),
                node.name(),
                node.parentId(),
                node.children().stream().map(this::toCategoryNode).toList()
        );
    }

    private StorefrontCategoryChildrenResponse.StorefrontCategoryChildResponse toCategoryChild(
            StorefrontCategoryChild child
    ) {
        return new StorefrontCategoryChildrenResponse.StorefrontCategoryChildResponse(
                child.id(),
                child.name(),
                child.parentId(),
                child.active(),
                child.listingAllowed(),
                child.c2cAllowed()
        );
    }

    private record VariantRef(String variantId, String sku) {
    }

    private record PriceStockIndex(
            Map<String, CalculatedPriceSetResult> pricesBySetId,
            Map<String, String> priceSetByVariantId,
            Map<String, SkuAvailabilityResult> stockBySku
    ) {
        CalculatedPriceSetResult priceForVariant(String variantId) {
            String priceSetId = priceSetByVariantId.get(variantId);
            if (priceSetId == null) {
                return null;
            }
            return pricesBySetId.get(priceSetId);
        }
    }
}
