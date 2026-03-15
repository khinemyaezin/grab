package com.grab.store.catalog.internal.command.handler;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductStatus;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.aggregate.ProductVariantStatus;
import com.catalog.domain.aggregate.VariantOption;
import com.catalog.domain.aggregate.VariantType;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.specification.UniqueSkuSpec;
import com.catalog.domain.service.*;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.domain.valueobject.VariantCombination;
import com.grab.framework.id.Id;
import com.grab.store.catalog.internal.command.SyncVariantsCommand;
import com.grab.store.catalog.internal.command.SyncVariantsResult;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SyncVariantsCommandHandler implements CommandHandler<SyncVariantsCommand, SyncVariantsResult> {

    private static final Logger log = Loggers.getLogger(SyncVariantsCommandHandler.class);

    private final ProductRepository productRepository;
    private final VariantCombinationService variantCombinationService;
    private final VariationCombinationManager variationCombinationManager;
    private final VariationKeyGenerator variationKeyGenerator;
    private final VariantDeletionStrategy variantDeletionStrategy;

    @Override
    @CatalogTransactional
    public SyncVariantsResult handle(SyncVariantsCommand command) {
        log.debug("Handling SyncVariantsCommand for productId={}", command.productId());

        Product product = productRepository.find(command.productId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(command.productId().getValue())
                ));

        List<VariantType> desiredTypes = mapToDomainVariantTypes(command.variantTypes());
        List<VariantType> filteredTypes = variantDeletionStrategy.filterVariantTypes(product, desiredTypes);

        List<VariantCombination> desiredCombinations = buildDesiredCombinations(filteredTypes);

        List<VariationCombinationManager.VariantCombinationResult> diffResults =
                variationCombinationManager.syncCombinations(product.getVariants(), desiredCombinations);

        Map<String, SyncVariantsCommand.Variant> requestLookup = buildRequestVariantLookup(command.variants());
        List<ProductVariant> targetVariants = materializeTargetVariants(
                diffResults,
                requestLookup
        );
        validateSkuAvailability(targetVariants);
        archiveProductBeforeBecomingUnsellable(product, targetVariants);

        syncProductVariants(product, targetVariants);
        productRepository.save(product);

        return toResult(product, filteredTypes);
    }

    private List<VariantType> mapToDomainVariantTypes(List<SyncVariantsCommand.VariantType> variantTypes) {
        if (variantTypes == null) {
            return Collections.emptyList();
        }

        List<VariantType> result = new ArrayList<>(variantTypes.size());
        for (SyncVariantsCommand.VariantType variantType : variantTypes) {
            if (variantType == null) continue;

            VariantType domainType = new VariantType(variantType.typeId(), variantType.typeName());

            if (variantType.options() != null) {
                for (SyncVariantsCommand.VariantOption option : variantType.options()) {
                    if (option == null) continue;
                    domainType.addOption(new VariantOption(option.optionId(), option.optionName(), domainType));
                }
            }

            result.add(domainType);
        }

        return result;
    }

    private List<VariantCombination> buildDesiredCombinations(List<VariantType> desiredVariantTypes) {
        List<List<VariantOption>> combinations = variantCombinationService.generateCombinations(desiredVariantTypes);
        if (combinations.isEmpty()) {
            return Collections.emptyList();
        }

        return convertToVariantCombination(combinations);
    }

    private List<VariantCombination> convertToVariantCombination(List<List<VariantOption>> combinations) {
        List<VariantCombination> result = new ArrayList<>(combinations.size());
        for (List<VariantOption> options : combinations) {
            result.add(new VariantCombination(mapOptionsToVariations(options)));
        }
        return result;
    }

    private List<ProductVariation> mapOptionsToVariations(List<VariantOption> options) {
        List<ProductVariation> result = new ArrayList<>(options.size());
        for (VariantOption opt : options) {
            result.add(new ProductVariation(
                    opt.getName(),
                    opt.getId(),
                    opt.getVariantType().getName(),
                    opt.getVariantType().getId()
            ));
        }
        return result;
    }

    private Map<String, SyncVariantsCommand.Variant> buildRequestVariantLookup(List<SyncVariantsCommand.Variant> variants) {
        Map<String, SyncVariantsCommand.Variant> byCombinationKey = new HashMap<>();

        if (variants == null) {
            return byCombinationKey;
        }

        for (SyncVariantsCommand.Variant variant : variants) {
            if (variant == null) continue;

            List<ProductVariation> domainProductVariations = mapToDomainVariations(variant.variations());
            String key = variationKeyGenerator.generateVariationKey(domainProductVariations);

            if (StringUtils.hasLength(key)) {
                SyncVariantsCommand.Variant previous = byCombinationKey.putIfAbsent(key, variant);
                if (previous != null) {
                    throw new CatalogServiceException(
                            new CatalogServiceError.DuplicateVariantCombinationKey(key)
                    );
                }
            }
        }

        return byCombinationKey;
    }

    private List<ProductVariant> materializeTargetVariants(
            List<VariationCombinationManager.VariantCombinationResult> diffResults,
            Map<String, SyncVariantsCommand.Variant> requestLookup) {

        List<ProductVariant> targetVariants = new ArrayList<>(diffResults.size());

        for (VariationCombinationManager.VariantCombinationResult diffResult : diffResults) {
            List<ProductVariation> desiredVariations = diffResult.variantCombination().getVariations();

            String combinationKey = variationKeyGenerator.generateVariationKey(desiredVariations);
            SyncVariantsCommand.Variant requestVariant = requestLookup.get(combinationKey);

            if(requestVariant == null){
                throw new CatalogServiceException(
                        new CatalogServiceError.VariationCombinationNotFound()
                );
            }

            targetVariants.add(new ProductVariant(
                    requestVariant.id(),
                    requestVariant.sku(),
                    ProductVariantStatus.ACTIVE,
                    desiredVariations
            ));
        }

        return targetVariants;
    }

    private void validateSkuAvailability(List<ProductVariant> targetVariants) {
        List<String> reservedSkus = new ArrayList<>(targetVariants.size());

        for (ProductVariant targetVariant : targetVariants) {
            if (!new UniqueSkuSpec(reservedSkus).isSatisfiedBy(targetVariant)
                    || productRepository.isSkuTaken(targetVariant.getSku(), targetVariant.getId().getValue())) {
                throw new CatalogServiceException(
                        new CatalogServiceError.SkuAlreadyExists(targetVariant.getSku())
                );
            }
            reservedSkus.add(targetVariant.getSku());
        }
    }

    private void syncProductVariants(Product product, List<ProductVariant> targetVariants) {
        for (ProductVariant targetVariant : targetVariants) {
            Optional<ProductVariant> existing = product.findVariantById(targetVariant.getId());
            if (existing.isPresent()) {
                boolean updated = product.updateVariant(existing.get(), targetVariant);
                if (!updated) {
                    throw new CatalogServiceException(
                            new CatalogServiceError.VariantUpdateFailed(targetVariant.getId().getValue())
                    );
                }
                continue;
            }

            Optional<ProductVariant> replacementCandidate = findVariantByVariations(product, targetVariant);
            if (replacementCandidate.isPresent()) {
                boolean updated = product.updateVariant(replacementCandidate.get(), targetVariant);
                if (!updated) {
                    throw new CatalogServiceException(
                            new CatalogServiceError.VariantUpdateFailed(targetVariant.getId().getValue())
                    );
                }
            } else {
                boolean added = product.addVariant(targetVariant);
                if (!added) {
                    throw new CatalogServiceException(
                            new CatalogServiceError.VariantAddFailed(targetVariant.getId().getValue())
                    );
                }
            }
        }

        Set<Id> targetVariantIds = new HashSet<>();
        for (ProductVariant targetVariant : targetVariants) {
            targetVariantIds.add(targetVariant.getId());
        }

        List<Id> existingVariantIds = new ArrayList<>();
        for (ProductVariant variant : product.getVariants()) {
            existingVariantIds.add(variant.getId());
        }

        for (Id existingVariantId : existingVariantIds) {
            if (!targetVariantIds.contains(existingVariantId)) {
                product.removeVariant(existingVariantId);
            }
        }
    }

    private Optional<ProductVariant> findVariantByVariations(Product product, ProductVariant targetVariant) {
        return product.getVariants().stream()
                .filter(existing -> existing.getVariations().equals(targetVariant.getVariations()))
                .findFirst();
    }

    private void archiveProductBeforeBecomingUnsellable(Product product, List<ProductVariant> targetVariants) {
        boolean hasActiveTargetVariants = targetVariants.stream().anyMatch(ProductVariant::isActive);
        if (product.getStatus() == ProductStatus.ACTIVE && !hasActiveTargetVariants) {
            product.changeStatus(ProductStatus.ARCHIVED);
        }
    }

    private SyncVariantsResult toResult(Product product, List<VariantType> desiredVariantTypes) {
        List<SyncVariantsResult.Variant> variants = product.getVariants().stream()
                .map(this::toResultVariant)
                .toList();

        List<SyncVariantsResult.VariantType> variantTypes = desiredVariantTypes.stream()
                .map(this::toResultVariantType)
                .toList();

        return new SyncVariantsResult(
                product.getId(),
                product.getName(),
                variants,
                variantTypes
        );
    }

    private SyncVariantsResult.Variant toResultVariant(ProductVariant variant) {
        List<SyncVariantsResult.Variation> variations = variant.getVariations().stream()
                .map(v -> new SyncVariantsResult.Variation(
                        v.getOptionName(),
                        v.getOptionId(),
                        v.getTypeId(),
                        v.getTypeName()
                ))
                .toList();

        return new SyncVariantsResult.Variant(
                variant.getId(),
                variant.getSku(),
                variant.getStatus().name(),
                variations
        );
    }

    private SyncVariantsResult.VariantType toResultVariantType(VariantType variantType) {
        List<SyncVariantsResult.VariantOption> options = variantType.getOptions().stream()
                .map(option -> new SyncVariantsResult.VariantOption(
                        option.getId(),
                        option.getName()
                ))
                .toList();
        return new SyncVariantsResult.VariantType(
                variantType.getId(),
                variantType.getName(),
                options
        );
    }

    private List<ProductVariation> mapToDomainVariations(List<SyncVariantsCommand.Variation> variations) {
        if (variations == null) {
            return Collections.emptyList();
        }
        List<ProductVariation> mapped = new ArrayList<>(variations.size());
        for (SyncVariantsCommand.Variation variation : variations) {
            if (variation == null) continue;
            mapped.add(new ProductVariation(
                    variation.optionName(),
                    variation.optionId(),
                    variation.typeName(),
                    variation.typeId()
            ));
        }
        return mapped;
    }

    @Override
    public Class<SyncVariantsCommand> getCommandType() {
        return SyncVariantsCommand.class;
    }


}
