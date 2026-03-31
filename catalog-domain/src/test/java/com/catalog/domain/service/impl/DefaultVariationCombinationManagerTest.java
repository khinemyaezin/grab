package com.catalog.domain.service.impl;

import com.catalog.domain.service.dto.ProductVariantSelection;
import com.grab.framework.id.Id;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.catalog.domain.service.VariationCombinationManager;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.domain.valueobject.VariantCombination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DefaultVariationCombinationManagerTest {
    private VariationCombinationManager variationCombinationManager;

    /**
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
    private List<ProductVariantSelection> getProductVariation() {
        return List.of(
                variant("1", "SKU-LYM",
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender")),

                variant("2",  "SKU-LYF",
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender")),

                variant("3",  "SKU-LRM",
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender")),

                variant("4",  "SKU-LRF",
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender")),

                variant("5",  "SKU-SYM",
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender")),

                variant("6",  "SKU-SYF",
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender")),

                variant("7",  "SKU-SRM",
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-male"), "Male", new CommonId("gender"), "Gender")),

                variant("8",  "SKU-SRF",
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("gender-female"), "Female", new CommonId("gender"), "Gender"))
        );
    }

    protected static ProductVariantSelection variant(String id, String sku, ProductVariation... variations) {
        return new ProductVariantSelection(new CommonId(id), List.of(variations), ProductVariantStatus.ACTIVE);
    }

    protected static ProductVariation variation(Id optionId, String optionName,Id typeId, String typeName) {
        return new ProductVariation(optionName, optionId, typeName, typeId);
    }

    protected static VariantCombination combination(ProductVariation... variations) {
        return new VariantCombination(List.of(variations));
    }

    private String extractTemplateFrom(VariationCombinationManager.VariantCombinationResult variantCombinationResult) {
        return String.format("%s %s %s",
                variantCombinationResult.variantCombination().variations().stream()
                        .map( variation -> variation.getOptionId().getValue())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining()),
                variantCombinationResult.matchedVariant() == null ? "ACTIVE" : variantCombinationResult.matchedVariant().status(),
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

    @BeforeEach
    public void init() {
        var keyFactory = new DefaultVariationKeyGenerator(new ProductVariationComparator());
        variationCombinationManager = new DefaultVariationCombinationManager(keyFactory);
    }

    @Test
    public void syncCombinations_addVariantType_returnNewVariants() {
        List<ProductVariantSelection> existingVariants = List.of();
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
                "color-yellowgender-male ACTIVE NEW",
                "color-yellowgender-female ACTIVE NEW",
                "color-redgender-male ACTIVE NEW",
                "color-redgender-female ACTIVE NEW"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_removeVariantTypeAtFirst_returnCombinationInOrder() {
        List<ProductVariantSelection> existingVariants = getProductVariation();
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
                "color-yellowgender-male ACTIVE EXTENDED",
                "color-yellowgender-female ACTIVE EXTENDED",
                "color-redgender-male ACTIVE EXTENDED",
                "color-redgender-female ACTIVE EXTENDED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_removeVariantTypeAtMiddle_returnCombinationInOrder() {
        List<ProductVariantSelection> existingVariants = getProductVariation();
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
                "size-largegender-male ACTIVE EXTENDED",
                "size-largegender-female ACTIVE EXTENDED",
                "size-smallgender-male ACTIVE EXTENDED",
                "size-smallgender-female ACTIVE EXTENDED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_removeVariantTypeAtLast_returnCombinationInOrder() {
        List<ProductVariantSelection> existingVariants = getProductVariation();
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
                "size-largecolor-yellow ACTIVE EXTENDED",
                "size-largecolor-red ACTIVE EXTENDED",
                "size-smallcolor-yellow ACTIVE EXTENDED",
                "size-smallcolor-red ACTIVE EXTENDED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_addVariantTypeAtLast_returnCombinationsInOrder_() {
        List<ProductVariantSelection> existingVariants = getProductVariation();
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
                "size-largecolor-yellowgender-malestorage-128 ACTIVE EXTENDED",
                "size-largecolor-yellowgender-malestorage-256 ACTIVE EXTENDED",
                "size-largecolor-yellowgender-femalestorage-128 ACTIVE EXTENDED",
                "size-largecolor-yellowgender-femalestorage-256 ACTIVE EXTENDED",
                "size-largecolor-redgender-malestorage-128 ACTIVE EXTENDED",
                "size-largecolor-redgender-malestorage-256 ACTIVE EXTENDED",
                "size-largecolor-redgender-femalestorage-128 ACTIVE EXTENDED",
                "size-largecolor-redgender-femalestorage-256 ACTIVE EXTENDED",
                "size-smallcolor-yellowgender-malestorage-128 ACTIVE EXTENDED",
                "size-smallcolor-yellowgender-malestorage-256 ACTIVE EXTENDED",
                "size-smallcolor-yellowgender-femalestorage-128 ACTIVE EXTENDED",
                "size-smallcolor-yellowgender-femalestorage-256 ACTIVE EXTENDED",
                "size-smallcolor-redgender-malestorage-128 ACTIVE EXTENDED",
                "size-smallcolor-redgender-malestorage-256 ACTIVE EXTENDED",
                "size-smallcolor-redgender-femalestorage-128 ACTIVE EXTENDED",
                "size-smallcolor-redgender-femalestorage-256 ACTIVE EXTENDED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_addVariantOption_returnCombinationsInOrder() {
        List<ProductVariantSelection> existingVariants = List.of(
                variant("1",  "SKU-LYM",
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color")),

                variant("3",  "SKU-LRM",
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color")),

                variant("5",  "SKU-SYM",
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color")),

                variant("7",  "SKU-SRM",
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"))
        );
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color")),

                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color")),

                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-blue"), "Blue", new CommonId("color"), "Color")),

                combination(
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color")),

               combination(
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color")),
                combination(
                        variation(new CommonId("size-small"), "Small", new CommonId("size"), "Size"),
                        variation(new CommonId("color-blue"), "Blue", new CommonId("color"), "Color"))
        );

        String[] desiredCombination = {
                "size-largecolor-yellow ACTIVE UNCHANGED",
                "size-largecolor-red ACTIVE UNCHANGED",
                "size-largecolor-blue ACTIVE NEW",
                "size-smallcolor-yellow ACTIVE UNCHANGED",
                "size-smallcolor-red ACTIVE UNCHANGED",
                "size-smallcolor-blue ACTIVE NEW"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_removeVariantOption_returnCombinationsInOrder() {
        List<ProductVariantSelection> existingVariants = List.of(
                variant("1",  "SKU-LYM",
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color")),

                variant("3",  "SKU-LRM",
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"))
        );
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"))
        );

        String[] desiredCombination = {
                "size-largecolor-yellow ACTIVE UNCHANGED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_changeVariantOptionOrder_returnCombinationsInOrder() {
        List<ProductVariantSelection> existingVariants = List.of(
                variant("1",  "SKU-LYM",
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                        variation(new CommonId("size-large"), "size-large", new CommonId("size"), "Size")),


                variant("3",  "SKU-LRM",
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"),
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"))
        );
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-yellow"), "Yellow", new CommonId("color"), "Color")),
                combination(
                        variation(new CommonId("size-large"), "Large", new CommonId("size"), "Size"),
                        variation(new CommonId("color-red"), "Red", new CommonId("color"), "Color"))
        );

        String[] desiredCombination = {
                "size-largecolor-yellow ACTIVE UNCHANGED",
                "size-largecolor-red ACTIVE UNCHANGED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }


}
