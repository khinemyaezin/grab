package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.ProductDescriptionsResult;
import com.grab.store.catalog.internal.command.ReplaceProductDescriptionsCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductContentCommandHandlerTest {

    private static final String PRODUCT_ID = "product-content-1";
    private static final String CATEGORY_ID = "category-content-1";

    private InMemoryProductRepositoryTest productRepository;
    private ReplaceProductDescriptionsCommandHandler replaceDescriptionsHandler;

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryProductRepositoryTest();
        replaceDescriptionsHandler = new ReplaceProductDescriptionsCommandHandler(productRepository);
    }

    @Test
    void replaceDescriptions_removesOmittedDescriptionsAndPreservesProvidedId() {
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

    private Product seedProduct() {
        Product product = Product.create(
                new CommonId(PRODUCT_ID),
                "Camera",
                new CommonId(CATEGORY_ID),
                null,
                null,
                null,
                false,
                false,
                null,
                List.of(new Description(null, "summary", "Summary", "Original body")),
                List.of(new ProductMedia(null, "IMAGE", "/images/original.png"))
        );
        productRepository.put(product);
        return product;
    }
}
