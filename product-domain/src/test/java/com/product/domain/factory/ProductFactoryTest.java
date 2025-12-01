package com.product.domain.factory;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.product.domain.aggregate.product.*;
import com.product.domain.factory.impl.ProductFactoryImpl;
import com.product.domain.service.SkuGenerator;
import com.product.domain.service.impl.VariantCombinationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.product.domain.factory.ProductTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
public class ProductFactoryTest{
    @Mock
    private IdGenerator idGenerator;
    @Mock
    private Id id;
    @Mock
    private SkuGenerator skuGenerator;
    private ProductFactory productFactory;

    @BeforeEach
    public void init() {
        productFactory = new ProductFactoryImpl(new VariantCombinationServiceImpl(), idGenerator, skuGenerator);
    }

    private VariantType variantType(String id, String name, String... options) {
        VariantType type = new VariantType(id(id), name);
        for (String option : options) {
            type.addOption(new VariantOption(id(name + "-" + option), option, type));
        }
        return type;
    }

    private String extractProductVariation(ProductVariant variant) {
        return variant.getId() + " " + variant.getVariations().stream()
                .map(ProductVariation::getOptionName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
    }

    @Test
    public void shouldGeneratesCombinationsInSpecifiedOrder_whenRemoveVariantOptionAtFirstOrder() {
        Product FULL_PRODUCT = fullProduct();
        List<VariantType> desiredTypes = List.of(
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female")
        );
        String[] desiredCombination = {
                "1 YellowMale",
                "2 YellowFemale",
                "3 RedMale",
                "4 RedFemale"
        };
        productFactory.create(FULL_PRODUCT, desiredTypes, List.of());
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
                "1 LargeMale",
                "2 LargeFemale",
                "5 SmallMale",
                "6 SmallFemale"
        };
        productFactory.create(FULL_PRODUCT, desiredTypes, List.of());
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
                "1 LargeYellow",
                "3 LargeRed",
                "5 SmallYellow",
                "7 SmallRed"
        };
        productFactory.create(FULL_PRODUCT, desiredTypes, List.of());
        assertThat(FULL_PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }

