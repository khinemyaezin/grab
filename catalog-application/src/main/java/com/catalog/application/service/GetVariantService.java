package com.catalog.application.service;

import com.catalog.application.port.inbound.GetVariantUseCase;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.application.port.outbound.VariantOptionQueryPort;
import com.catalog.application.readmodel.VariantOptionView;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.query.GetVariantQuery;
import com.catalog.application.query.GetVariantResult;
import com.catalog.application.service.StandaloneVariationFactory;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@lombok.RequiredArgsConstructor
public class GetVariantService implements GetVariantUseCase {

    private static final Logger log = Loggers.getLogger(GetVariantService.class);

    private final ProductRepository productRepository;
    private final VariantOptionQueryPort variantOptionQueryRepository;
    private final IdGenerator idGenerator;
    private final MatrixKeyGenerator matrixKeyGenerator;

        public GetVariantResult execute(GetVariantQuery query) {
        log.debug("Handling GetVariantQuery for productId={} variantId={}", query.productId(), query.variantId());

        Id productId = idGenerator.convertIdFrom(query.productId());
        Id merchantId = idGenerator.convertIdFrom(query.merchantId());
        Id variantId = idGenerator.convertIdFrom(query.variantId());

        Product product = productRepository.find(productId, merchantId)
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(query.productId())
                ));

        ProductVariant variant = product.findVariantById(variantId)
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.VariantNotFound(query.variantId())
                ));

        return mapToResult(product, variant);
    }

    private GetVariantResult mapToResult(Product product, ProductVariant variant) {
        List<String> optionIds = variant.getVariations().stream()
                .map(v -> v.getOptionId().getValue())
                .distinct()
                .toList();

        List<VariantOptionView> optionViews = variantOptionQueryRepository.findAllByUuidIn(optionIds);
        Map<String, VariantOptionView> variationMapByOptionId = Optional.ofNullable(optionViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .collect(Collectors.toMap(VariantOptionView::optionId, Function.identity(), (a, b) -> a));

        List<GetVariantResult.Variation> variations = variant.getVariations().stream()
                .map(pv -> mapToVariation(pv, variationMapByOptionId))
                .filter(Objects::nonNull)
                .toList();

        String matrixKey = matrixKeyGenerator.generateKey(new ArrayList<>(variant.getVariations()));

        return new GetVariantResult(
                product.getId().getValue(),
                product.getName(),
                variant.getId().getValue(),
                variant.getSku(),
                variant.getStatus().name(),
                matrixKey,
                variations,
                variant.isManageInventory(),
                variant.getMediaIds().stream().map(Id::getValue).toList(),
                variant.getThumbnailMediaId() == null ? null : variant.getThumbnailMediaId().getValue()
        );
    }

    private GetVariantResult.Variation mapToVariation(
            ProductVariation pv,
            Map<String, VariantOptionView> viewMap
    ) {
        if (StandaloneVariationFactory.isStandAloneVariation(pv)) {
            return null;
        }

        String optId = Optional.ofNullable(pv.getOptionId()).map(Id::getValue).orElse(null);
        VariantOptionView view = viewMap.get(optId);

        return (view != null)
                ? new GetVariantResult.Variation(view.optionId(), view.optionName(), view.typeId(), view.typeName())
                : new GetVariantResult.Variation(optId, "", pv.getTypeId().getValue(), "");
    }
}
