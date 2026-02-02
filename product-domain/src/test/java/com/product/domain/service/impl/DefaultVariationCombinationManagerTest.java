package com.product.domain.service.impl;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.aggregate.product.VariantType;
import com.product.domain.service.VariationCombinationManager;
import com.product.domain.valueobject.ProductVariation;
import com.product.domain.valueobject.VariantCombination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.product.domain.factory.ProductTestData.fullProduct;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DefaultVariationCombinationManagerTest {
    private static final Id PRODUCT_ID = new CommonId("prod-1");
    private VariationCombinationManager variationCombinationManager;

    @BeforeEach
    public void init() {
        var keyFactory = new DefaultVariationKeyGenerator();
        variationCombinationManager = new DefaultVariationCombinationManager(keyFactory);
    }

    @Test
    public void syncCombinations_addVariantType_returnNewVariants() {
        List<ProductVariant> existingVariants = List.of();
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender")),
                combination(
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender")),
                combination(
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender")),
                combination(
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender"))
        );
        String[] desiredCombination = {
                "YellowMale ACTIVE NEW",
                "YellowFemale ACTIVE NEW",
                "RedMale ACTIVE NEW",
                "RedFemale ACTIVE NEW"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_removeVariantTypeAtFirst_returnCombinationInOrder() {
        List<ProductVariant> existingVariants = extractProductVariation(PRODUCT_ID);
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender")),
                combination(
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender")),
                combination(
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender")),
                combination(
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender"))
        );
        String[] desiredCombination = {
                "YellowMale ACTIVE EXTENDED",
                "YellowFemale ACTIVE EXTENDED",
                "RedMale ACTIVE EXTENDED",
                "RedFemale ACTIVE EXTENDED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_removeVariantTypeAtMiddle_returnCombinationInOrder() {
        List<ProductVariant> existingVariants = extractProductVariation(PRODUCT_ID);
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender")),

                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender")),

                combination(
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender")),

                combination(
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender"))
        );
        String[] desiredCombination = {
                "LargeMale ACTIVE EXTENDED",
                "LargeFemale ACTIVE EXTENDED",
                "SmallMale ACTIVE EXTENDED",
                "SmallFemale ACTIVE EXTENDED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_removeVariantTypeAtLast_returnCombinationInOrder() {
        List<ProductVariant> existingVariants = extractProductVariation(PRODUCT_ID);
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color")),

                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color")),

                combination(
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color")),

                combination(
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"))
        );
        String[] desiredCombination = {
                "LargeYellow ACTIVE EXTENDED",
                "LargeRed ACTIVE EXTENDED",
                "SmallYellow ACTIVE EXTENDED",
                "SmallRed ACTIVE EXTENDED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_addVariantTypeAtLast_returnCombinationsInOrder_() {
        List<ProductVariant> existingVariants = extractProductVariation(PRODUCT_ID);
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-128"), "128", new CommonId("storage"), "Storage")),

                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-256"), "256", new CommonId("storage"), "Storage")),

                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-128"), "128", new CommonId("storage"), "Storage")),

                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-256"), "256", new CommonId("storage"), "Storage")),

                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-128"), "128", new CommonId("storage"), "Storage")),

                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-256"), "256", new CommonId("storage"), "Storage")),

                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-128"), "128", new CommonId("storage"), "Storage")),

                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-256"), "256", new CommonId("storage"), "Storage")),

                combination(
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-128"), "128", new CommonId("storage"), "Storage")),

                combination(
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-256"), "256", new CommonId("storage"), "Storage")),

                combination(
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-128"), "128", new CommonId("storage"), "Storage")),
                combination(
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-256"), "256", new CommonId("storage"), "Storage")),

                combination(
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-128"), "128", new CommonId("storage"), "Storage")),

                combination(
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-256"), "256", new CommonId("storage"), "Storage")),

                combination(
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-128"), "128", new CommonId("storage"), "Storage")),
                combination(
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender"),
                        variation(new CommonId("storage-256"), "256", new CommonId("storage"), "Storage"))
        );
        String[] desiredCombination = {
                "LargeYellowMale128 ACTIVE EXTENDED",
                "LargeYellowMale256 ACTIVE EXTENDED",
                "LargeYellowFemale128 ACTIVE EXTENDED",
                "LargeYellowFemale256 ACTIVE EXTENDED",
                "LargeRedMale128 ACTIVE EXTENDED",
                "LargeRedMale256 ACTIVE EXTENDED",
                "LargeRedFemale128 ACTIVE EXTENDED",
                "LargeRedFemale256 ACTIVE EXTENDED",
                "SmallYellowMale128 ACTIVE EXTENDED",
                "SmallYellowMale256 ACTIVE EXTENDED",
                "SmallYellowFemale128 ACTIVE EXTENDED",
                "SmallYellowFemale256 ACTIVE EXTENDED",
                "SmallRedMale128 ACTIVE EXTENDED",
                "SmallRedMale256 ACTIVE EXTENDED",
                "SmallRedFemale128 ACTIVE EXTENDED",
                "SmallRedFemale256 ACTIVE EXTENDED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_addVariantOption_returnCombinationsInOrder() {

    }
    /*
    productId    | sku      | size    | color   | gender
    -------------|----------|---------|---------|----------
    product-1    | SKU-LYM  | Large   | Yellow  | Male
    product-1    | SKU-LYF  | Large   | Yellow  | Female
    product-1    | SKU-LRM  | Large   | Red     | Male
    product-1    | SKU-LRF  | Large   | Red     | Female
    product-1    | SKU-SYM  | Small   | Yellow  | Male
    product-1    | SKU-SYF  | Small   | Yellow  | Female
    product-1    | SKU-SRM  | Small   | Red     | Male
    product-1    | SKU-SRF  | Small   | Red     | Female
    */
    private List<ProductVariant> extractProductVariation(Id productId) {
        return List.of(
                variant("1", productId, "SKU-LYM",
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender")),

                variant("2", productId, "SKU-LYF",
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender")),

                variant("3", productId, "SKU-LRM",
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender")),

                variant("4", productId, "SKU-LRF",
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender")),

                variant("5", productId, "SKU-SYM",
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender")),

                variant("6", productId, "SKU-SYF",
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender")),

                variant("7", productId, "SKU-SRM",
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender")),

                variant("8", productId, "SKU-SRF",
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender"))
        );
    }

    protected static ProductVariant variant(String variantId, Id productId, String sku, ProductVariation... variations) {
        return new ProductVariant(new CommonId(variantId), productId, sku, List.of(variations));
    }

    protected static ProductVariation variation(Id optionId, String optionName,Id typeId, String typeName) {
        return new ProductVariation(optionName, optionId, typeName, typeId);
    }

    protected static VariantCombination combination(ProductVariation... variations) {
        return new VariantCombination(List.of(variations));
    }

    private String extractTemplateFrom(VariationCombinationManager.VariantCombinationResult variantCombinationResult) {
        return String.format("%s %s %s",
                variantCombinationResult.variantCombination().getVariations().stream()
                        .map(ProductVariation::getOptionName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining()),
                variantCombinationResult.matchedVariant() == null ? "ACTIVE" : variantCombinationResult.matchedVariant().getStatus(),
                variantCombinationResult.matchedType());
    }

    record CommonId(String id) implements Id {
        @Override
        public String getValue() {
            return this.id;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CommonId(String id1))) return false;
            return Objects.equals(id, id1);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }

        @Override
        public String toString() {
            return id;
        }
    }

}