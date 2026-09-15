package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.infrastructure.repository.jpa.VariantOptionQueryRepository;
import com.catalog.infrastructure.view.VariantOptionView;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.query.GetVariantQuery;
import com.grab.store.catalog.internal.query.GetVariantResult;
import com.grab.store.catalog.internal.service.StandaloneVariationFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetVariantQueryHandlerTest {

    private static final String PRODUCT_ID = "product-123";
    private static final String CATEGORY_ID = "category-456";
    private static final String VARIANT_ID = "variant-789";
    private static final String MATRIX_KEY = "color:red";

    @Mock
    private ProductRepository productRepository;
    @Mock
    private VariantOptionQueryRepository variantOptionQueryRepository;
    @Mock
    private IdGenerator idGenerator;
    @Mock
    private MatrixKeyGenerator matrixKeyGenerator;

    private GetVariantQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetVariantQueryHandler(
                productRepository,
                variantOptionQueryRepository,
                idGenerator,
                matrixKeyGenerator
        );
        when(idGenerator.convertIdFrom(anyString()))
                .thenAnswer(invocation -> new CommonId(invocation.getArgument(0)));
    }

    @Test
    void handle_returnsNamedVariations() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);
        ProductVariation variation = new ProductVariation(
                new CommonId("opt-red"), new CommonId("type-color"));
        Product product = productWithVariant(
                ProductVariant.create(variantId, "TSHIRT-RED-L", List.of(variation), true)
        );

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));
        when(variantOptionQueryRepository.findAllByUuidIn(List.of("opt-red")))
                .thenReturn(List.of(new VariantOptionView("opt-red", "Red", "type-color", "Color")));
        when(matrixKeyGenerator.generateKey(anyList())).thenReturn(MATRIX_KEY);

        GetVariantResult result = handler.handle(new GetVariantQuery(PRODUCT_ID, PRODUCT_ID, VARIANT_ID));

        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.productName()).isEqualTo("Classic T-Shirt");
        assertThat(result.variantId()).isEqualTo(VARIANT_ID);
        assertThat(result.sku()).isEqualTo("TSHIRT-RED-L");
        assertThat(result.status()).isEqualTo(ProductVariantStatus.ACTIVE.name());
        assertThat(result.matrixKey()).isEqualTo(MATRIX_KEY);
        assertThat(result.manageInventory()).isTrue();
        assertThat(result.variations()).containsExactly(
                new GetVariantResult.Variation("opt-red", "Red", "type-color", "Color")
        );
    }

    @Test
    void handle_standaloneVariant_returnsEmptyVariations() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);
        List<ProductVariation> standAloneVariation = StandaloneVariationFactory.create(idGenerator);
        Product product = productWithVariant(
                ProductVariant.create(variantId, "STANDALONE", standAloneVariation)
        );

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));
        when(variantOptionQueryRepository.findAllByUuidIn(anyList())).thenReturn(Collections.emptyList());
        when(matrixKeyGenerator.generateKey(anyList())).thenReturn("standalone");

        GetVariantResult result = handler.handle(new GetVariantQuery(PRODUCT_ID, PRODUCT_ID, VARIANT_ID));

        assertThat(result.sku()).isEqualTo("STANDALONE");
        assertThat(result.variations()).isEmpty();
    }

    @Test
    void handle_productNotFound_throws() {
        Id productId = new CommonId(PRODUCT_ID);
        when(productRepository.find(productId, productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetVariantQuery(PRODUCT_ID, PRODUCT_ID, VARIANT_ID)))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.product.not_found");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.NOT_FOUND);
                });
    }

    @Test
    void handle_variantNotFound_throws() {
        Id productId = new CommonId(PRODUCT_ID);
        Product product = Product.create(productId, productId, "Classic T-Shirt", new CommonId(CATEGORY_ID));
        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> handler.handle(new GetVariantQuery(PRODUCT_ID, PRODUCT_ID, VARIANT_ID)))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.variant.not_found");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.NOT_FOUND);
                });
    }

    @Test
    void handle_deletedVariant_isReturned() {
        Id productId = new CommonId(PRODUCT_ID);
        Id variantId = new CommonId(VARIANT_ID);
        ProductVariation variation = new ProductVariation(
                new CommonId("opt-red"), new CommonId("type-color"));
        ProductVariant variant = ProductVariant.create(variantId, "TSHIRT-RED-L", List.of(variation));
        variant.markAsDeleted();
        Product product = productWithVariant(variant);

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(product));
        when(variantOptionQueryRepository.findAllByUuidIn(anyList())).thenReturn(Collections.emptyList());
        when(matrixKeyGenerator.generateKey(anyList())).thenReturn(MATRIX_KEY);

        GetVariantResult result = handler.handle(new GetVariantQuery(PRODUCT_ID, PRODUCT_ID, VARIANT_ID));

        assertThat(result.status()).isEqualTo(ProductVariantStatus.DELETED.name());
        assertThat(result.variantId()).isEqualTo(VARIANT_ID);
    }

    private Product productWithVariant(ProductVariant variant) {
        Id productId = new CommonId(PRODUCT_ID);
        Product product = Product.create(productId, productId, "Classic T-Shirt", new CommonId(CATEGORY_ID));
        product.addVariant(variant);
        return product;
    }
}
