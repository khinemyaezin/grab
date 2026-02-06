package com.grab.store.product.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.store.product.internal.assembler.CommonId;
import com.grab.store.product.internal.command.SaveProductCommand;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariantStatus;
import com.product.domain.repository.ProductRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveProductCommandHandlerTest {
    @Mock
    private ProductRepository productRepository;

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
        handler = new SaveProductCommandHandler(productRepository);
    }

    @Test
    void handle_withVariants_addsAllVariantsToProduct() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id variantId = new CommonId(VARIANT_ID);
        Id colorTypeId = new CommonId(COLOR_TYPE_ID);
        Id redOptionId = new CommonId(RED_OPTION_ID);

        SaveProductCommand.Variation variation = new SaveProductCommand.Variation(
                "Red", redOptionId, colorTypeId, "Color"
        );
        SaveProductCommand.Variant variant = new SaveProductCommand.Variant(
                variantId, "SKU-RED-001", "ACTIVE", List.of(variation)
        );
        SaveProductCommand command = new SaveProductCommand(
                new SaveProductCommand.Product(
                        productId,
                        "Product with Variants",
                        categoryId,
                        List.of(variant)));

        handler.handle(command);

        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getVariants()).hasSize(1);
        assertThat(savedProduct.getVariants().getFirst().getSku()).isEqualTo("SKU-RED-001");
        assertThat(savedProduct.getVariants().getFirst().getStatus()).isEqualTo(ProductVariantStatus.ACTIVE);
    }
}