    @Test
    public void shouldGeneratesCombinationsInSpecifiedOrder_whenAddVariantOptionAtLastOrder() {
        when(idGenerator.generateId()).thenReturn(id);
        doAnswer(invocationOnMock -> {
            SkuGenerator.Context context = invocationOnMock.getArgument(0);
            if (context.collisionIndex() > 0) {
                return String.join("-", context.baseSku(), ""+context.collisionIndex());
            }
            return context.baseSku();
        }).when(skuGenerator).generate(any(SkuGenerator.Context.class));

        Product FULL_PRODUCT = fullProduct();
        List<VariantType> desiredTypes = List.of(
                variantType("size", "Size", "Large", "Small"),
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female"),
                variantType("storage", "Storage", "128", "256")
                );
        String[] desiredCombination = {
                "1 LargeYellowMale128",
                "id LargeYellowMale256",
                "2 LargeYellowFemale128",
                "id LargeYellowFemale256",
                "3 LargeRedMale128",
                "id LargeRedMale256",
                "4 LargeRedFemale128",
                "id LargeRedFemale256",
                "5 SmallYellowMale128",
                "id SmallYellowMale256",
                "6 SmallYellowFemale128",
                "id SmallYellowFemale256",
                "7 SmallRedMale128",
                "id SmallRedMale256",
                "8 SmallRedFemale128",
                "id SmallRedFemale256"
        };
        productFactory.create(FULL_PRODUCT, desiredTypes, List.of());
        assertThat(FULL_PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }

    @Test
    public void shouldGeneratesCombinationsInSpecifiedOrder_whenAddManyVariantOptionAtLastOrder() {
        when(idGenerator.generateId()).thenReturn(id);
        doAnswer(invocationOnMock -> {
            SkuGenerator.Context context = invocationOnMock.getArgument(0);
            if (context.collisionIndex() > 0) {
                return String.join("-", context.baseSku(), ""+context.collisionIndex());
            }
            return context.baseSku();
        }).when(skuGenerator).generate(any(SkuGenerator.Context.class));

        Product FULL_PRODUCT = fullProduct();
        List<VariantType> desiredTypes = List.of(
                variantType("size", "Size", "Large", "Small"),
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female"),
                variantType("storage", "Storage", "128", "256"),
                variantType("storage", "SupportLevel", "Level1", "Level2")
        );
        String[] desiredCombination = {
                "1 LargeYellowMale128Level1",
                "id LargeYellowMale128Level2",
                "id LargeYellowMale256Level1",
                "id LargeYellowMale256Level2",
                "2 LargeYellowFemale128Level1",
                "id LargeYellowFemale128Level2",
                "id LargeYellowFemale256Level1",
                "id LargeYellowFemale256Level2",
                "3 LargeRedMale128Level1",
                "id LargeRedMale128Level2",
                "id LargeRedMale256Level1",
                "id LargeRedMale256Level2",
                "4 LargeRedFemale128Level1",
                "id LargeRedFemale128Level2",
                "id LargeRedFemale256Level1",
                "id LargeRedFemale256Level2",
                "5 SmallYellowMale128Level1",
                "id SmallYellowMale128Level2",
                "id SmallYellowMale256Level1",
                "id SmallYellowMale256Level2",
                "6 SmallYellowFemale128Level1",
                "id SmallYellowFemale128Level2",
                "id SmallYellowFemale256Level1",
                "id SmallYellowFemale256Level2",
                "7 SmallRedMale128Level1",
                "id SmallRedMale128Level2",
                "id SmallRedMale256Level1",
                "id SmallRedMale256Level2",
                "8 SmallRedFemale128Level1",
                "id SmallRedFemale128Level2",
                "id SmallRedFemale256Level1",
                "id SmallRedFemale256Level2"
        };
        productFactory.create(FULL_PRODUCT, desiredTypes, List.of());
        assertThat(FULL_PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }

    @Test
    public void shouldGeneratesCombinationsInSpecifiedOrder_whenAddVariantOptionAtFirstOrder() {
        when(idGenerator.generateId()).thenReturn(id);
        doAnswer(invocationOnMock -> {
            SkuGenerator.Context context = invocationOnMock.getArgument(0);
            if (context.collisionIndex() > 0) {
                return String.join("-", context.baseSku(), ""+context.collisionIndex());
            }
            return context.baseSku();
        }).when(skuGenerator).generate(any(SkuGenerator.Context.class));

        Product FULL_PRODUCT = fullProduct();
        List<VariantType> desiredTypes = List.of(
                variantType("storage", "Storage", "128", "256"),
                variantType("size", "Size", "Large", "Small"),
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female")
        );
        String[] desiredCombination = {
                "1 128LargeYellowMale",
                "2 128LargeYellowFemale",
                "3 128LargeRedMale",
                "4 128LargeRedFemale",
                "5 128SmallYellowMale",
                "6 128SmallYellowFemale",
                "7 128SmallRedMale",
                "8 128SmallRedFemale",
                "id 256LargeYellowMale",
                "id 256LargeYellowFemale",
                "id 256LargeRedMale",
                "id 256LargeRedFemale",
                "id 256SmallYellowMale",
                "id 256SmallYellowFemale",
                "id 256SmallRedMale",
                "id 256SmallRedFemale"
        };
        productFactory.create(FULL_PRODUCT, desiredTypes, List.of());
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
                "1 LargeYellowMale",
                "2 LargeYellowFemale",
                "3 LargeRedMale",
                "4 LargeRedFemale",
                "6 SmallYellowFemale",
                "7 SmallRedMale",
                "8 SmallRedFemale",
        };
        productFactory.create(FULL_PRODUCT, desiredTypes, List.of(new ProductTestData.CommonId("5")) );
        assertThat(FULL_PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }

    @Test
    public void shouldGeneratesCombinationsInSpecifiedOrder_whenAddVariantTypes() {
        when(idGenerator.generateId()).thenReturn(id);
        doAnswer(invocationOnMock -> {
            SkuGenerator.Context context = invocationOnMock.getArgument(0);
            String sku = context.baseSku() == null ? getSku(context.orderedVariations()) : context.baseSku();
            if (context.collisionIndex() > 0) {
                return String.join("-", sku, "" + context.collisionIndex());
            }
            return sku;
        }).when(skuGenerator).generate(any(SkuGenerator.Context.class));

        Product PRODUCT = emptyVariationProduct();
        List<VariantType> desiredTypes = List.of(
                variantType("size", "Size", "Large", "Small"),
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female")
        );
        String[] desiredCombination = {
                "id LargeYellowMale",
                "id LargeYellowFemale",
                "id LargeRedMale",
                "id LargeRedFemale",
                "id SmallYellowMale",
                "id SmallYellowFemale",
                "id SmallRedMale",
                "id SmallRedFemale",
        };
        productFactory.create(PRODUCT, desiredTypes, List.of(new ProductTestData.CommonId("5")) );
        assertThat(PRODUCT.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }

    @Test
    public void shouldCreateProduct_whenDesiredTypesGiven() {
        when(idGenerator.generateId()).thenReturn(id);
        doAnswer(invocationOnMock -> {
            SkuGenerator.Context context = invocationOnMock.getArgument(0);
            String sku = context.baseSku() == null ? getSku(context.orderedVariations()) : context.baseSku();
            if (context.collisionIndex() > 0) {
                return String.join("-", sku, "" + context.collisionIndex());
            }
            return sku;
        }).when(skuGenerator).generate(any(SkuGenerator.Context.class));

        List<VariantType> desiredTypes = List.of(
                variantType("size", "Size", "Large", "Small"),
                variantType("color", "Color", "Yellow", "Red"),
                variantType("gender", "Gender", "Male", "Female")
        );
        String[] desiredCombination = {
                "id LargeYellowMale",
                "id LargeYellowFemale",
                "id LargeRedMale",
                "id LargeRedFemale",
                "id SmallYellowMale",
                "id SmallYellowFemale",
                "id SmallRedMale",
                "id SmallRedFemale",
        };
        ProductSpec spec = new ProductSpec(
                "T-Shirt",
                new CommonId("Clothing"),
                desiredTypes
        );
        Product product = productFactory.create(spec);
        assertEquals(spec.name(), product.getName());
        assertEquals(spec.categoryId(), product.getCategoryId());
        assertThat(product.getVariants())
                .extracting(this::extractProductVariation)
                .containsExactly(desiredCombination );
    }


}
