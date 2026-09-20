package com.catalog.application.command.handler;

import com.catalog.application.service.PublishProductToChannelService;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.port.outbound.ProductPublicationRepository;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.domain.valueobject.ProductStatus;
import com.grab.framework.id.impl.CommonId;
import com.catalog.application.model.write.PublishProductToChannelCommand;
import com.catalog.application.model.write.PublishProductToChannelResult;
import com.catalog.application.exception.CatalogServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublishProductToChannelServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductPublicationRepository productPublicationRepository;

    private PublishProductToChannelService service;

    @BeforeEach
    void setUp() {
        service = new PublishProductToChannelService(productRepository, productPublicationRepository);
    }

    @Test
    void handle_whenRowExists_isIdempotent() {
        when(productRepository.find(new CommonId("prod-1"), new CommonId("merchant-1")))
                .thenReturn(Optional.of(activeProduct()));
        when(productPublicationRepository.exists(new CommonId("var-1"), new CommonId("channel-1")))
                .thenReturn(true);

        PublishProductToChannelResult result = service.execute(command());

        assertThat(result.written()).isFalse();
        assertThat(result.variantId()).isEqualTo("var-1");
        verify(productPublicationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void handle_whenMissing_insertsPublicationWithoutSavingProduct() {
        when(productRepository.find(new CommonId("prod-1"), new CommonId("merchant-1")))
                .thenReturn(Optional.of(activeProduct()));
        when(productPublicationRepository.exists(new CommonId("var-1"), new CommonId("channel-1")))
                .thenReturn(false);

        PublishProductToChannelResult result = service.execute(command());

        assertThat(result.written()).isTrue();
        ArgumentCaptor<ProductPublication> captor = ArgumentCaptor.forClass(ProductPublication.class);
        verify(productPublicationRepository).save(captor.capture());
        assertThat(captor.getValue().getVariantId().getValue()).isEqualTo("var-1");
        assertThat(captor.getValue().getSalesChannelId().getValue()).isEqualTo("channel-1");
        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void handle_whenProductNotActive_skipsWrite() {
        when(productRepository.find(new CommonId("prod-1"), new CommonId("merchant-1")))
                .thenReturn(Optional.of(product(ProductStatus.DRAFT)));
        when(productPublicationRepository.exists(new CommonId("var-1"), new CommonId("channel-1")))
                .thenReturn(false);

        PublishProductToChannelResult result = service.execute(command());

        assertThat(result.written()).isFalse();
        assertThat(result.variantId()).isEqualTo("var-1");
        verify(productPublicationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void handle_whenVariantNotOnProduct_throws() {
        when(productRepository.find(new CommonId("prod-1"), new CommonId("merchant-1")))
                .thenReturn(Optional.of(activeProduct()));

        PublishProductToChannelCommand otherVariant = new PublishProductToChannelCommand(
                new CommonId("merchant-1"),
                new CommonId("prod-1"),
                new CommonId("var-missing"),
                new CommonId("channel-1")
        );

        assertThatThrownBy(() -> service.execute(otherVariant))
                .isInstanceOf(CatalogServiceException.class);
        verify(productPublicationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private PublishProductToChannelCommand command() {
        return new PublishProductToChannelCommand(
                new CommonId("merchant-1"),
                new CommonId("prod-1"),
                new CommonId("var-1"),
                new CommonId("channel-1")
        );
    }

    private Product activeProduct() {
        return product(ProductStatus.ACTIVE);
    }

    private Product product(ProductStatus status) {
        return new Product(
                new CommonId("prod-1"),
                new CommonId("merchant-1"),
                "Shirt",
                new CommonId("cat-1"),
                null,
                status,
                "shirt",
                List.of(),
                List.of(),
                List.of(ProductVariant.create(new CommonId("var-1"), "SKU-1", List.of()))
        );
    }
}
