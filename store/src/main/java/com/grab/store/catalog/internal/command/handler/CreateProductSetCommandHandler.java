package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.service.SkuGenerator;
import com.catalog.domain.service.MatrixCombinationService;
import com.catalog.domain.service.MatrixKeyGenerator;
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
import com.grab.store.catalog.internal.exception.CatalogCommandHandlerError;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.util.CatalogPolicyValidator;
import com.grab.store.catalog.internal.util.StandaloneVariationFactory;
import com.grab.store.catalog.internal.util.UniqueSlugResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    private final MatrixCombinationService matrixCombinationService;
    private final MatrixKeyGenerator matrixKeyGenerator;

    @Override
    @CatalogTransactional
    public CreateProductSetResult handle(CreateProductSetCommand command) {
        log.debug("Handling SaveProductCommand for product: {}", command.product().name());

        Category category = findCategoryOrElseThrow(command.product().categoryId());
        CatalogPolicyValidator.validateCategoryPolicy(category);

        Product product = createProductDraft(command.product());
        List<ProductVariant> variants = buildVariants(command.product().name(), command.product().variants(), command.variantTypes());
        addVariants(product, variants);

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
                convertToCondition(product.condition()),
                slug,
                List.of(),
                List.of()
        );
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
        String sku = StringUtils.hasText(inputVariant.sku())
                ? inputVariant.sku()
                : generateSku(productName, variations);

        return ProductVariant.create(
                variantId,
                sku,
                variations
        );
    }

    public ProductVariant fallbackToStandaloneVariant(String productName, List<CreateProductSetCommand.Variant> overrideVariants) {
        CreateProductSetCommand.Variant defVariant = overrideVariants.stream().findFirst().orElse(null);
        return createStandaloneVariant(productName, defVariant);
    }

    public ProductVariant createStandaloneVariant(String productName, CreateProductSetCommand.Variant variant) {
        List<ProductVariation> variations = StandaloneVariationFactory.create(idGenerator);
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

    private void addVariants(Product product, List<ProductVariant> variants) {
        for (ProductVariant variant : variants) {
            if (!product.addVariant(variant)) {
                throw new CatalogServiceException(
                        new CatalogServiceError.VariantAddFailed(variant.getSku())
                );
            }
        }
    }
}
