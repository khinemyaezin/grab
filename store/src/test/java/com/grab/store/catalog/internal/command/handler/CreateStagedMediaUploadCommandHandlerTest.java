package com.grab.store.catalog.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.storage.FileStoragePort;
import com.grab.framework.storage.PresignedUpload;
import com.grab.framework.storage.UploadRequest;
import com.grab.store.catalog.internal.command.CreateStagedMediaUploadCommand;
import com.grab.store.catalog.internal.service.MediaUploadValidator;
import com.grab.store.catalog.internal.command.ProductMediaUploadResult;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateStagedMediaUploadCommandHandlerTest {

    private FileStoragePort fileStoragePort;
    private CreateStagedMediaUploadCommandHandler handler;

    @BeforeEach
    void setUp() {
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
        handler = new CreateStagedMediaUploadCommandHandler(
                fileStoragePort,
                idGenerator,
                new MediaUploadValidator(10_485_760)
        );
    }

    @Test
    void issuesPresignedPutWithStagedKeyThatHasNoProductSegment() {
        when(fileStoragePort.createPresignedUpload(any(UploadRequest.class))).thenReturn(new PresignedUpload(
                "https://minio/put",
                "PUT",
                Map.of("Content-Type", "image/jpeg"),
                "merchants/merchant-1/staged/object-1.jpg",
                Instant.parse("2026-01-01T00:15:00Z")
        ));

        ProductMediaUploadResult result = handler.handle(new CreateStagedMediaUploadCommand(
                new CommonId("merchant-1"),
                "hero.jpg",
                "image/jpeg",
                1024L
        ));

        assertThat(result.storageKey()).isEqualTo("merchants/merchant-1/staged/object-1.jpg");
        assertThat(result.storageKey()).doesNotContain("products/");
        assertThat(result.method()).isEqualTo("PUT");
        verify(fileStoragePort).createPresignedUpload(any(UploadRequest.class));
    }

    @Test
    void rejectsDisallowedContentTypeWithoutLoadingAProduct() {
        assertThatThrownBy(() -> handler.handle(new CreateStagedMediaUploadCommand(
                new CommonId("merchant-1"),
                "notes.txt",
                "text/plain",
                12L
        )))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.product.media_upload_invalid"));
    }

    @Test
    void rejectsFileOverMaxSize() {
        assertThatThrownBy(() -> handler.handle(new CreateStagedMediaUploadCommand(
                new CommonId("merchant-1"),
                "hero.jpg",
                "image/jpeg",
                10_485_761L
        )))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.product.media_upload_invalid"));
    }
}
