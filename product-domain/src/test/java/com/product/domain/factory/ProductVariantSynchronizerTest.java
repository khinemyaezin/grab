package com.product.domain.factory;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.product.domain.aggregate.product.*;
import com.product.domain.service.ProductVariantSynchronizer;
import com.product.domain.service.SkuGenerator;
import com.product.domain.service.impl.*;
import com.product.domain.valueobject.ProductVariation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.product.domain.factory.ProductTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 *  Size: [Large, Small]},
 *  Color: [Yellow, Red]},
 *  Gender:[Male, Female]}
 *
 * Large Yellow Male
 * Large Yellow Female
 * Large Red Male
 * Large Red Female
 * Small Yellow Male
 * Small Yellow Female
 * Small Red Male
 * Small Red Female
 */
@ExtendWith(MockitoExtension.class)
public class ProductVariantSynchronizerTest {
    @Mock
    private IdGenerator idGenerator;
    @Mock
    private Id id;
    @Mock
    private SkuGenerator skuGenerator;
    private ProductVariantSynchronizer productVariantSynchronizer;

    @BeforeEach
    public void init() {
        var keyFactory = new DefaultVariantKeyGenerator();
        productVariantSynchronizer = new DefaultProductVariantSynchronizer(
                idGenerator,
                skuGenerator,
                new DefaultVariantCombinationService(),
                new FullOptionHardDeleteStrategy(),
                new DefaultVariantInputsFactory(keyFactory),
                new DefaultVariantSorter(keyFactory),
                new DefaultVariantKeyGenerator()
        );
    }

    private VariantType variantType(String id, String name, String... options) {
        VariantType type = new VariantType(id(id), name);
        for (String option : options) {
            type.addOption(new VariantOption(id(id + "-" + option.toLowerCase()), option, type));
        }
        return type;
    }

    private String extractProductVariation(ProductVariant variant) {
        return String.format("%s %s %s",variant.getId(),
                variant.getVariations().stream()
                .map(ProductVariation::getOptionName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining()),
                variant.getStatus());
    }

