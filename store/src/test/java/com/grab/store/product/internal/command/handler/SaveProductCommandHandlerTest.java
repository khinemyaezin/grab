package com.grab.store.product.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.product.internal.assembler.CommonId;
import com.grab.store.product.internal.command.SaveProductCommand;
import com.grab.store.product.internal.command.SaveProductResult;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariantStatus;
import com.product.domain.repository.ProductRepository;
import com.product.domain.service.ProductVariantSynchronizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveProductCommandHandlerTest {

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantSynchronizer productVariantSynchronizer;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private SaveProductCommandHandler handler;

    private static final String PRODUCT_ID = "product-123";
    private static final String CATEGORY_ID = "category-456";
    private static final String VARIANT_ID = "variant-789";
    private static final String COLOR_TYPE_ID = "color-type-001";
    private static final String RED_OPTION_ID = "red-option-001";

    @BeforeEach
    void setUp() {
        handler = new SaveProductCommandHandler(idGenerator, productRepository, productVariantSynchronizer);
    }

    @Test
    void handle_withNewProduct_savesProductAndReturnsId() {
        Id generatedProductId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);

        when(idGenerator.generateId()).thenReturn(generatedProductId);
        when(idGenerator.generateId(CATEGORY_ID)).thenReturn(categoryId);

        SaveProductCommand command = createCommandWithoutProductId();

        // When
        SaveProductResult result = handler.handle(command);

        // Then
        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        verify(productRepository).save(any(Product.class));
        verify(productVariantSynchronizer).synchronize(any(Product.class), anyList());
    }


    @Test
    void handle_withVariants_addsAllVariantsToProduct() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id variantId = new CommonId(VARIANT_ID);
        Id colorTypeId = new CommonId(COLOR_TYPE_ID);
        Id redOptionId = new CommonId(RED_OPTION_ID);

        when(idGenerator.generateId(PRODUCT_ID)).thenReturn(productId);
        when(idGenerator.generateId(CATEGORY_ID)).thenReturn(categoryId);
        when(idGenerator.generateId(VARIANT_ID)).thenReturn(variantId);
        when(idGenerator.generateId(COLOR_TYPE_ID)).thenReturn(colorTypeId);
        when(idGenerator.generateId(RED_OPTION_ID)).thenReturn(redOptionId);

        SaveProductCommand command = createCommandWithVariants();

        handler.handle(command);

        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getVariants()).hasSize(1);
        assertThat(savedProduct.getVariants().getFirst().getSku()).isEqualTo("SKU-RED-001");
        assertThat(savedProduct.getVariants().getFirst().getStatus()).isEqualTo(ProductVariantStatus.ACTIVE);
    }

    @Test
    void handle_withVariantTypes_callsSynchronizerWithMappedTypes() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id colorTypeId = new CommonId(COLOR_TYPE_ID);
        Id redOptionId = new CommonId(RED_OPTION_ID);

        when(idGenerator.generateId(PRODUCT_ID)).thenReturn(productId);
        when(idGenerator.generateId(CATEGORY_ID)).thenReturn(categoryId);
        when(idGenerator.generateId(COLOR_TYPE_ID)).thenReturn(colorTypeId);
        when(idGenerator.generateId(RED_OPTION_ID)).thenReturn(redOptionId);

        SaveProductCommand command = createCommandWithVariantTypes();

        handler.handle(command);

        verify(productVariantSynchronizer).synchronize(any(Product.class), argThat(variantTypes -> {
            assertThat(variantTypes).hasSize(1);
            assertThat(variantTypes.getFirst().getName()).isEqualTo("Color");
            assertThat(variantTypes.getFirst().getOptions()).hasSize(1);
            assertThat(variantTypes.getFirst().getOptions().iterator().next().getName()).isEqualTo("Red");
            return true;
        }));
    }

    private SaveProductCommand createCommandWithoutProductId() {
        return new SaveProductCommand(
                new SaveProductCommand.Product(null, "New Product", CATEGORY_ID, List.of()),
                List.of()
        );
    }

    private SaveProductCommand createCommandWithProductId() {
        return new SaveProductCommand(
                new SaveProductCommand.Product(PRODUCT_ID, "Existing Product", CATEGORY_ID, List.of()),
                List.of()
        );
    }

    private SaveProductCommand createCommandWithVariants() {
        SaveProductCommand.Variation variation = new SaveProductCommand.Variation(
                "Red", RED_OPTION_ID, COLOR_TYPE_ID, "Color"
        );
        SaveProductCommand.Variant variant = new SaveProductCommand.Variant(
                VARIANT_ID, "SKU-RED-001", "ACTIVE", List.of(variation)
        );
        return new SaveProductCommand(
                new SaveProductCommand.Product(PRODUCT_ID, "Product with Variants", CATEGORY_ID, List.of(variant)),
                List.of()
        );
    }

    private SaveProductCommand createCommandWithVariantTypes() {
        SaveProductCommand.VariantOption redOption = new SaveProductCommand.VariantOption(RED_OPTION_ID, "Red");
        SaveProductCommand.VariantType colorType = new SaveProductCommand.VariantType(
                COLOR_TYPE_ID, "Color", List.of(redOption)
        );
        return new SaveProductCommand(
                new SaveProductCommand.Product(PRODUCT_ID, "Product with Types", CATEGORY_ID, List.of()),
                List.of(colorType)
        );
    }

    private SaveProductCommand createCommandWithNullVariations() {
        SaveProductCommand.Variant variant = new SaveProductCommand.Variant(
                VARIANT_ID, "SKU-001", "ACTIVE", null
        );
        return new SaveProductCommand(
                new SaveProductCommand.Product(PRODUCT_ID, "Product", CATEGORY_ID, List.of(variant)),
                List.of()
        );
    }

    private SaveProductCommand createCommandWithNullStatus() {
        SaveProductCommand.Variant variant = new SaveProductCommand.Variant(
                VARIANT_ID, "SKU-001", null, List.of()
        );
        return new SaveProductCommand(
                new SaveProductCommand.Product(PRODUCT_ID, "Product", CATEGORY_ID, List.of(variant)),
                List.of()
        );
    }

    private SaveProductCommand createCommandWithInactiveStatus() {
        SaveProductCommand.Variant variant = new SaveProductCommand.Variant(
                VARIANT_ID, "SKU-001", "INACTIVE", List.of()
        );
        return new SaveProductCommand(
                new SaveProductCommand.Product(PRODUCT_ID, "Product", CATEGORY_ID, List.of(variant)),
                List.of()
        );
    }
}
