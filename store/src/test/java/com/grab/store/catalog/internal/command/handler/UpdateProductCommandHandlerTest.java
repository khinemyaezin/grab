package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductStatus;
import com.catalog.domain.repository.ProductRepository;
import com.grab.framework.id.Id;
import com.grab.store.catalog.internal.assembler.CommonId;
import com.grab.store.catalog.internal.command.UpdateProductCommand;
import com.grab.store.catalog.internal.command.UpdateProductResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProductCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private UpdateProductCommandHandler handler;

    private static final String PRODUCT_ID = "product-123";
    private static final String CATEGORY_ID = "category-456";
    private static final String NEW_CATEGORY_ID = "category-789";

    @BeforeEach
    void setUp() {
        handler = new UpdateProductCommandHandler(productRepository);
    }

    @Test
    void handle_updatesNameAndCategory() {
        Id productId = new CommonId(PRODUCT_ID);
        Id categoryId = new CommonId(CATEGORY_ID);
        Id newCategoryId = new CommonId(NEW_CATEGORY_ID);

        Product existing = Product.create(productId, "Old Name", categoryId);
        when(productRepository.find(productId)).thenReturn(Optional.of(existing));

        UpdateProductCommand command = new UpdateProductCommand(productId, "New Name", newCategoryId);
        UpdateProductResult result = handler.handle(command);

        verify(productRepository).save(productCaptor.capture());
        Product saved = productCaptor.getValue();

        assertThat(saved.getName()).isEqualTo("New Name");
        assertThat(saved.getCategoryId()).isEqualTo(newCategoryId);
        assertThat(saved.getSlug()).isEqualTo("new-name");

        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.categoryId()).isEqualTo(NEW_CATEGORY_ID);
        assertThat(result.status()).isEqualTo(ProductStatus.DRAFT.name());
        assertThat(result.slug()).isEqualTo("new-name");
        assertThat(result.featured()).isFalse();
    }

    @Test
    void handle_productNotFound_throws() {
        Id productId = new CommonId(PRODUCT_ID);
        when(productRepository.find(productId)).thenReturn(Optional.empty());

        UpdateProductCommand command = new UpdateProductCommand(productId, "Name", new CommonId(CATEGORY_ID));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
