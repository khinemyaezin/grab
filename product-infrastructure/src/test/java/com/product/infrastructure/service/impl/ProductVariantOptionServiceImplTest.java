package com.product.infrastructure.service.impl;

import com.product.domain.aggregate.product.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.entity.product.entity.ProductVariationEntity;
import com.product.infrastructure.entity.product.entity.VariantOptionEntity;
import com.product.infrastructure.entity.product.entity.VariantTypeEntity;
import com.product.infrastructure.entity.product.factory.ProductVariantOptionEntityFactory;
import com.product.infrastructure.mapper.product.ProductVariationMapper;
import com.product.infrastructure.repository.jpa.VariantOptionJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductVariantOptionServiceImplTest extends ProductTestData{

    @Mock
    private ProductVariantOptionEntityFactory productVariantOptionEntityFactory;

    @Mock
    private ProductVariationMapper productVariationMapper;

    @Mock
    private VariantOptionJpaRepository variantOptionJpaRepository;

    @InjectMocks
    private ProductVariantOptionServiceImpl service;

    @Test
    void updateVariations_shouldKeepExistingAndAddNewOnes() {
        ProductVariantEntity variantEntity = new ProductVariantEntity();

        ProductVariationEntity existingOne = new ProductVariationEntity();
        existingOne.setId(1L);
        existingOne.setVariantOptionValue("Red");
        ProductVariation existingVariationOne = new ProductVariation("Red", id("opt-red"), "Color",id("color"));
        when(productVariationMapper.toDomain(existingOne)).thenReturn(existingVariationOne);

        ProductVariationEntity existingTwo = new ProductVariationEntity();
        existingTwo.setId(2L);
        existingTwo.setVariantOptionValue("Small");
        ProductVariation existingVariationTwo = new ProductVariation("Small", id("opt-s"), "Size", id("size"));
        when(productVariationMapper.toDomain(existingTwo)).thenReturn(existingVariationTwo);

        variantEntity.addProductVariantOption(existingOne);
        variantEntity.addProductVariantOption(existingTwo);

        ProductVariation blueVariation = new ProductVariation("Blue", id("opt-blue"), "Color",id("color"));
        ProductVariation mediumVariation = new ProductVariation("Medium", id("opt-medium"), "Size", id("size"));

        VariantTypeEntity colorType = new VariantTypeEntity();
        VariantOptionEntity blueOption = new VariantOptionEntity();
        blueOption.setUuid(blueVariation.getOptionId().getValue());
        blueOption.setVariantType(colorType);

        when(variantOptionJpaRepository.findByUuid(blueVariation.getOptionId().getValue()))
                .thenReturn(Optional.of(blueOption));

        ProductVariationEntity blueVariationEntity = new ProductVariationEntity();
        blueVariationEntity.setVariantOptionValue("Blue");
        when(productVariantOptionEntityFactory.create(eq(blueVariation), any(), any()))
                .thenAnswer(invocation -> blueVariationEntity);
        ProductVariationEntity mediumVariationEntity = new ProductVariationEntity();
        mediumVariationEntity.setVariantOptionValue("Medium");
        when(productVariantOptionEntityFactory.create(eq(mediumVariation), any(), any()))
                .thenAnswer(invocation -> mediumVariationEntity);

        Set<ProductVariation> updates = new LinkedHashSet<>();
        updates.add(blueVariation);
        updates.add(mediumVariation);

        service.updateVariations(variantEntity, updates);

        assertThat(variantEntity.getProductVariations()).hasSize(2);
        assertThat(variantEntity.getProductVariations()).contains(blueVariationEntity, mediumVariationEntity);

        verify(variantOptionJpaRepository, times(1)).findByUuid(blueVariation.getOptionId().getValue());
        verify(productVariantOptionEntityFactory, times(2)).create(any(ProductVariation.class), any(), any());
        verify(productVariationMapper, times(2)).toDomain(any(ProductVariationEntity.class));
    }
}
