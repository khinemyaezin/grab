package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductStatus;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.event.ProductDeletedEvent;
import com.catalog.domain.event.ProductStatusChangedEvent;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.domain.repository.ProductRepository;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.DeleteProductCommand;
import com.grab.store.catalog.internal.command.DeleteProductResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteProductCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private DeleteProductCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DeleteProductCommandHandler(productRepository);
    }

    @Test
    void handle_archivesProductBeforeSavingDeleteEvent() {
        Id productId = new CommonId("product-123");
        Product product = Product.create(productId, "Product", new CommonId("category-456"));
        product.addVariant(ProductVariant.create(
                new CommonId("variant-1"),
                "SKU-1",
                List.of(new ProductVariation("Red", new CommonId("opt-red"), "Color", new CommonId("type-color")))
        ));

        when(productRepository.find(productId)).thenReturn(Optional.of(product));

        DeleteProductResult result = handler.handle(new DeleteProductCommand(productId));

        verify(productRepository).save(productCaptor.capture());
        Product saved = productCaptor.getValue();

        assertThat(saved.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
        assertThat(saved.getEvents()).anyMatch(ProductStatusChangedEvent.class::isInstance);
        assertThat(saved.getEvents()).anyMatch(ProductDeletedEvent.class::isInstance);
        assertThat(result.deleted()).isTrue();
    }
}
