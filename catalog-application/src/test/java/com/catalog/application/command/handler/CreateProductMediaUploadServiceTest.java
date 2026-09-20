package com.catalog.application.command.handler;

import com.catalog.application.service.CreateProductMediaUploadService;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.port.outbound.ProductRepository;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.storage.FileStoragePort;
import com.grab.framework.storage.PresignedUpload;
import com.grab.framework.storage.UploadRequest;
import com.catalog.application.command.CreateProductMediaUploadCommand;
import com.catalog.application.service.MediaUploadValidator;
import com.catalog.application.command.ProductMediaUploadResult;
import com.catalog.application.exception.CatalogServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateProductMediaUploadServiceTest {

    private ProductRepository productRepository;
    private FileStoragePort fileStoragePort;
    private CreateProductMediaUploadService service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        fileStoragePort = mock(FileStoragePort.class);
        IdGenerator idGenerator = new IdGenerator() {
            @Override
            public Id generateId() {
                return new CommonId("object-1");
            }

            @Override
            public Id convertIdFrom(String id) {
                return new CommonId(id);
            }
        };
        service = new CreateProductMediaUploadService(
                productRepository,
                fileStoragePort,
                idGenerator,
                new MediaUploadValidator(10_485_760)
        );
    }

    @Test
    void issuesPresignedPutWithProductScopedKey() {
        Product product = Product.create(
                new CommonId("product-1"),
                new CommonId("merchant-1"),
                "Camera",
                new CommonId("electronics")
        );
        when(productRepository.find(new CommonId("product-1"), new CommonId("merchant-1")))
                .thenReturn(Optional.of(product));
        when(fileStoragePort.createPresignedUpload(any(UploadRequest.class))).thenReturn(new PresignedUpload(
                "https://minio/put",
                "PUT",
                Map.of("Content-Type", "image/jpeg"),
                "merchants/merchant-1/products/product-1/object-1.jpg",
                Instant.parse("2026-01-01T00:15:00Z")
        ));

        ProductMediaUploadResult result = service.execute(new CreateProductMediaUploadCommand(
                new CommonId("merchant-1"),
                new CommonId("product-1"),
                "hero.jpg",
                "image/jpeg",
                1024L
        ));

        assertThat(result.storageKey()).isEqualTo("merchants/merchant-1/products/product-1/object-1.jpg");
        assertThat(result.method()).isEqualTo("PUT");
        assertThat(result.url()).isEqualTo("https://minio/put");
        verify(fileStoragePort).createPresignedUpload(any(UploadRequest.class));
    }

    @Test
    void rejectsDisallowedContentType() {
        Product product = Product.create(
                new CommonId("product-1"),
                new CommonId("merchant-1"),
                "Camera",
                new CommonId("electronics")
        );
        when(productRepository.find(new CommonId("product-1"), new CommonId("merchant-1")))
                .thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.execute(new CreateProductMediaUploadCommand(
                new CommonId("merchant-1"),
                new CommonId("product-1"),
                "notes.txt",
                "text/plain",
                12L
        )))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.product.media_upload_invalid"));
    }
}
