package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.infrastructure.repository.jpa.VariantOptionQueryRepository;
import com.catalog.infrastructure.view.VariantOptionView;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.query.GetVariantQuery;
import com.grab.store.catalog.internal.query.GetVariantResult;
import com.grab.store.catalog.internal.service.StandaloneVariationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetVariantQueryHandler implements QueryHandler<GetVariantQuery, GetVariantResult> {

    private static final Logger log = Loggers.getLogger(GetVariantQueryHandler.class);

    private final ProductRepository productRepository;
    private final VariantOptionQueryRepository variantOptionQueryRepository;
    private final IdGenerator idGenerator;
    private final MatrixKeyGenerator matrixKeyGenerator;

    @Override
    @CatalogReadTransactional
    public GetVariantResult handle(GetVariantQuery query) {
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

    @Override
    public Class<GetVariantQuery> getQueryType() {
        return GetVariantQuery.class;
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
