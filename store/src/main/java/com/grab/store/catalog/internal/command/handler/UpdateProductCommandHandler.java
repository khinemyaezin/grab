package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.*;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.service.SkuGenerator;
import com.catalog.domain.service.MatrixCombinationService;
import com.catalog.domain.service.MatrixCombinationSynchronizer;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.service.dto.VariantOptionSelection;
import com.catalog.domain.service.dto.VariantTypeSelection;
import com.catalog.domain.valueobject.*;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.command.GetProductPayload;
import com.grab.store.catalog.internal.command.UpdateProductCommand;
import com.grab.store.catalog.internal.command.UpdateProductResult;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogCommandHandlerError;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.util.CatalogPolicyValidator;
import com.grab.store.catalog.internal.util.StandaloneVariationFactory;
import com.grab.store.catalog.internal.util.UniqueSlugResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UpdateProductCommandHandler implements CommandHandler<UpdateProductCommand, UpdateProductResult> {

    private static final Logger log = Loggers.getLogger(UpdateProductCommandHandler.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UniqueSlugResolver uniqueSlugResolver;
    private final IdGenerator idGenerator;
    private final SkuGenerator skuGenerator;
    private final MatrixCombinationService matrixCombinationService;
    private final MatrixCombinationSynchronizer matrixCombinationSynchronizer;
    private final MatrixKeyGenerator matrixKeyGenerator;

    @Override
    @CatalogTransactional
    public UpdateProductResult handle(UpdateProductCommand command) {
        log.debug("Handling UpdateProductCommand for productId={}", command.productId());

        Product product = findProductOrElseThrow(command.productId(), command.merchantId());
        Category category = findCategoryOrElseThrow(command.categoryId());

        ProductMetadata next = getProductMetadata(command, product, category);

        product.updateMetadata(next);
        applyVariantSync(product, command);

        CatalogPolicyValidator.validateCategoryPolicy(category);

        productRepository.save(product);

        return new UpdateProductResult(
                product.getId().getValue(),
                product.getName(),
                product.getCategoryId() != null ? product.getCategoryId().getValue() : "",
                product.getListingCondition() != null ? product.getListingCondition().name() : null,
                product.getStatus() != null ? product.getStatus().name() : null,
                product.getSlug(),
                mapPayloadDescriptions(product.getDescriptions()),
                mapPayloadMedias(product.getMedias())
        );
    }

    @Override
    public Class<UpdateProductCommand> getCommandType() {
        return UpdateProductCommand.class;
    }

    private Product findProductOrElseThrow(Id productId, Id merchantId) {
        return productRepository.find(productId, merchantId)
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(productId.getValue())
                ));
    }

    private Category findCategoryOrElseThrow(Id categoryId) {
        return categoryRepository.find(categoryId).orElseThrow(() -> new CatalogServiceException(
                new CatalogServiceError.CategoryNotFound(categoryId.getValue())
        ));
    }

    private ProductMetadata getProductMetadata(UpdateProductCommand command, Product existingProduct, Category category) {
        ProductMetadata current = existingProduct.metadata();
        return new ProductMetadata(
                command.name() != null ? command.name() : current.name(),
                category.getId(),
                mapConditionCached(command.condition(), current.condition()),
                resolveSlug(command, existingProduct)
        );
    }

    private void applyVariantSync(Product product, UpdateProductCommand command) {
        UpdateProductCommand.VariantSync variantSync = command.variantSync();
        UpdateProductCommand.VariantSyncIntent intent =
                Objects.isNull(variantSync) || Objects.isNull(variantSync.intent())
                        ? UpdateProductCommand.VariantSyncIntent.LEAVE_AS_IS
                        : variantSync.intent();

        switch (intent) {
            case COLLAPSE_TO_STANDALONE -> syncProductVariants(product, List.of(createStandaloneVariant(command)));
            case FULL_SYNC -> handleFullSync(product, variantSync);
        }

    }

    private void handleFullSync(Product product, UpdateProductCommand.VariantSync variantSync) {
        if (variantSync.overrides() == null || variantSync.overrides().isEmpty()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.EmptyVariantOverrides(UpdateProductCommand.VariantSyncIntent.FULL_SYNC.name())
            );
        }

        List<VariantTypeSelection> variantTypes = resolveVariantTypes(variantSync);
        List<VariantCombination> variantCombinations = generateVariantCombinations(variantTypes);
        Map<String, MatrixCombinationSynchronizer.VariantCombinationResult> combinationResultMap =
                buildCombinationResultMap(product, variantCombinations);

        List<ProductVariant> resultVariants = buildTargetVariants(product, variantSync.overrides(), combinationResultMap);
        syncProductVariants(product, resultVariants);
    }

    private List<VariantTypeSelection> resolveVariantTypes(UpdateProductCommand.VariantSync variantSync) {
        if (variantSync.variantTypes() == null || variantSync.variantTypes().isEmpty()) {
            log.error("handleFullSync: variantTypes is null or empty during full sync");
            log.info("Falling back to overrides for variant type selection");
            return getVariantTypeSelectionList(variantSync.overrides());
        }
        return mapVariantTypeSelectionList(variantSync.variantTypes());
    }

    private List<VariantCombination> generateVariantCombinations(List<VariantTypeSelection> variantTypes) {
        List<List<VariantOptionSelection>> combinations = matrixCombinationService.generateMatrixCombination(variantTypes);
        return mapVariantCombinations(combinations);
    }

    private Map<String, MatrixCombinationSynchronizer.VariantCombinationResult> buildCombinationResultMap(
            Product product, List<VariantCombination> variantCombinations) {
        List<MatrixCombinationSynchronizer.VariantCombinationResult> syncResults =
                matrixCombinationSynchronizer.syncMatrixCombination(product.getVariants(), variantCombinations);
        return getOverrideVariantMapByMatrixKey(syncResults);
    }

    private List<ProductVariant> buildTargetVariants(
            Product product,
            List<UpdateProductCommand.Variant> overrides,
            Map<String, MatrixCombinationSynchronizer.VariantCombinationResult> combinationResultMap) {
        
        List<ProductVariant> resultVariants = new ArrayList<>(overrides.size());
        
        for (UpdateProductCommand.Variant overrideVariant : overrides) {
            ProductVariant targetVariant = resolveOrCreateVariant(product, overrideVariant, combinationResultMap);
            resultVariants.add(targetVariant);
        }
        
        return resultVariants;
    }

    private ProductVariant resolveOrCreateVariant(
            Product product,
            UpdateProductCommand.Variant overrideVariant,
            Map<String, MatrixCombinationSynchronizer.VariantCombinationResult> combinationResultMap) {
        
        MatrixCombinationSynchronizer.VariantCombinationResult combinationResult =
                combinationResultMap.get(overrideVariant.matrixKey());

        if (combinationResult == null) {
            log.error("Override variant with matrixKey={} does not match any generated combination", overrideVariant.matrixKey());
            throw new CatalogServiceException(
                    new CatalogCommandHandlerError.VariantOverrideCombinationNotFound(overrideVariant.matrixKey())
            );
        }

        if (Objects.equals(combinationResult.matchedType(),
                MatrixCombinationSynchronizer.VariantCombinationResult.MatchedType.UNCHANGED)) {
            ProductVariant existingVariant = combinationResult.productVariants().getFirst();

            return ProductVariant.create(
                    existingVariant.getId(),
                    overrideVariant.sku(),
                    new ArrayList<>(existingVariant.getVariations())
            );
        }

        return createVariant(
                product.getName(),
                overrideVariant,
                combinationResult.variantCombination().variations()
        );
    }

    private ProductVariant createVariant(String productName, UpdateProductCommand.Variant overrideVariant, List<ProductVariation> variations) {
        Id variantId = idGenerator.generateId();
        String sku = StringUtils.hasText(overrideVariant.sku())
                ? overrideVariant.sku()
                : generateSku(productName, variations);

        return ProductVariant.create(
                variantId,
                sku,
                variations
        );
    }

    private Map<String, MatrixCombinationSynchronizer.VariantCombinationResult> getOverrideVariantMapByMatrixKey(
            List<MatrixCombinationSynchronizer.VariantCombinationResult> variants) {
        if (variants == null) {
            return Map.of();
        }

        return variants.stream()
                .collect(Collectors.toMap(
                        result-> matrixKeyGenerator.generateKey(result.variantCombination().variations()),
                        Function.identity()));
    }

    private List<VariantTypeSelection> getVariantTypeSelectionList(List<UpdateProductCommand.Variant> variants) {
        if (variants == null || variants.isEmpty()) {
            return List.of();
        }
        return variants.stream()
                .flatMap(variant -> variant.variations().stream())
                .collect(Collectors.groupingBy(
                        UpdateProductCommand.Variation::typeId,
                        Collectors.mapping(
                                variation -> new VariantOptionSelection(
                                        variation.optionId(),
                                        variation.typeId()),
                                Collectors.toList()
                        )
                ))
                .entrySet().stream()
                .map(entry -> new VariantTypeSelection(entry.getKey(), entry.getValue()))
                .toList();
    }

    private ProductVariant createStandaloneVariant(UpdateProductCommand command) {
        UpdateProductCommand.Variant variant = command.variantSync().overrides().stream()
                .findFirst()
                .orElse(null);
        List<ProductVariation> variations = StandaloneVariationFactory.create(idGenerator);
        String sku = variant != null
                && StringUtils.hasText(variant.sku())
                ? variant.sku()
                : generateSku(command.name(), variations);

        return ProductVariant.create(
                idGenerator.generateId(),
                sku,
                variations
        );
    }

    private String generateSku(String productName, List<ProductVariation> variations) {
        return skuGenerator.generate(new SkuGenerator.Context(productName, variations));
    }

    private void syncProductVariants(Product product, List<ProductVariant> targetVariants) {
        for (ProductVariant targetVariant : targetVariants) {
            Optional<ProductVariant> existing = product.findVariantByVariation(targetVariant.getVariations());
            if (existing.isPresent()) {
                updateVariant(product, existing.get(), targetVariant);
            } else {
                product.addVariant(targetVariant);
            }
        }

        Set<Id> targetVariantIds = targetVariants.stream()
                .map(ProductVariant::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Id> existingVariantIds = product.getVariants().stream()
                .map(ProductVariant::getId)
                .toList();

        for (Id existingVariantId : existingVariantIds) {
            if (!targetVariantIds.contains(existingVariantId)) {
                product.removeVariant(existingVariantId);
            }
        }
    }

    private void updateVariant(Product product, ProductVariant existing, ProductVariant target) {
        boolean updated = product.updateVariant(existing, target);
        if (!updated) {
            throw new CatalogServiceException(
                    new CatalogServiceError.VariantUpdateFailed(target.getId().getValue())
            );
        }
    }

    private List<VariantCombination> mapVariantCombinations(List<List<VariantOptionSelection>> combinations) {
        return combinations.stream()
                .map(options -> new VariantCombination(
                        options.stream()
                                .map(optionSelection -> new ProductVariation(
                                        optionSelection.valueId(),
                                        optionSelection.typeId()
                                ))
                                .toList()
                ))
                .toList();
    }

    private List<VariantTypeSelection> mapVariantTypeSelectionList(List<UpdateProductCommand.VariantType> variantTypes) {
        if (variantTypes == null || variantTypes.isEmpty()) {
            return List.of();
        }
        return variantTypes.stream()
                .map(variantType -> new VariantTypeSelection(
                        variantType.typeId(),
                        variantType.options().stream()
                                .map(option -> new VariantOptionSelection(
                                        option.optionId(),
                                        variantType.typeId()))
                                .toList()
                ))
                .toList();
    }

    private SellerType mapSellerTypeCached(String sellerType, SellerType currentType) {
        if (sellerType == null) {
            return currentType;
        }
        try {
            return SellerType.valueOf(sellerType);
        } catch (IllegalArgumentException e) {
            throw new CatalogServiceException(
                    new CatalogServiceError.InvalidEnumValue(SellerType.class.getSimpleName(), sellerType)
            );
        }
    }

    private ListingCondition mapConditionCached(String condition, ListingCondition currentCondition) {
        if (condition == null || condition.isBlank()) {
            return currentCondition;
        }
        try {
            return ListingCondition.valueOf(condition);
        } catch (IllegalArgumentException e) {
            throw new CatalogServiceException(
                    new CatalogServiceError.InvalidEnumValue(ListingCondition.class.getSimpleName(), condition)
            );
        }
    }

    private List<GetProductPayload.Description> mapPayloadDescriptions(List<Description> descriptions) {
        return descriptions.stream()
                .map(d -> new GetProductPayload.Description(
                        d.getId() == null ? null : new CommonId(d.getId().getValue()),
                        d.getName(),
                        d.getTitle(),
                        d.getDescription()
                ))
                .toList();
    }

    private List<GetProductPayload.Media> mapPayloadMedias(List<ProductMedia> medias) {
        return medias.stream()
                .map(media -> new GetProductPayload.Media(
                        media.getId() == null ? null : new CommonId(media.getId().getValue()),
                        media.getType(),
                        media.getPath()
                ))
                .toList();
    }

    private String resolveSlug(UpdateProductCommand command, Product product) {
        boolean slugProvided = command.slug() != null && !command.slug().isBlank();
        boolean nameProvided = command.name() != null && !command.name().isBlank();

        if (!slugProvided && !nameProvided) {
            return product.getSlug();
        }

        String name = nameProvided ? command.name() : product.getName();
        return uniqueSlugResolver.resolve(command.merchantId(), command.slug(), name, product.getId().getValue());
    }
}
