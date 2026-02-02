package com.product.domain.factory;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.product.domain.aggregate.product.*;
import com.product.domain.factory.impl.ProductFactoryImpl;
import com.product.domain.service.SkuGenerator;
import com.product.domain.service.impl.VariantCombinationServiceImpl;
import com.product.domain.valueobject.ProductVariation;
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
        productFactory = new ProductFactoryImpl(
                new VariantCombinationServiceImpl(),
                idGenerator,
                skuGenerator
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
                "id LargeYellowMale ACTIVE",
                "id LargeYellowFemale ACTIVE",
                "id LargeRedMale ACTIVE",
                "id LargeRedFemale ACTIVE",
                "id SmallYellowMale ACTIVE",
                "id SmallYellowFemale ACTIVE",
                "id SmallRedMale ACTIVE",
                "id SmallRedFemale ACTIVE",
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
