package com.catalog.application.service;

import com.catalog.application.port.inbound.GetProductBySlugUseCase;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.application.port.outbound.MerchantAvailabilityPort;
import com.catalog.application.port.outbound.VariantOptionQueryPort;
import com.catalog.application.readmodel.VariantOptionView;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.query.GetProductBySlugQuery;
import com.catalog.application.query.GetProductBySlugResult;
import com.catalog.application.query.ProductMediaQueryMapper;
import com.catalog.application.service.ParentChildTransformer;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@lombok.RequiredArgsConstructor
public class GetProductBySlugService implements GetProductBySlugUseCase {

    private static final Logger log = Loggers.getLogger(GetProductBySlugService.class);

    private final ProductRepository productRepository;
    private final VariantOptionQueryPort variantOptionQueryRepository;
    private final MerchantAvailabilityPort merchantAvailabilityPort;
    private final IdGenerator idGenerator;
    private final ProductMediaQueryMapper productMediaQueryMapper;

        public GetProductBySlugResult execute(GetProductBySlugQuery query) {
        log.debug("Handling GetProductBySlugQuery for slug: {}", query.slug());

        Product product = productRepository.findBySlug(query.slug())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFoundBySlug(query.slug())
                ));

        if (!product.isVisibleOnStorefront()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.ProductNotFoundBySlug(query.slug())
            );
        }

        MerchantAvailabilityPort.MerchantAvailabilitySlice availability = merchantAvailabilityPort
                .findByMerchantId(product.getMerchantId().getValue())
                .orElse(null);
        if (availability == null || !"ACTIVE".equals(availability.status())) {
            throw new CatalogServiceException(
                    new CatalogServiceError.ProductNotFoundBySlug(query.slug())
            );
        }

        return mapToSlugResult(product, availability.merchantType());
    }

    public GetProductBySlugResult mapToSlugResult(Product product, String merchantType) {
        List<ProductVariation> allVariations = product.getVariants().stream()
                .flatMap(v -> v.getVariations().stream())
                .toList();

        List<String> optionIds = allVariations.stream()
                .map(v -> v.getOptionId().getValue())
                .distinct()
                .toList();

        List<VariantOptionView> optionViews = fetchVariantOptions(optionIds);

        List<GetProductBySlugResult.Variant> variants = mapToSlugResultVariant(product.getVariants(), optionViews);

        List<GetProductBySlugResult.VariantType> variantTypes = extractSlugVariantTypes(optionViews);

        return new GetProductBySlugResult(
                product.getId().getValue(),
                product.getName(),
                product.getCategoryId().getValue(),
                product.getMerchantId().getValue(),
                merchantType,
                product.getListingCondition() == null ? null : product.getListingCondition().name(),
                product.getStatus().name(),
                product.getSlug(),
                product.isFeatured(),
                product.getDescriptions().stream()
                        .map(description -> new GetProductBySlugResult.Description(
                                description.getId() == null ? null : description.getId().getValue(),
                                description.getName(),
                                description.getTitle(),
                                description.getDescription()
                        ))
                        .toList(),
                productMediaQueryMapper.toGetProductBySlugMedias(product.getMedias()),
                variants,
                variantTypes
        );
    }

    private List<VariantOptionView> fetchVariantOptions(List<String> optionIds) {
        return variantOptionQueryRepository.findAllByUuidIn(optionIds);
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

    private List<GetProductBySlugResult.Variant> mapToSlugResultVariant(List<ProductVariant> variantList, List<VariantOptionView> optionViews) {
        Map<String, VariantOptionView> variationMapByOptionId = optionViews.stream()
                .collect(Collectors.toMap(
                        VariantOptionView::optionId,
                        Function.identity()));
        List<GetProductBySlugResult.Variant> result = new ArrayList<>(variantList.size());

        for(ProductVariant variant : variantList) {
            List<GetProductBySlugResult.Variation> variations = new ArrayList<>();
            for(ProductVariation productVariation : variant.getVariations()) {
                VariantOptionView variantOptionView = variationMapByOptionId.get(productVariation.getOptionId().getValue());
                GetProductBySlugResult.Variation variation = new GetProductBySlugResult.Variation(
                        variantOptionView.optionId(),
                        variantOptionView.optionName(),
                        variantOptionView.typeId(),
                        variantOptionView.typeName()
                );
                variations.add(variation);
            }

            GetProductBySlugResult.Variant resultVariant = new GetProductBySlugResult.Variant(
                    variant.getId().getValue(),
                    variant.getSku(),
                    variant.getStatus().name(),
                    variations,
                    variant.isManageInventory(),
                    variant.getMediaIds().stream().map(Id::getValue).toList(),
                    variant.getThumbnailMediaId() == null ? null : variant.getThumbnailMediaId().getValue()
            );
            result.add(resultVariant);
        }
        return result;
    }
}
