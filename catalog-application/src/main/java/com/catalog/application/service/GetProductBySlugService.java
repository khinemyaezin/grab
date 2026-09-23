package com.catalog.application.service;

import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.port.inbound.GetProductBySlugUseCase;
import com.catalog.application.port.outbound.MerchantAvailabilityQueryPort;
import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.port.outbound.VariantOptionQueryPort;
import com.catalog.application.model.read.GetProductBySlugQuery;
import com.catalog.application.model.read.GetProductBySlugResult;
import com.catalog.application.model.read.ProductDetailView;
import com.catalog.application.model.read.VariantOptionView;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.Logger;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetProductBySlugService implements GetProductBySlugUseCase {

    private static final Logger log = Loggers.getLogger(GetProductBySlugService.class);

    private final ProductQueryPort productQueryPort;
    private final VariantOptionQueryPort variantOptionQueryPort;
    private final MerchantAvailabilityQueryPort merchantAvailabilityQueryPort;
    private final ProductMediaConverter productMediaConverter;
    private final IdGenerator idGenerator;

    public GetProductBySlugResult execute(GetProductBySlugQuery query) {
        log.debug("Handling GetProductBySlugQuery for slug: {}", query.slug());

        ProductDetailView product = productQueryPort.findDetailBySlug(query.slug())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFoundBySlug(query.slug())
                ));

        if (!product.visibleOnStorefront()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.ProductNotFoundBySlug(query.slug())
            );
        }

        MerchantAvailabilityQueryPort.MerchantAvailabilitySlice availability = merchantAvailabilityQueryPort
                .findByMerchantId(product.merchantId())
                .orElse(null);
        if (availability == null || !"ACTIVE".equals(availability.status())) {
            throw new CatalogServiceException(
                    new CatalogServiceError.ProductNotFoundBySlug(query.slug())
            );
        }

        return mapToSlugResult(product, availability.merchantType());
    }

    private GetProductBySlugResult mapToSlugResult(ProductDetailView product, String merchantType) {
        List<String> optionIds = product.variants().stream()
                .flatMap(v -> v.variations().stream())
                .map(ProductDetailView.VariationView::optionId)
                .distinct()
                .toList();

        List<VariantOptionView> optionViews = variantOptionQueryPort.findAllByUuidIn(optionIds);
        List<GetProductBySlugResult.Variant> variants = mapToSlugResultVariant(product.variants(), optionViews);
        List<GetProductBySlugResult.VariantType> variantTypes = extractSlugVariantTypes(optionViews);

        return new GetProductBySlugResult(
                product.id(),
                product.name(),
                product.categoryId(),
                product.merchantId(),
                merchantType,
                product.listingCondition(),
                product.status().name(),
                product.slug(),
                product.featured(),
                product.descriptions().stream()
                        .map(description -> new GetProductBySlugResult.Description(
                                description.id(),
                                description.name(),
                                description.title(),
                                description.description()
                        ))
                        .toList(),
                productMediaConverter.toGetProductBySlugMediasFromViews(product.medias()),
                variants,
                variantTypes
        );
    }

    private List<GetProductBySlugResult.VariantType> extractSlugVariantTypes(List<VariantOptionView> optionViews) {
        ParentChildTransformer<VariantOptionView, Id, GetProductBySlugResult.VariantType, GetProductBySlugResult.VariantOption> transformer =
                ParentChildTransformer.of(
                        view -> idGenerator.convertIdFrom(view.typeId()),
                        view -> new GetProductBySlugResult.VariantType(view.typeId(), view.typeName(), new ArrayList<>()),
                        view -> new GetProductBySlugResult.VariantOption(view.optionId(), view.optionName()),
                        (parent, child) -> {
                            List<GetProductBySlugResult.VariantOption> options = new ArrayList<>(parent.options());
                            options.add(child);
                            return new GetProductBySlugResult.VariantType(parent.typeId(), parent.typeName(), options);
                        }
                );
        return transformer.apply(optionViews);
    }

    private List<GetProductBySlugResult.Variant> mapToSlugResultVariant(
            List<ProductDetailView.VariantView> variantList,
            List<VariantOptionView> optionViews
    ) {
        Map<String, VariantOptionView> variationMapByOptionId = optionViews.stream()
                .collect(Collectors.toMap(VariantOptionView::optionId, Function.identity()));
        List<GetProductBySlugResult.Variant> result = new ArrayList<>(variantList.size());

        for (ProductDetailView.VariantView variant : variantList) {
            List<GetProductBySlugResult.Variation> variations = new ArrayList<>();
            for (ProductDetailView.VariationView productVariation : variant.variations()) {
                if (StandaloneVariationFactory.isStandAloneVariation(productVariation.optionId())) {
                    continue;
                }
                VariantOptionView variantOptionView = variationMapByOptionId.get(productVariation.optionId());
                if (variantOptionView == null) {
                    continue;
                }
                variations.add(new GetProductBySlugResult.Variation(
                        variantOptionView.optionId(),
                        variantOptionView.optionName(),
                        variantOptionView.typeId(),
                        variantOptionView.typeName()
                ));
            }

            result.add(new GetProductBySlugResult.Variant(
                    variant.id(),
                    variant.sku(),
                    variant.status(),
                    variations,
                    variant.manageInventory(),
                    variant.mediaIds(),
                    variant.thumbnailMediaId()
            ));
        }
        return result;
    }
}
