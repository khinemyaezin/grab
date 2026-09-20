package com.catalog.application.service;

import com.catalog.application.port.inbound.GetProductUseCase;

import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.port.outbound.VariantOptionQueryPort;
import com.catalog.application.readmodel.CategoryView;
import com.catalog.application.readmodel.VariantOptionView;
import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.IdGenerator;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.query.GetProductQuery;
import com.catalog.application.query.GetProductResult;
import com.catalog.application.query.ProductMediaQueryMapper;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.application.service.ParentChildTransformer;
import com.catalog.application.service.StandaloneVariationFactory;
import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.readmodel.ProductPublicationView;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@lombok.RequiredArgsConstructor
public class GetProductService implements GetProductUseCase {

    private static final Logger log = Loggers.getLogger(GetProductService.class);

    private final ProductRepository productRepository;
    private final ProductQueryPort productQueryRepository;
    private final VariantOptionQueryPort variantOptionQueryRepository;
    private final IdGenerator idGenerator;
    private final CategoryQueryPort categoryQueryRepository;
    private final MatrixKeyGenerator matrixKeyGenerator;
    private final ProductMediaQueryMapper productMediaQueryMapper;

        public GetProductResult execute(GetProductQuery query) {
        log.debug("Handling GetProductQuery for productId: {}", query.productId());

        Id productId = idGenerator.convertIdFrom(query.productId());
        Id merchantId = idGenerator.convertIdFrom(query.merchantId());
        Product product = productRepository.find(productId, merchantId)
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(query.productId())
                ));

        List<ProductPublicationView> publicationViews = productQueryRepository
                .findPublicationsByProductIds(List.of(query.productId()));
        return mapToResult(product, publicationViews);
    }

    public GetProductResult mapToResult(Product product) {
        return mapToResult(product, List.of());
    }

    public GetProductResult mapToResult(Product product, List<ProductPublicationView> publicationViews) {
        List<ProductVariation> allVariations = product.getVariants().stream()
                .flatMap(v -> v.getVariations().stream())
                .toList();

        List<String> optionIds = allVariations.stream()
                .map(v -> v.getOptionId().getValue())
                .distinct()
                .toList();

        List<VariantOptionView> optionViews = fetchVariantOptions(optionIds);
        List<GetProductResult.Variant> variants = mapToResultVariantList(
                product.getVariants(),
                optionViews,
                publicationViews
        );
        List<GetProductResult.VariantType> variantTypes = extractVariantTypes(optionViews);
        GetProductResult.Category category = findCategoryById(product.getCategoryId())
                .map(this::mapToCategory)
                .orElse(null);

        return new GetProductResult(
                product.getId().getValue(),
                product.getName(),
                category,
                product.getListingCondition() == null ? null : product.getListingCondition().name(),
                product.getStatus().name(),
                product.getSlug(),
                mapDescriptions(product.getDescriptions()),
                productMediaQueryMapper.toGetProductMedias(product.getMedias()),
                variants,
                variantTypes
        );
    }

    private List<GetProductResult.Description> mapDescriptions(List<Description> descriptions) {
        if (descriptions == null || descriptions.isEmpty()) {
            return List.of();
        }
        return descriptions.stream()
                .map(description -> new GetProductResult.Description(
                        description.getId() == null ? null : description.getId().getValue(),
                        description.getName(),
                        description.getTitle(),
                        description.getDescription()
                ))
                .toList();
    }

    private List<GetProductResult.VariantType> extractVariantTypes(List<VariantOptionView> optionViews) {
        if (optionViews == null || optionViews.isEmpty()) return Collections.emptyList();

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

    private List<VariantOptionView> fetchVariantOptions(List<String> optionIds) {
        return variantOptionQueryRepository.findAllByUuidIn(optionIds);
    }

    private List<GetProductResult.Variant> mapToResultVariantList(
            List<ProductVariant> variantList,
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
            List<GetProductResult.Variation> variations = variant.getVariations().stream()
                    .map(pv -> mapToVariation(pv, variationMapByOptionId))
                    .filter(Objects::nonNull)
                    .toList();

            String matrixKey = matrixKeyGenerator.generateKey(new ArrayList<>(variant.getVariations()));
            List<GetProductResult.Publication> publications = publicationsByVariantId.getOrDefault(
                    variant.getId().getValue(),
                    List.of()
            );

            return new GetProductResult.Variant(
                    variant.getId().getValue(),
                    variant.getSku(),
                    variant.getStatus().name(),
                    matrixKey,
                    variations,
                    variant.isManageInventory(),
                    variant.getMediaIds().stream().map(Id::getValue).toList(),
                    variant.getThumbnailMediaId() == null ? null : variant.getThumbnailMediaId().getValue(),
                    publications
            );
        }).toList();
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

    private GetProductResult.Variation mapToVariation(ProductVariation pv, Map<String, VariantOptionView> viewMap) {
        if (StandaloneVariationFactory.isStandAloneVariation(pv)) {
            return null;
        }

        String optId = Optional.ofNullable(pv.getOptionId()).map(Id::getValue).orElse(null);
        VariantOptionView view = viewMap.get(optId);

        return (view != null)
                ? new GetProductResult.Variation(view.optionId(), view.optionName(), view.typeId(), view.typeName())
                : new GetProductResult.Variation(optId, "", pv.getTypeId().getValue(), "");
    }

    private Optional<CategoryView> findCategoryById(Id id) {
        List<CategoryView> matchedCategoryViews = this.categoryQueryRepository.findViewByIds(List.of(id.getValue()));
        if (!matchedCategoryViews.isEmpty()) {
            return Optional.of(matchedCategoryViews.getFirst());
        }
        log.error("Category id {} not found", id);
        return Optional.empty();
    }

    private GetProductResult.Category mapToCategory(CategoryView categoryView) {
        return new GetProductResult.Category(categoryView.id(), categoryView.name());
    }
}
