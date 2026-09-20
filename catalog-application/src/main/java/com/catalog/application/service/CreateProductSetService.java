package com.catalog.application.service;

import com.catalog.application.port.inbound.CreateProductSetUseCase;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.port.outbound.CategoryRepository;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.domain.service.SkuGenerator;
import com.catalog.domain.service.MatrixCombinationService;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.service.dto.VariantOptionSelection;
import com.catalog.domain.service.dto.VariantTypeSelection;
import com.catalog.domain.valueobject.ListingCondition;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.domain.valueobject.VariantCombination;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.catalog.application.model.write.CreateProductSetCommand;
import com.catalog.application.model.write.CreateProductSetResult;
import com.catalog.application.exception.CatalogCommandHandlerError;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@lombok.RequiredArgsConstructor
public class CreateProductSetService implements CreateProductSetUseCase {
    private static final Logger log = Loggers.getLogger(CreateProductSetService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UniqueSlugResolver uniqueSlugResolver;
    private final IdGenerator idGenerator;
    private final SkuGenerator skuGenerator;
    private final MatrixCombinationService matrixCombinationService;
    private final MatrixKeyGenerator matrixKeyGenerator;

        public CreateProductSetResult execute(CreateProductSetCommand command) {
        log.debug("Handling SaveProductCommand for product: {}", command.product().name());

        Category category = findCategoryOrElseThrow(command.product().categoryId());
        CatalogPolicyValidator.validateCategoryPolicy(category);

        Product product = createProductDraft(command.merchantId(), command.product());
        List<ProductVariant> variants = buildVariants(command.product().name(), command.product().variants(), command.variantTypes());
        validateSkuAvailability(command.merchantId(), variants);
        addVariants(product, variants);

        productRepository.save(product);

        log.info("Product saved successfully with {} variants", product.getVariants().size());

        List<CreateProductSetResult.VariantRef> variantRefs = product.getVariants().stream()
                .map(variant -> new CreateProductSetResult.VariantRef(
                        variant.getId().getValue(),
                        variant.getSku()
                ))
                .toList();
        return new CreateProductSetResult(
                product.getId().getValue(),
                variantRefs,
                product.getStatus() == null ? null : product.getStatus().name()
        );
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

    private Product createProductDraft(Id commandMerchantId, CreateProductSetCommand.Product product) {
        Id productId = idGenerator.generateId();
        String slug = uniqueSlugResolver.resolve(commandMerchantId, product.slug(), product.name(), null);
        return Product.create(
                productId,
                commandMerchantId,
                product.name(),
                product.categoryId(),
                convertToCondition(product.condition()),
                slug,
                List.of(),
                List.of()
        );
    }

    private ListingCondition convertToCondition(String condition) {
        return condition == null || condition.isBlank() ? null : ListingCondition.valueOf(condition);
    }

    private Category findCategoryOrElseThrow(Id categoryId) {
        return categoryRepository.find(categoryId).orElseThrow(() -> new CatalogServiceException(
                new CatalogServiceError.CategoryNotFound(categoryId.getValue())
        ));
    }

    private List<ProductVariant> buildVariants(String productName,
                                               List<CreateProductSetCommand.Variant> overrideVariants,
                                               List<CreateProductSetCommand.VariantType> variantTypes) {
        if (variantTypes == null || variantTypes.isEmpty()) {
            log.info("No variant types specified");
            return List.of(fallbackToStandaloneVariant(productName, overrideVariants));
        }

        List<VariantTypeSelection> variantTypeSelections = convertToVariantTypeSelectionList(variantTypes);
        List<VariantCombination> combinations = generateVariantCombinations(variantTypeSelections);
        Map<String, VariantCombination> combinationMapByMatrixKey = buildCombinationMap(combinations);
        return buildTargetVariants(productName, overrideVariants, combinationMapByMatrixKey);
    }

    private List<ProductVariant> buildTargetVariants(
            String productName,
            List<CreateProductSetCommand.Variant> overrides,
            Map<String, VariantCombination> combinationResultMap) {

        List<ProductVariant> resultVariants = new ArrayList<>(overrides.size());

        for (CreateProductSetCommand.Variant overrideVariant : overrides) {
            ProductVariant targetVariant = resolveOrCreateVariant(productName, overrideVariant, combinationResultMap);
            resultVariants.add(targetVariant);
        }

        return resultVariants;
    }

    private ProductVariant resolveOrCreateVariant(
            String productName,
            CreateProductSetCommand.Variant overrideVariant,
            Map<String, VariantCombination> combinationResultMap) {

        String overrideMatrixKey = matrixKeyGenerator.generateKey(convertToProductVariations(overrideVariant.variations()));
        VariantCombination combination = combinationResultMap.get(overrideMatrixKey);

        if (combination == null) {
            log.error("Override variant with matrixKey={} does not match any generated combination", overrideMatrixKey);
            throw new CatalogServiceException(
                    new CatalogCommandHandlerError.VariantOverrideCombinationNotFound(overrideMatrixKey)
            );
        }

        return createVariant(
                productName,
                overrideVariant,
                combination.variations()
        );
    }

    private List<ProductVariation> convertToProductVariations(List<CreateProductSetCommand.Variation> variations){
        return variations.stream()
                .map(variation -> new ProductVariation(variation.optionId(), variation.typeId()))
                .toList();
    }

    private List<VariantCombination> generateVariantCombinations(List<VariantTypeSelection> variantTypes) {
        List<List<VariantOptionSelection>> combinations = matrixCombinationService.generateMatrixCombination(variantTypes);
        return mapVariantCombinations(combinations);
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

    private Map<String, VariantCombination> buildCombinationMap(List<VariantCombination> variantCombinations) {
        if (variantCombinations == null) {
            return Map.of();
        }

        return variantCombinations.stream()
                .collect(Collectors.toMap(
                        combination-> matrixKeyGenerator.generateKey(combination.variations()),
                        Function.identity()));
    }

    private ProductVariant createVariant(String productName, CreateProductSetCommand.Variant inputVariant,  List<ProductVariation> variations) {
        Id variantId = idGenerator.generateId();
        String sku = hasText(inputVariant.sku())
                ? inputVariant.sku()
                : generateSku(productName, variations);

        return ProductVariant.create(
                variantId,
                sku,
                variations,
                Boolean.TRUE.equals(inputVariant.manageInventory())
        );
    }

    public ProductVariant fallbackToStandaloneVariant(String productName, List<CreateProductSetCommand.Variant> overrideVariants) {
        CreateProductSetCommand.Variant defVariant = overrideVariants.stream().findFirst().orElse(null);
        return createStandaloneVariant(productName, defVariant);
    }

    public ProductVariant createStandaloneVariant(String productName, CreateProductSetCommand.Variant variant) {
        List<ProductVariation> variations = StandaloneVariationFactory.create(idGenerator);
        String sku = variant != null && hasText(variant.sku()) ? variant.sku() : generateSku(productName, variations);

        return ProductVariant.create(
                idGenerator.generateId(),
                sku,
                variations,
                Boolean.TRUE.equals(variant == null ? null : variant.manageInventory())
        );
    }

    private String generateSku(String productName, List<ProductVariation> variations) {
        return skuGenerator.generate(new SkuGenerator.Context(productName, variations));
    }

    private void addVariants(Product product, List<ProductVariant> variants) {
        for (ProductVariant variant : variants) {
            product.addVariant(variant);
        }
    }

    private void validateSkuAvailability(Id merchantId, List<ProductVariant> variants) {
        for (ProductVariant variant : variants) {
            if (productRepository.isSkuTaken(merchantId, variant.getSku(), null)) {
                throw new CatalogServiceException(
                        new CatalogServiceError.SkuAlreadyExists(variant.getSku())
                );
            }
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
