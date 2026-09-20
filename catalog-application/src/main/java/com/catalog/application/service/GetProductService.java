package com.catalog.application.service;

import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.port.inbound.GetProductUseCase;
import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.port.outbound.VariantOptionQueryPort;
import com.catalog.application.model.read.GetProductQuery;
import com.catalog.application.model.read.GetProductResult;
import com.catalog.application.model.read.CategoryView;
import com.catalog.application.model.read.ProductDetailView;
import com.catalog.application.model.read.ProductPublicationView;
import com.catalog.application.model.read.VariantOptionView;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.Logger;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetProductService implements GetProductUseCase {

    private static final Logger log = Loggers.getLogger(GetProductService.class);

    private final ProductQueryPort productQueryPort;
    private final VariantOptionQueryPort variantOptionQueryPort;
    private final IdGenerator idGenerator;
    private final CategoryQueryPort categoryQueryPort;
    private final MatrixKeyGenerator matrixKeyGenerator;
    private final ProductMediaConverter productMediaConverter;

    public GetProductResult execute(GetProductQuery query) {
        log.debug("Handling GetProductQuery for productId: {}", query.productId());

        ProductDetailView product = productQueryPort.findDetailByIdAndMerchantId(query.productId(), query.merchantId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(query.productId())
                ));

        List<ProductPublicationView> publicationViews = productQueryPort
                .findPublicationsByProductIds(List.of(query.productId()));
        return mapToResult(product, publicationViews);
    }

    private GetProductResult mapToResult(
            ProductDetailView product,
            List<ProductPublicationView> publicationViews
    ) {
        List<String> optionIds = product.variants().stream()
                .flatMap(variant -> variant.variations().stream())
                .map(ProductDetailView.VariationView::optionId)
                .distinct()
                .toList();

        List<VariantOptionView> optionViews = variantOptionQueryPort.findAllByUuidIn(optionIds);
        List<GetProductResult.Variant> variants = mapToResultVariantList(product.variants(), optionViews, publicationViews);
        List<GetProductResult.VariantType> variantTypes = extractVariantTypes(optionViews);
        GetProductResult.Category category = findCategoryById(product.categoryId())
                .map(this::mapToCategory)
                .orElse(null);

        return new GetProductResult(
                product.id(),
                product.name(),
                category,
                product.listingCondition(),
                product.status().name(),
                product.slug(),
                mapDescriptions(product.descriptions()),
                productMediaConverter.toGetProductMediasFromViews(product.medias()),
                variants,
                variantTypes
        );
    }

    private List<GetProductResult.Description> mapDescriptions(List<ProductDetailView.DescriptionView> descriptions) {
        if (descriptions == null || descriptions.isEmpty()) {
            return List.of();
        }
        return descriptions.stream()
                .map(description -> new GetProductResult.Description(
                        description.id(),
                        description.name(),
                        description.title(),
                        description.description()
                ))
                .toList();
    }

    private List<GetProductResult.VariantType> extractVariantTypes(List<VariantOptionView> optionViews) {
        if (optionViews == null || optionViews.isEmpty()) {
            return Collections.emptyList();
        }

        ParentChildTransformer<VariantOptionView, Id, GetProductResult.VariantType, GetProductResult.VariantOption> transformer =
                ParentChildTransformer.of(
                        view -> idGenerator.convertIdFrom(view.typeId()),
                        view -> new GetProductResult.VariantType(view.typeId(), view.typeName(), new ArrayList<>()),
                        view -> new GetProductResult.VariantOption(view.optionId(), view.optionName()),
                        (parent, child) -> {
                            List<GetProductResult.VariantOption> options = new ArrayList<>(parent.options());
                            options.add(child);
                            return new GetProductResult.VariantType(parent.typeId(), parent.typeName(), options);
                        }
                );
        return transformer.apply(optionViews);
    }

    private List<GetProductResult.Variant> mapToResultVariantList(
            List<ProductDetailView.VariantView> variantList,
            List<VariantOptionView> optionViews,
            List<ProductPublicationView> publicationViews
    ) {
        Map<String, VariantOptionView> variationMapByOptionId = Optional.ofNullable(optionViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .collect(Collectors.toMap(VariantOptionView::optionId, Function.identity(), (a, b) -> a));
        Map<String, List<GetProductResult.Publication>> publicationsByVariantId =
                publicationsByVariantId(publicationViews);

        return variantList.stream().map(variant -> {
            List<GetProductResult.Variation> variations = variant.variations().stream()
                    .map(variation -> mapToVariation(variation, variationMapByOptionId))
                    .filter(Objects::nonNull)
                    .toList();

            String matrixKey = matrixKeyGenerator.generateKey(toDomainVariations(variant));
            List<GetProductResult.Publication> publications = publicationsByVariantId.getOrDefault(
                    variant.id(),
                    List.of()
            );

            return new GetProductResult.Variant(
                    variant.id(),
                    variant.sku(),
                    variant.status(),
                    matrixKey,
                    variations,
                    variant.manageInventory(),
                    variant.mediaIds(),
                    variant.thumbnailMediaId(),
                    publications
            );
        }).toList();
    }

    private List<ProductVariation> toDomainVariations(ProductDetailView.VariantView variant) {
        return variant.variations().stream()
                .map(v -> new ProductVariation(
                        idGenerator.convertIdFrom(v.optionId()),
                        idGenerator.convertIdFrom(v.typeId())
                ))
                .toList();
    }

    private Map<String, List<GetProductResult.Publication>> publicationsByVariantId(
            List<ProductPublicationView> publicationViews
    ) {
        return Optional.ofNullable(publicationViews)
                .orElseGet(List::of)
                .stream()
                .collect(Collectors.groupingBy(
                        ProductPublicationView::variantId,
                        Collectors.mapping(
                                view -> new GetProductResult.Publication(view.salesChannelId()),
                                Collectors.toList()
                        )
                ));
    }

    private GetProductResult.Variation mapToVariation(
            ProductDetailView.VariationView variation,
            Map<String, VariantOptionView> viewMap
    ) {
        if (StandaloneVariationFactory.isStandAloneVariation(variation.optionId())) {
            return null;
        }
        VariantOptionView view = viewMap.get(variation.optionId());
        return (view != null)
                ? new GetProductResult.Variation(view.optionId(), view.optionName(), view.typeId(), view.typeName())
                : new GetProductResult.Variation(variation.optionId(), "", variation.typeId(), "");
    }

    private Optional<CategoryView> findCategoryById(String categoryId) {
        if (categoryId == null) {
            return Optional.empty();
        }
        List<CategoryView> matchedCategoryViews = categoryQueryPort.findViewByIds(List.of(categoryId));
        if (!matchedCategoryViews.isEmpty()) {
            return Optional.of(matchedCategoryViews.getFirst());
        }
        log.error("Category id {} not found", categoryId);
        return Optional.empty();
    }

    private GetProductResult.Category mapToCategory(CategoryView categoryView) {
        return new GetProductResult.Category(categoryView.id(), categoryView.name());
    }
}
