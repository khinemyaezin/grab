package com.product.infrastructure.service.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.aggregate.product.ProductVariation;
import com.product.infrastructure.common.CommonId;
import com.product.infrastructure.entity.category.entity.CategoryEntity;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.repository.jpa.CategoryJpaRepository;
import com.product.infrastructure.repository.jpa.ProductJpaRepository;
import com.product.infrastructure.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ProductServiceImplTest {

    @MockBean
    private ProductJpaRepository productJpaRepository;
    @MockBean
    private CategoryJpaRepository categoryJpaRepository;
    @MockBean
    private IdGenerator idGenerator;
    @Autowired
    private ProductService productService;

    private Product product;
    private CategoryEntity categoryEntity;
    private ProductVariant variant1, variant2;

    @BeforeEach
    void setup() throws IOException {
        Id categoryId = new CommonId();
        Id productId = new CommonId();

        categoryEntity = new CategoryEntity();
        categoryEntity.setId(1L);
        categoryEntity.setUuid(categoryId.getValue());
        categoryEntity.setName("Shirt");

        ProductVariation variationSizeL = new ProductVariation("Size", "L");
        ProductVariation variationColorRed = new ProductVariation("Color", "Red");

        variant1 = new ProductVariant(new CommonId(), productId, "SKU-ABC", List.of(variationSizeL));
        variant2 = new ProductVariant(new CommonId(), productId, "SKU-XYZ", List.of(variationColorRed));

        product = new Product(productId, "Test Product", categoryId);
        product.addVariant(variant1);
        product.addVariant(variant2);
    }

    @Test
    void findOrBuildProduct_shouldCreateNewEntity_whenProductNotExists() {
        when(productJpaRepository.findByUuid(product.getId().getValue())).thenReturn(Optional.empty());
        ProductEntity result = productService.findOrBuildProduct(product, categoryEntity);

        assertNotNull(result);
        assertEquals(product.getName(), result.getName());
        assertEquals(product.getId().getValue(), result.getUuid());
        assertEquals(categoryEntity.getId(), result.getCategory().getId());
    }

    @Test
    void save_shouldPersistProductWithCategory() {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(product.getName());
        productEntity.setUuid(product.getId().getValue());
        productEntity.setCategory(categoryEntity);

        when(productJpaRepository.save(any(ProductEntity.class)))
                .thenReturn(productEntity);

        ProductEntity savedEntity = productService.save(productEntity);

        verify(productJpaRepository, times(1)).save(productEntity);
        assertNotNull(savedEntity);
        assertEquals(product.getName(), savedEntity.getName());
    }

    @Test
    void save_shouldMaintainBidirectionalRelationships() {
        // Arrange
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(product.getName());
        productEntity.setUuid(product.getId().getValue());

        // Set up the category with products list
        categoryEntity.setProducts(new ArrayList<>());
        productEntity.setCategory(categoryEntity);

        when(productJpaRepository.save(any(ProductEntity.class)))
                .thenAnswer(invocation -> {
                    ProductEntity pe = invocation.getArgument(0);
                    // Verify bidirectional relationship
                    assertTrue(pe.getCategory().getProducts().contains(pe));
                    return pe;
                });

        // Act
        productService.save(productEntity);

        // Assert
        verify(productJpaRepository).save(productEntity);
    }
}