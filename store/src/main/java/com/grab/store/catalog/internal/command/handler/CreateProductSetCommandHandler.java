package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.service.SkuGenerator;
import com.catalog.domain.service.VariantCombinationService;
import com.catalog.domain.service.VariationCombinationManager;
import com.catalog.domain.service.VariationKeyGenerator;
import com.catalog.domain.service.dto.VariantOptionSelection;
import com.catalog.domain.service.dto.VariantTypeSelection;
import com.catalog.domain.valueobject.ListingCondition;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.domain.valueobject.SellerType;
import com.catalog.domain.valueobject.VariantCombination;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.command.CreateProductSetCommand;
import com.grab.store.catalog.internal.command.CreateProductSetResult;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.util.CatalogPolicyValidator;
import com.grab.store.catalog.internal.util.StandaloneVariantDefaults;
import com.grab.store.catalog.internal.util.UniqueSlugResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CreateProductSetCommandHandler implements CommandHandler<CreateProductSetCommand, CreateProductSetResult> {

    private static final Logger log = Loggers.getLogger(CreateProductSetCommandHandler.class);
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UniqueSlugResolver uniqueSlugResolver;
    private final IdGenerator idGenerator;
    private final SkuGenerator skuGenerator;
    private final VariantCombinationService variantCombinationService;
    private final VariationCombinationManager variationCombinationManager;
    private final VariationKeyGenerator variationKeyGenerator;

    @Override
    @CatalogTransactional
    public CreateProductSetResult handle(CreateProductSetCommand command) {
        log.debug("Handling SaveProductCommand for product: {}", command.product().name());

        Category category = findCategoryOrElseThrow(command.product().categoryId());
        CatalogPolicyValidator.validateCategoryPolicy(category);

        Product product = createProductDraft(command.product());
        List<ProductVariant> variants = materializeVariants(command);

        for (ProductVariant variant : variants) {
            if (!product.addVariant(variant)) {
                throw new CatalogServiceException(
                        new CatalogServiceError.VariantAddFailed(variant.getSku())
                );
            }
        }
        productRepository.save(product);

        log.info("Product saved successfully with {} variants", product.getVariants().size());

        return new CreateProductSetResult(product.getId().getValue());
    }

    @Override
    public Class<CreateProductSetCommand> getCommandType() {
        return CreateProductSetCommand.class;
    }

    private List<VariantTypeSelection> convertToVariantTypeSelectionList(List<CreateProductSetCommand.VariantType> variantTypes) {
        if (variantTypes == null || variantTypes.isEmpty()) {
            return List.of();
        }
        return variantTypes.stream()
                .map(variantType -> new VariantTypeSelection(
                        idGenerator.convertIdFrom(variantType.typeId()),
                        variantType.options().stream()
                                .map(option -> new VariantOptionSelection(
                                        idGenerator.convertIdFrom(option.optionId()),
                                        idGenerator.convertIdFrom(variantType.typeId())))
                                .toList()
                ))
                .toList();
    }

    private Product createProductDraft(CreateProductSetCommand.Product product) {
        Id productId = idGenerator.generateId();
        String slug = uniqueSlugResolver.resolve(product.slug(), product.name(), null);
        return Product.create(
                productId,
                product.name(),
                product.categoryId(),
                product.sellerId(),
                convertToSellerType(product.sellerType()),
                convertToCondition(product.condition()),
                Boolean.TRUE.equals(product.offerEligible()),
                Boolean.TRUE.equals(product.featured()),
                slug,
                convertToDescriptions(product.descriptions()),
                convertToProductMedia(product.medias())
        );
    }

    private List<Description> convertToDescriptions(List<CreateProductSetCommand.Description> descriptions) {
        if (descriptions == null) {
            return List.of();
        }
        return descriptions.stream()
                .map(d -> new Description(null, d.name(), d.title(), d.description()))
                .toList();
    }

    private List<ProductMedia> convertToProductMedia(List<CreateProductSetCommand.Media> medias) {
        if (medias == null) {
            return List.of();
        }
        return medias.stream()
                .map(media -> new ProductMedia(null, media.type(), media.path()))
                .toList();
    }

    private SellerType convertToSellerType(String sellerType) {
        return sellerType == null ? null : SellerType.valueOf(sellerType);
    }

    private ListingCondition convertToCondition(String condition) {
        return condition == null || condition.isBlank() ? null : ListingCondition.valueOf(condition);
    }

    private Category findCategoryOrElseThrow(Id categoryId) {
        return categoryRepository.find(categoryId).orElseThrow(() -> new CatalogServiceException(
                new CatalogServiceError.CategoryNotFound(categoryId.getValue())
        ));
    }

    private List<ProductVariant> materializeVariants(CreateProductSetCommand command) {
        if (command.variantTypes() == null || command.variantTypes().isEmpty()) {
            return List.of(fallbackToStandaloneVariant(command));
        }
        List<VariantTypeSelection> variantTypes = convertToVariantTypeSelectionList(command.variantTypes());
        List<List<VariantOptionSelection>> combinations = variantCombinationService.generateCombinations(variantTypes);
        List<VariantCombination> variantCombinations = convertToVariantCombinations(combinations);

        List<VariationCombinationManager.VariantCombinationResult> syncResults =
                variationCombinationManager.syncCombinations(List.of(), variantCombinations);

        Map<String, CreateProductSetCommand.Variant> variantByMatrixKey =
                createInputVariantMapByMatrixKey(command.product().variants());

        List<ProductVariant> resultVariants = new ArrayList<>(syncResults.size());
        for(VariationCombinationManager.VariantCombinationResult  combinationResult : syncResults) {
            List<ProductVariation> variations = combinationResult.variantCombination().variations();
            String key = variationKeyGenerator.generateVariationKey(variations);
            CreateProductSetCommand.Variant variantInput = variantByMatrixKey.get(key);
            if(variantInput == null) { continue;}

            ProductVariant variant = createVariant(command.product().name(), variantInput, variations);
            resultVariants.add(variant);
        }

        return resultVariants;
    }

    private void validateInputsBelongToMatrix(Set<String> inputKeys, Set<String> desiredKeys) {
        for (String inputKey : inputKeys) {
            if (!desiredKeys.contains(inputKey)) {
                throw new IllegalArgumentException(
                        "Input combination does not belong to calculated matrix: " + inputKey
                );
            }
        }
    }

    private Map<String, CreateProductSetCommand.Variant> createInputVariantMapByMatrixKey(List<CreateProductSetCommand.Variant> variants) {
        if (variants == null) {
            return Map.of();
        }

        return variants.stream()
                .collect(Collectors.toMap(
                        CreateProductSetCommand.Variant::matrixKey,
                        v -> v));
    }

    private ProductVariant createVariant(String productName, CreateProductSetCommand.Variant inputVariant,  List<ProductVariation> variations) {
        Id variantId = idGenerator.generateId();
        String sku = StringUtils.hasText(inputVariant.sku())
                ? inputVariant.sku()
                : generateSku(productName, variations);

        return ProductVariant.create(
                variantId,
                sku,
                variations
        );
    }

    private List<VariantCombination> convertToVariantCombinations(List<List<VariantOptionSelection>> combinations) {
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

    public ProductVariant fallbackToStandaloneVariant(CreateProductSetCommand command) {
        CreateProductSetCommand.Variant defVariant = command.product().variants().stream().findFirst().orElse(null);
        return createStandaloneVariant(command.product().name(), defVariant);
    }

    public ProductVariant createStandaloneVariant(String productName, CreateProductSetCommand.Variant variant) {
        List<ProductVariation> variations = StandaloneVariantDefaults.defaultVariations(idGenerator);
        String sku = variant != null && StringUtils.hasText(variant.sku()) ? variant.sku() : generateSku(productName, variations);

        return ProductVariant.create(
                idGenerator.generateId(),
                sku,
                variations
        );
    }

    private String generateSku(String productName, List<ProductVariation> variations) {
        return skuGenerator.generate(new SkuGenerator.Context(productName, variations));
    }
}
