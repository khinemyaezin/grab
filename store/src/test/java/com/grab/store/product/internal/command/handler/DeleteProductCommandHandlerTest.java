package com.grab.store.product.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.product.internal.assembler.CommonId;
import com.grab.store.product.internal.command.DeleteProductCommand;
import com.grab.store.product.internal.command.DeleteProductResult;
import com.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProductCommandHandlerTest {

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private ProductRepository productRepository;

    private DeleteProductCommandHandler handler;

    private static final String PRODUCT_ID = "product-123";

    @BeforeEach
    void setUp() {
        handler = new DeleteProductCommandHandler(idGenerator, productRepository);
    }

    @Test
    void handle_withExistingProduct_deletesAndReturnsTrue() {
        // Given
        Id productId = new CommonId(PRODUCT_ID);
        DeleteProductCommand command = new DeleteProductCommand(PRODUCT_ID);

        when(idGenerator.generateId(PRODUCT_ID)).thenReturn(productId);
        when(productRepository.exists(productId)).thenReturn(true);

        // When
        DeleteProductResult result = handler.handle(command);

        // Then
        assertThat(result.deleted()).isTrue();
        verify(productRepository).exists(productId);
        verify(productRepository).delete(productId);
    }

    @Test
    void handle_withNonExistingProduct_returnsFalseAndDoesNotDelete() {
        // Given
        Id productId = new CommonId(PRODUCT_ID);
        DeleteProductCommand command = new DeleteProductCommand(PRODUCT_ID);

        when(idGenerator.generateId(PRODUCT_ID)).thenReturn(productId);
        when(productRepository.exists(productId)).thenReturn(false);

        // When
        DeleteProductResult result = handler.handle(command);

        // Then
        assertThat(result.deleted()).isFalse();
        verify(productRepository).exists(productId);
        verify(productRepository, never()).delete(any());
    }
}
