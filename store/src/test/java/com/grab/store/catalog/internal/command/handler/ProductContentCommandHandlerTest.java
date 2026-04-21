package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.ProductDescriptionsResult;
import com.grab.store.catalog.internal.command.ProductMediaResult;
import com.grab.store.catalog.internal.command.ReplaceProductDescriptionsCommand;
import com.grab.store.catalog.internal.command.ReplaceProductMediaCommand;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductContentCommandHandlerTest {

    private static final String PRODUCT_ID = "product-content-1";
    private static final String CATEGORY_ID = "category-content-1";

    private InMemoryProductRepositoryTest productRepository;
    private ReplaceProductDescriptionsCommandHandler replaceDescriptionsHandler;
    private ReplaceProductMediaCommandHandler replaceMediaHandler;

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryProductRepositoryTest();
        replaceDescriptionsHandler = new ReplaceProductDescriptionsCommandHandler(productRepository);
        replaceMediaHandler = new ReplaceProductMediaCommandHandler(productRepository);
    }

    @Test
    void replaceDescriptionsRemovesOmittedDescriptionsAndPreservesProvidedId() {
        Product product = seedProduct();
        var existingDescriptionId = product.getDescriptions().getFirst().getId();

        ProductDescriptionsResult result = replaceDescriptionsHandler.handle(new ReplaceProductDescriptionsCommand(
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
    void replaceMediaRemovesOmittedMediaAndPreservesProvidedId() {
        Product product = seedProduct();
        var existingMediaId = product.getMedias().getFirst().getId();

        ProductMediaResult result = replaceMediaHandler.handle(new ReplaceProductMediaCommand(
                new CommonId(PRODUCT_ID),
                List.of(new ReplaceProductMediaCommand.Media(
                        existingMediaId,
                        "IMAGE",
                        "/images/updated.png"
                ))
        ));

        Product saved = productRepository.getLastSaved();
        assertThat(saved.getMedias()).hasSize(1);
        assertThat(saved.getMedias().getFirst().getId()).isEqualTo(existingMediaId);
        assertThat(saved.getMedias().getFirst().getPath()).isEqualTo("/images/updated.png");
        assertThat(result.medias()).hasSize(1);
        assertThat(result.medias().getFirst().id()).isEqualTo(existingMediaId);
    }

    @Test
    void replaceDescriptionsRejectsBlankName() {
        seedProduct();

        assertThatThrownBy(() -> replaceDescriptionsHandler.handle(new ReplaceProductDescriptionsCommand(
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
    void replaceMediaRejectsBlankPath() {
        seedProduct();

        assertThatThrownBy(() -> replaceMediaHandler.handle(new ReplaceProductMediaCommand(
                new CommonId(PRODUCT_ID),
                List.of(new ReplaceProductMediaCommand.Media(
                        null,
                        "IMAGE",
                        " "
                ))
        )))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.product.media_patch_invalid"));
    }

    private Product seedProduct() {
        Product product = Product.create(
                new CommonId(PRODUCT_ID),
                "Camera",
                new CommonId(CATEGORY_ID),
                null,
                null,
                List.of(new Description(new CommonId("description-1"), "summary", "Summary", "Original body")),
                List.of(new ProductMedia(new CommonId("media-1"), "IMAGE", "/images/original.png"))
        );
        productRepository.put(product);
        return product;
    }
}