    @Test
    public void shouldGeneratesCombinationsInSpecifiedOrder_whenRemoveVariantOptionAtFirstOrder() {
        Product FULL_PRODUCT = fullProduct();
        List<VariantType> desiredTypes = List.of(
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female")
        );
        String[] desiredCombination = {
                "1 YellowMale ACTIVE",
                "2 YellowFemale ACTIVE",
                "3 RedMale ACTIVE",
                "4 RedFemale ACTIVE"
        };
        productVariantSynchronizer.synchronize(FULL_PRODUCT, desiredTypes);
        assertThat(FULL_PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }

    @Test
    public void shouldGeneratesCombinationsInSpecifiedOrder_whenRemoveMiddleVariantOption() {
        Product FULL_PRODUCT = fullProduct();
        List<VariantType> desiredTypes = List.of(
                variantType("size", "Size", "Large", "Small"),
                variantType("gender", "Gender", "Male", "Female")
        );
        String[] desiredCombination = {
                "1 LargeMale ACTIVE",
                "2 LargeFemale ACTIVE",
                "5 SmallMale ACTIVE",
                "6 SmallFemale ACTIVE"
        };
        productVariantSynchronizer.synchronize(FULL_PRODUCT, desiredTypes);
        assertThat(FULL_PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }

    @Test
    public void shouldGeneratesCombinationsInSpecifiedOrder_whenRemoveVariantOptionAtLastOrder() {
        Product FULL_PRODUCT = fullProduct();
        List<VariantType> desiredTypes = List.of(
                variantType("size", "Size", "Large", "Small"),
                variantType("color", "Color", "Yellow", "Red")
        );
        String[] desiredCombination = {
                "1 LargeYellow ACTIVE",
                "3 LargeRed ACTIVE",
                "5 SmallYellow ACTIVE",
                "7 SmallRed ACTIVE"
        };
        productVariantSynchronizer.synchronize(FULL_PRODUCT, desiredTypes);
        System.out.println(FULL_PRODUCT);
        assertThat(FULL_PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }

    @Test
    public void shouldGeneratesCombinationsInSpecifiedOrder_whenAddVariantOptionAtLastOrder() {
        when(idGenerator.generateId()).thenReturn(id);
        doAnswer(invocationOnMock -> UUID.randomUUID().toString()).when(skuGenerator).generate(any(SkuGenerator.Context.class));

        Product FULL_PRODUCT = fullProduct();
        List<VariantType> desiredTypes = List.of(
                variantType("size", "Size", "Large", "Small"),
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female"),
                variantType("storage", "Storage", "128", "256")
                );
        String[] desiredCombination = {
                "1 LargeYellowMale128 ACTIVE",
                "id LargeYellowMale256 ACTIVE",
                "2 LargeYellowFemale128 ACTIVE",
                "id LargeYellowFemale256 ACTIVE",
                "3 LargeRedMale128 ACTIVE",
                "id LargeRedMale256 ACTIVE",
                "4 LargeRedFemale128 ACTIVE",
                "id LargeRedFemale256 ACTIVE",
                "5 SmallYellowMale128 ACTIVE",
                "id SmallYellowMale256 ACTIVE",
                "6 SmallYellowFemale128 ACTIVE",
                "id SmallYellowFemale256 ACTIVE",
                "7 SmallRedMale128 ACTIVE",
                "id SmallRedMale256 ACTIVE",
                "8 SmallRedFemale128 ACTIVE",
                "id SmallRedFemale256 ACTIVE"
        };
        productVariantSynchronizer.synchronize(FULL_PRODUCT, desiredTypes);
        assertThat(FULL_PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }

    @Test
    public void shouldGeneratesCombinationsInSpecifiedOrder_whenAddManyVariantOptionAtLastOrder() {
        when(idGenerator.generateId()).thenReturn(id);
        doAnswer(invocationOnMock -> UUID.randomUUID().toString()).when(skuGenerator).generate(any(SkuGenerator.Context.class));

        Product FULL_PRODUCT = fullProduct();
        List<VariantType> desiredTypes = List.of(
                variantType("size", "Size", "Large", "Small"),
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female"),
                variantType("storage", "Storage", "128", "256"),
                variantType("supportlevel", "SupportLevel", "Level1", "Level2")
        );
        String[] desiredCombination = {
                "1 LargeYellowMale128Level1 ACTIVE",
                "id LargeYellowMale128Level2 ACTIVE",
                "id LargeYellowMale256Level1 ACTIVE",
                "id LargeYellowMale256Level2 ACTIVE",
                "2 LargeYellowFemale128Level1 ACTIVE",
                "id LargeYellowFemale128Level2 ACTIVE",
                "id LargeYellowFemale256Level1 ACTIVE",
                "id LargeYellowFemale256Level2 ACTIVE",
                "3 LargeRedMale128Level1 ACTIVE",
                "id LargeRedMale128Level2 ACTIVE",
                "id LargeRedMale256Level1 ACTIVE",
                "id LargeRedMale256Level2 ACTIVE",
                "4 LargeRedFemale128Level1 ACTIVE",
                "id LargeRedFemale128Level2 ACTIVE",
                "id LargeRedFemale256Level1 ACTIVE",
                "id LargeRedFemale256Level2 ACTIVE",
                "5 SmallYellowMale128Level1 ACTIVE",
                "id SmallYellowMale128Level2 ACTIVE",
                "id SmallYellowMale256Level1 ACTIVE",
                "id SmallYellowMale256Level2 ACTIVE",
                "6 SmallYellowFemale128Level1 ACTIVE",
                "id SmallYellowFemale128Level2 ACTIVE",
                "id SmallYellowFemale256Level1 ACTIVE",
                "id SmallYellowFemale256Level2 ACTIVE",
                "7 SmallRedMale128Level1 ACTIVE",
                "id SmallRedMale128Level2 ACTIVE",
                "id SmallRedMale256Level1 ACTIVE",
                "id SmallRedMale256Level2 ACTIVE",
                "8 SmallRedFemale128Level1 ACTIVE",
                "id SmallRedFemale128Level2 ACTIVE",
                "id SmallRedFemale256Level1 ACTIVE",
                "id SmallRedFemale256Level2 ACTIVE"
        };
        productVariantSynchronizer.synchronize(FULL_PRODUCT, desiredTypes);
        assertThat(FULL_PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination);
    }

    @Test
    public void shouldGeneratesCombinationsInSpecifiedOrder_whenAddVariantOptionAtFirstOrder() {
        when(idGenerator.generateId()).thenReturn(id);
        doAnswer(invocationOnMock -> UUID.randomUUID().toString()).when(skuGenerator).generate(any(SkuGenerator.Context.class));

        Product FULL_PRODUCT = fullProduct();
        List<VariantType> desiredTypes = List.of(
                variantType("storage", "Storage", "128", "256"),
                variantType("size", "Size", "Large", "Small"),
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female")
        );
        String[] desiredCombination = {
                "1 128LargeYellowMale ACTIVE",
                "2 128LargeYellowFemale ACTIVE",
                "3 128LargeRedMale ACTIVE",
                "4 128LargeRedFemale ACTIVE",
                "5 128SmallYellowMale ACTIVE",
                "6 128SmallYellowFemale ACTIVE",
                "7 128SmallRedMale ACTIVE",
                "8 128SmallRedFemale ACTIVE",
                "id 256LargeYellowMale ACTIVE",
                "id 256LargeYellowFemale ACTIVE",
                "id 256LargeRedMale ACTIVE",
                "id 256LargeRedFemale ACTIVE",
                "id 256SmallYellowMale ACTIVE",
                "id 256SmallYellowFemale ACTIVE",
                "id 256SmallRedMale ACTIVE",
                "id 256SmallRedFemale ACTIVE"
        };
        productVariantSynchronizer.synchronize(FULL_PRODUCT, desiredTypes);
        assertThat(FULL_PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }

    @Test
    public void shouldGeneratesCombinationsInSpecifiedOrder_whenRemoveById() {
        Product FULL_PRODUCT = fullProduct();
        List<VariantType> desiredTypes = List.of(
                variantType("size", "Size", "Large", "Small"),
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female")
        );
        String[] desiredCombination = {
                "1 LargeYellowMale DELETED",
                "2 LargeYellowFemale ACTIVE",
                "3 LargeRedMale ACTIVE",
                "4 LargeRedFemale ACTIVE",
                "5 SmallYellowMale ACTIVE",
                "6 SmallYellowFemale ACTIVE",
                "7 SmallRedMale ACTIVE",
                "8 SmallRedFemale ACTIVE",
        };
        FULL_PRODUCT.applySoftDeleteVariants(List.of(new CommonId("1")));
        productVariantSynchronizer.synchronize(FULL_PRODUCT, desiredTypes );
        assertThat(FULL_PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }

    @Test
    public void shouldGeneratesCombinationsInSpecifiedOrder_whenAddVariantTypes() {
        when(idGenerator.generateId()).thenReturn(id);
        doAnswer(invocationOnMock -> UUID.randomUUID().toString()).when(skuGenerator).generate(any(SkuGenerator.Context.class));

        Product PRODUCT = emptyVariationProduct();
        List<VariantType> desiredTypes = List.of(
                variantType("size", "Size", "Large", "Small"),
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female")
        );
        String[] desiredCombination = {
                "id LargeYellowMale ACTIVE",
                "id LargeYellowFemale ACTIVE",
                "id LargeRedMale ACTIVE",
                "id LargeRedFemale ACTIVE",
                "id SmallYellowMale ACTIVE",
                "id SmallYellowFemale ACTIVE",
                "id SmallRedMale ACTIVE",
                "id SmallRedFemale ACTIVE",
        };
        PRODUCT.applySoftDeleteVariants(List.of(new CommonId("5")));
        productVariantSynchronizer.synchronize(PRODUCT, desiredTypes );
        assertThat(PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }

    @Test
    public void createProduct_withDeletedStatusAndRemovedSizeType_returnValidCombination() {
        Product FULL_PRODUCT = fullProduct();
        FULL_PRODUCT.findVariantById(new CommonId("1"))
                        .ifPresent(ProductVariant::markAsDeleted);
        List<VariantType> desiredTypes = List.of(
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female")
        );
        productVariantSynchronizer.synchronize(FULL_PRODUCT, desiredTypes );
        String[] desiredCombination = new String[]{
                "1 YellowMale DELETED",
                "2 YellowFemale ACTIVE",
                "3 RedMale ACTIVE",
                "4 RedFemale ACTIVE"
        };
        assertThat(FULL_PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }

    @Test
    public void createProduct_withDeletedStatusAndRemovedLargeOption_returnValidCombination() {
        Product FULL_PRODUCT = fullProduct();
        FULL_PRODUCT.findVariantById(new CommonId("5"))
                .ifPresent(ProductVariant::markAsDeleted);
        List<VariantType> desiredTypes = List.of(
                variantType("size", "Size", "Small"),
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female")
        );
        String[] desiredCombination = {
                "5 SmallYellowMale DELETED",
                "6 SmallYellowFemale ACTIVE",
                "7 SmallRedMale ACTIVE",
                "8 SmallRedFemale ACTIVE",
        };
        productVariantSynchronizer.synchronize(FULL_PRODUCT, desiredTypes );
        assertThat(FULL_PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }

    @Test
    public void createProduct_withDeletedStatusLargeOptions_returnWithoutLarge() {
        Product FULL_PRODUCT = fullProduct();
        FULL_PRODUCT.findVariantById(new CommonId("1"))
                .ifPresent(ProductVariant::markAsDeleted);
        FULL_PRODUCT.findVariantById(new CommonId("2"))
                .ifPresent(ProductVariant::markAsDeleted);
        FULL_PRODUCT.findVariantById(new CommonId("3"))
                .ifPresent(ProductVariant::markAsDeleted);
        FULL_PRODUCT.findVariantById(new CommonId("4"))
                .ifPresent(ProductVariant::markAsDeleted);
        List<VariantType> desiredTypes = List.of(
                variantType("size", "Size", "Large", "Small"),
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female")
        );
        String[] desiredCombination = {
                "5 SmallYellowMale ACTIVE",
                "6 SmallYellowFemale ACTIVE",
                "7 SmallRedMale ACTIVE",
                "8 SmallRedFemale ACTIVE",
        };
        productVariantSynchronizer.synchronize(FULL_PRODUCT, desiredTypes);
        assertThat(FULL_PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }

    @Test
    public void createProduct_withDeletedStatusLargeOptions_returnWithoutLarge1() {
        Product FULL_PRODUCT = fullProduct();
        List<VariantType> desiredTypes = List.of(
                variantType("size", "Size", "Large", "Small"),
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female")
        );
        String[] desiredCombination = {
                "1 LargeYellowMale ACTIVE",
                "2 LargeYellowFemale ACTIVE",
                "5 SmallYellowMale ACTIVE",
                "6 SmallYellowFemale ACTIVE"
        };
        FULL_PRODUCT.applySoftDeleteVariants(List.of(new CommonId("3"),
                new CommonId("4"),
                new CommonId("7"),
                new CommonId("8")
        ));
        productVariantSynchronizer.synchronize(FULL_PRODUCT, desiredTypes);
        assertThat(FULL_PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }


}
