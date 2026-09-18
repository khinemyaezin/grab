package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.domain.repository.ProductPublicationRepository;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.valueobject.ProductStatus;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.UnpublishProductFromChannelCommand;
import com.grab.store.catalog.internal.command.UnpublishProductFromChannelResult;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnpublishProductFromChannelCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductPublicationRepository productPublicationRepository;

    private UnpublishProductFromChannelCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UnpublishProductFromChannelCommandHandler(productRepository, productPublicationRepository);
    }

    @Test
    void handle_whenMissingRow_isNoOp() {
        when(productRepository.find(new CommonId("prod-1"), new CommonId("merchant-1")))
                .thenReturn(Optional.of(product()));
        when(productPublicationRepository.find(new CommonId("prod-1"), new CommonId("channel-1")))
                .thenReturn(Optional.empty());

        UnpublishProductFromChannelResult result = handler.handle(command());

        assertThat(result.deleted()).isFalse();
        verify(productPublicationRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void handle_whenPresent_deletesWithoutChangingProduct() {
        ProductPublication publication = ProductPublication.restore(
                new CommonId("prod-1"),
                new CommonId("channel-1"),
                Instant.parse("2026-09-18T00:00:00Z")
        );
        when(productRepository.find(new CommonId("prod-1"), new CommonId("merchant-1")))
                .thenReturn(Optional.of(product()));
        when(productPublicationRepository.find(new CommonId("prod-1"), new CommonId("channel-1")))
                .thenReturn(Optional.of(publication));

        UnpublishProductFromChannelResult result = handler.handle(command());

        assertThat(result.deleted()).isTrue();
        verify(productPublicationRepository).delete(publication);
        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void handle_whenProductMissing_throws() {
        when(productRepository.find(new CommonId("prod-1"), new CommonId("merchant-1")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command()))
                .isInstanceOf(CatalogServiceException.class);
    }

    private UnpublishProductFromChannelCommand command() {
        return new UnpublishProductFromChannelCommand(
                new CommonId("merchant-1"),
                new CommonId("prod-1"),
                new CommonId("channel-1")
        );
    }

    private Product product() {
        return new Product(
                new CommonId("prod-1"),
                new CommonId("merchant-1"),
                "Shirt",
                new CommonId("cat-1"),
                null,
                ProductStatus.ACTIVE,
                "shirt",
                List.of(),
                List.of(),
                List.of()
        );
    }
}
