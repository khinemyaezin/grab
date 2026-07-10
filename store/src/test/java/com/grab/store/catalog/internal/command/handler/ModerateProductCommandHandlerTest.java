package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.valueobject.ListingCondition;
import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.ModerateProductCommand;
import com.grab.store.catalog.internal.command.ModerateProductResult;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModerateProductCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private ModerateProductCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ModerateProductCommandHandler(productRepository, categoryRepository);
    }

    @Test
    void handle_publishMovesProductActive() {
        Product product = draftProduct(id("product-2"), id("category-1"));
        product.addVariant(ProductVariant.create(
                id("variant-" + product.getId().getValue()),
                "SKU-" + product.getId().getValue(),
                List.of(new ProductVariation(id("opt-red"), id("type-color")))
        ));
        Category category = Category.createRoot(id("category-1"), "Category");

        when(productRepository.find(product.getId())).thenReturn(Optional.of(product));
        when(categoryRepository.find(product.getCategoryId())).thenReturn(Optional.of(category));

        ModerateProductResult result = handler.handle(new ModerateProductCommand(product.getId(), "PUBLISH", null));

        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(result.action()).isEqualTo("PUBLISH");
        assertThat(result.oldStatus()).isEqualTo(ProductStatus.DRAFT.name());
        assertThat(result.newStatus()).isEqualTo(ProductStatus.ACTIVE.name());
    }

    @Test
    void handle_suspendAndRestoreRoundTripsModerationLifecycle() {
        Product product = activeProduct(id("product-4"), id("category-1"));
        Category category = Category.createRoot(id("category-1"), "Category");

        when(productRepository.find(product.getId())).thenReturn(Optional.of(product));
        when(categoryRepository.find(product.getCategoryId())).thenReturn(Optional.of(category));

        ModerateProductResult suspendResult = handler.handle(new ModerateProductCommand(product.getId(), "SUSPEND", "Policy violation"));
        assertThat(suspendResult.newStatus()).isEqualTo(ProductStatus.SUSPENDED.name());

        ModerateProductResult restoreResult = handler.handle(new ModerateProductCommand(product.getId(), "RESTORE", null));
        assertThat(restoreResult.oldStatus()).isEqualTo(ProductStatus.SUSPENDED.name());
        assertThat(restoreResult.newStatus()).isEqualTo(ProductStatus.DRAFT.name());
    }

    @Test
    void handle_missingProductThrows() {
        Id productId = id("missing-product");
        when(productRepository.find(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new ModerateProductCommand(productId, "PUBLISH", null)))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> assertThat(((CatalogServiceException) exception).getMessageSource().code())
                        .isEqualTo("cat.service.product.not_found"));
    }

    private Product draftProduct(Id productId, Id categoryId) {
        return Product.create(
                productId,
                productId,
                "Product",
                categoryId,
                ListingCondition.USED,
                "product",
                List.of(new Description(null, "default", "Product", "Description")),
                List.of(new ProductMedia(null, "IMAGE", "/images/product.jpg"))
        );
    }

    private Product activeProduct(Id productId, Id categoryId) {
        Product product = draftProduct(productId, categoryId);
        product.addVariant(ProductVariant.create(
                id("variant-" + productId.getValue()),
                "SKU-" + productId.getValue(),
                List.of(new ProductVariation(id("opt-red"), id("type-color")))
        ));
        product.publish();
        product.pullEvents();
        return product;
    }

    private Id id(String value) {
        return new CommonId(value);
    }
}
