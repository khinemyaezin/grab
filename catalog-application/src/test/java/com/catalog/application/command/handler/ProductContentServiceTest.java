package com.catalog.application.command.handler;

import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.service.impl.DefaultProductMediaService;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.storage.FileStoragePort;
import com.catalog.application.model.write.ProductDescriptionsResult;
import com.catalog.application.model.write.ProductMediaResult;
import com.catalog.application.model.write.ReplaceProductDescriptionsCommand;
import com.catalog.application.model.write.ReplaceProductMediaCommand;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.service.ReplaceProductDescriptionsService;
import com.catalog.application.service.ReplaceProductMediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductContentServiceTest {

    private static final String PRODUCT_ID = "product-content-1";
    private static final String CATEGORY_ID = "category-content-1";

    private InMemoryProductRepositoryTest productRepository;
    private ReplaceProductDescriptionsService replaceDescriptionsHandler;
    private ReplaceProductMediaService replaceMediaHandler;
    private FileStoragePort fileStoragePort;

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryProductRepositoryTest();
        IdGenerator idGenerator = new UuidGenerator();
        replaceDescriptionsHandler = new ReplaceProductDescriptionsService(productRepository, idGenerator);
        fileStoragePort = mock(FileStoragePort.class);
        when(fileStoragePort.objectExists(anyString())).thenReturn(true);
        when(fileStoragePort.resolvePublicUrl(anyString())).thenAnswer(invocation ->
                "http://localhost:9000/grab-media/" + invocation.getArgument(0));
        replaceMediaHandler = new ReplaceProductMediaService(
                productRepository, fileStoragePort, idGenerator, new DefaultProductMediaService());
    }

    @Test
    void replaceDescriptionsRemovesOmittedDescriptionsAndPreservesProvidedId() {
        Product product = seedProduct();
        var existingDescriptionId = product.getDescriptions().getFirst().getId();

        ProductDescriptionsResult result = replaceDescriptionsHandler.execute(new ReplaceProductDescriptionsCommand(
                new CommonId(PRODUCT_ID),
                new CommonId(PRODUCT_ID),
                List.of(new ReplaceProductDescriptionsCommand.Description(
                        existingDescriptionId,
                        "summary",
                        "Updated Summary",
                        "Updated body"
                ))
        ));

        Product saved = productRepository.getLastSaved();
        assertThat(saved.getDescriptions()).hasSize(1);
        assertThat(saved.getDescriptions().getFirst().getId()).isEqualTo(existingDescriptionId);
        assertThat(saved.getDescriptions().getFirst().getTitle()).isEqualTo("Updated Summary");
        assertThat(result.descriptions()).hasSize(1);
        assertThat(result.descriptions().getFirst().id()).isEqualTo(existingDescriptionId);
    }

    @Test
    void replaceDescriptionsGeneratesIdWhenMissing() {
        seedProduct();

        ProductDescriptionsResult result = replaceDescriptionsHandler.execute(new ReplaceProductDescriptionsCommand(
                new CommonId(PRODUCT_ID),
                new CommonId(PRODUCT_ID),
                List.of(new ReplaceProductDescriptionsCommand.Description(
                        null,
                        "overview",
                        "Overview",
                        "New body"
                ))
        ));

        Product saved = productRepository.getLastSaved();
        assertThat(saved.getDescriptions()).hasSize(1);
        assertThat(saved.getDescriptions().getFirst().getId()).isNotNull();
        assertThat(saved.getDescriptions().getFirst().getId().getValue()).isNotBlank();
        assertThat(saved.getDescriptions().getFirst().getName()).isEqualTo("overview");
        assertThat(result.descriptions().getFirst().id()).isEqualTo(saved.getDescriptions().getFirst().getId());
    }

    @Test
    void replaceMediaRemovesOmittedMediaAndPreservesProvidedId() {
        Product product = seedProduct();
        var existingMediaId = product.getMedias().getFirst().getId();

        ProductMediaResult result = replaceMediaHandler.execute(new ReplaceProductMediaCommand(
                new CommonId(PRODUCT_ID),
                new CommonId(PRODUCT_ID),
                List.of(new ReplaceProductMediaCommand.Media(
                        existingMediaId,
                        "/images/updated.png",
                        "image/png",
                        0
                ))
        ));

        Product saved = productRepository.getLastSaved();
        assertThat(saved.getMedias()).hasSize(1);
        assertThat(saved.getMedias().getFirst().getId()).isEqualTo(existingMediaId);
        assertThat(saved.getMedias().getFirst().getStorageKey()).isEqualTo("/images/updated.png");
        assertThat(result.medias()).hasSize(1);
        assertThat(result.medias().getFirst().id()).isEqualTo(existingMediaId);
    }

    @Test
    void replaceDescriptionsRejectsBlankName() {
        seedProduct();

        assertThatThrownBy(() -> replaceDescriptionsHandler.execute(new ReplaceProductDescriptionsCommand(
                new CommonId(PRODUCT_ID),
                new CommonId(PRODUCT_ID),
                List.of(new ReplaceProductDescriptionsCommand.Description(
                        null,
                        " ",
                        "Updated Summary",
                        "Updated body"
                ))
        )))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.product.description_patch_invalid"));
    }

    @Test
    void replaceMediaRejectsBlankStorageKey() {
        seedProduct();

        assertThatThrownBy(() -> replaceMediaHandler.execute(new ReplaceProductMediaCommand(
                new CommonId(PRODUCT_ID),
                new CommonId(PRODUCT_ID),
                List.of(new ReplaceProductMediaCommand.Media(
                        null,
                        " ",
                        "image/png",
                        0
                ))
        )))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.product.media_patch_invalid"));
    }

    @Test
    void replaceMediaRejectsMissingObject() {
        seedProduct();
        when(fileStoragePort.objectExists("missing-key")).thenReturn(false);

        assertThatThrownBy(() -> replaceMediaHandler.execute(new ReplaceProductMediaCommand(
                new CommonId(PRODUCT_ID),
                new CommonId(PRODUCT_ID),
                List.of(new ReplaceProductMediaCommand.Media(
                        null,
                        "missing-key",
                        "image/png",
                        0
                ))
        )))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.product.media_object_not_found"));
    }

    @Test
    void replaceMediaCopiesThisMerchantStagedKeyIntoProductPrefix() {
        seedProduct("merchant-1", "product-1");
        String stagedKey = "merchants/merchant-1/staged/object-1.jpg";
        String productKey = "merchants/merchant-1/products/product-1/object-1.jpg";

        ProductMediaResult result = replaceMediaHandler.execute(new ReplaceProductMediaCommand(
                new CommonId("merchant-1"),
                new CommonId("product-1"),
                List.of(new ReplaceProductMediaCommand.Media(
                        null,
                        stagedKey,
                        "image/jpeg",
                        0
                ))
        ));

        Product saved = productRepository.getLastSaved();
        assertThat(saved.getMedias()).hasSize(1);
        assertThat(saved.getMedias().getFirst().getStorageKey()).isEqualTo(productKey);
        assertThat(saved.getMedias().getFirst().getUrl())
                .isEqualTo("http://localhost:9000/grab-media/" + productKey);
        assertThat(result.medias().getFirst().storageKey()).isEqualTo(productKey);
        verify(fileStoragePort).copy(stagedKey, productKey);
        verify(fileStoragePort).delete(stagedKey);
    }

    @Test
    void replaceMediaKeepsExistingIdAndPromotesStagedKeyInSameGallery() {
        Product product = seedProduct("merchant-1", "product-1");
        var existingMediaId = product.getMedias().getFirst().getId();
        String keepKey = "merchants/merchant-1/products/product-1/keep.jpg";
        String stagedKey = "merchants/merchant-1/staged/object-2.jpg";
        String promotedKey = "merchants/merchant-1/products/product-1/object-2.jpg";

        ProductMediaResult result = replaceMediaHandler.execute(new ReplaceProductMediaCommand(
                new CommonId("merchant-1"),
                new CommonId("product-1"),
                List.of(
                        new ReplaceProductMediaCommand.Media(
                                existingMediaId,
                                keepKey,
                                "image/jpeg",
                                0
                        ),
                        new ReplaceProductMediaCommand.Media(
                                null,
                                stagedKey,
                                "image/jpeg",
                                1
                        )
                )
        ));

        Product saved = productRepository.getLastSaved();
        assertThat(saved.getMedias()).hasSize(2);
        assertThat(saved.getMedias().getFirst().getId()).isEqualTo(existingMediaId);
        assertThat(saved.getMedias().getFirst().getStorageKey()).isEqualTo(keepKey);
        assertThat(saved.getMedias().get(1).getStorageKey()).isEqualTo(promotedKey);
        assertThat(result.medias()).hasSize(2);
        assertThat(result.medias().getFirst().id()).isEqualTo(existingMediaId);
        verify(fileStoragePort).copy(stagedKey, promotedKey);
        verify(fileStoragePort, never()).copy(keepKey, keepKey);
    }

    @Test
    void replaceMediaLeavesNonStagedKeysUnchanged() {
        seedProduct("merchant-1", "product-1");
        String existingKey = "merchants/merchant-1/products/product-1/keep.jpg";

        replaceMediaHandler.execute(new ReplaceProductMediaCommand(
                new CommonId("merchant-1"),
                new CommonId("product-1"),
                List.of(new ReplaceProductMediaCommand.Media(
                        null,
                        existingKey,
                        "image/jpeg",
                        0
                ))
        ));

        assertThat(productRepository.getLastSaved().getMedias().getFirst().getStorageKey())
                .isEqualTo(existingKey);
        verify(fileStoragePort, never()).copy(anyString(), anyString());
        verify(fileStoragePort, never()).delete(anyString());
    }

    @Test
    void replaceMediaRejectsAnotherMerchantsStagedKey() {
        seedProduct("merchant-1", "product-1");

        assertThatThrownBy(() -> replaceMediaHandler.execute(new ReplaceProductMediaCommand(
                new CommonId("merchant-1"),
                new CommonId("product-1"),
                List.of(new ReplaceProductMediaCommand.Media(
                        null,
                        "merchants/other-merchant/staged/object-1.jpg",
                        "image/jpeg",
                        0
                ))
        )))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.product.media_patch_invalid"));
        verify(fileStoragePort, never()).copy(anyString(), anyString());
    }

    @Test
    void replaceMediaStillPersistsWhenStagedDeleteFails() {
        seedProduct("merchant-1", "product-1");
        String stagedKey = "merchants/merchant-1/staged/object-1.jpg";
        doThrow(new RuntimeException("delete failed")).when(fileStoragePort).delete(stagedKey);

        replaceMediaHandler.execute(new ReplaceProductMediaCommand(
                new CommonId("merchant-1"),
                new CommonId("product-1"),
                List.of(new ReplaceProductMediaCommand.Media(
                        null,
                        stagedKey,
                        "image/jpeg",
                        0
                ))
        ));

        assertThat(productRepository.getLastSaved().getMedias().getFirst().getStorageKey())
                .isEqualTo("merchants/merchant-1/products/product-1/object-1.jpg");
    }

    private Product seedProduct() {
        return seedProduct(PRODUCT_ID, PRODUCT_ID);
    }

    private Product seedProduct(String merchantId, String productId) {
        Product product = Product.create(
                new CommonId(productId),
                new CommonId(merchantId),
                "Camera",
                new CommonId(CATEGORY_ID),
                null,
                null,
                List.of(new Description(new CommonId("description-1"), "summary", "Summary", "Original body")),
                List.of(new ProductMedia(
                        new CommonId("media-1"),
                        "/images/original.png",
                        "http://localhost:9000/grab-media/images/original.png",
                        "image/png",
                        0
                ))
        );
        productRepository.put(product);
        return product;
    }
}
