package com.catalog.application.service;

import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.port.inbound.GetVariantUseCase;
import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.port.outbound.VariantOptionQueryPort;
import com.catalog.application.model.read.GetVariantQuery;
import com.catalog.application.model.read.GetVariantResult;
import com.catalog.application.model.read.ProductDetailView;
import com.catalog.application.model.read.VariantOptionView;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.valueobject.ProductVariation;
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
public class GetVariantService implements GetVariantUseCase {

    private static final Logger log = Loggers.getLogger(GetVariantService.class);

    private final ProductQueryPort productQueryPort;
    private final VariantOptionQueryPort variantOptionQueryPort;
    private final IdGenerator idGenerator;
    private final MatrixKeyGenerator matrixKeyGenerator;

    public GetVariantResult execute(GetVariantQuery query) {
        log.debug("Handling GetVariantQuery for productId={} variantId={}", query.productId(), query.variantId());

        ProductDetailView product = productQueryPort.findDetailByIdAndMerchantId(query.productId(), query.merchantId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(query.productId())
                ));

        ProductDetailView.VariantView variant = product.variants().stream()
                .filter(v -> query.variantId().equals(v.id()))
                .findFirst()
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.VariantNotFound(query.variantId())
                ));

        return mapToResult(product, variant);
    }

    private GetVariantResult mapToResult(ProductDetailView product, ProductDetailView.VariantView variant) {
        List<String> optionIds = variant.variations().stream()
                .map(ProductDetailView.VariationView::optionId)
                .distinct()
                .toList();

        List<VariantOptionView> optionViews = variantOptionQueryPort.findAllByUuidIn(optionIds);
        Map<String, VariantOptionView> variationMapByOptionId = Optional.ofNullable(optionViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .collect(Collectors.toMap(VariantOptionView::optionId, Function.identity(), (a, b) -> a));

        List<GetVariantResult.Variation> variations = variant.variations().stream()
                .map(v -> mapToVariation(v, variationMapByOptionId))
                .filter(Objects::nonNull)
                .toList();

        List<ProductVariation> domainVariations = variant.variations().stream()
                .map(v -> new ProductVariation(
                        idGenerator.convertIdFrom(v.optionId()),
                        idGenerator.convertIdFrom(v.typeId())
                ))
                .toList();
        String matrixKey = matrixKeyGenerator.generateKey(new ArrayList<>(domainVariations));

        return new GetVariantResult(
                product.id(),
                product.name(),
                variant.id(),
                variant.sku(),
                variant.status(),
                matrixKey,
                variations,
                variant.manageInventory(),
                variant.mediaIds(),
                variant.thumbnailMediaId()
        );
    }

    private GetVariantResult.Variation mapToVariation(
            ProductDetailView.VariationView variation,
            Map<String, VariantOptionView> viewMap
    ) {
        if (StandaloneVariationFactory.isStandAloneVariation(variation.optionId())) {
            return null;
        }
        VariantOptionView view = viewMap.get(variation.optionId());
        return (view != null)
                ? new GetVariantResult.Variation(view.optionId(), view.optionName(), view.typeId(), view.typeName())
                : new GetVariantResult.Variation(variation.optionId(), "", variation.typeId(), "");
    }
}
