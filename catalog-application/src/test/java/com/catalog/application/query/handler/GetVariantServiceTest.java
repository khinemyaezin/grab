package com.catalog.application.query.handler;

import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.port.outbound.VariantOptionQueryPort;
import com.catalog.application.model.read.GetVariantQuery;
import com.catalog.application.model.read.GetVariantResult;
import com.catalog.application.model.read.ProductDetailView;
import com.catalog.application.model.read.VariantOptionView;
import com.catalog.application.service.GetVariantService;
import com.catalog.application.service.StandaloneVariationFactory;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetVariantServiceTest {

    private static final String PRODUCT_ID = "product-123";
    private static final String CATEGORY_ID = "category-456";
    private static final String VARIANT_ID = "variant-789";
    private static final String MATRIX_KEY = "color:red";

    @Mock
    private ProductQueryPort productQueryPort;
    @Mock
    private VariantOptionQueryPort variantOptionQueryPort;
    @Mock
    private IdGenerator idGenerator;
    @Mock
    private MatrixKeyGenerator matrixKeyGenerator;

    private GetVariantService service;

    @BeforeEach
    void setUp() {
        service = new GetVariantService(
                productQueryPort,
                variantOptionQueryPort,
                idGenerator,
                matrixKeyGenerator
        );
        when(idGenerator.convertIdFrom(anyString()))
                .thenAnswer(invocation -> new CommonId(invocation.getArgument(0)));
    }

    @Test
    void handle_returnsNamedVariations() {
        when(productQueryPort.findDetailByIdAndMerchantId(PRODUCT_ID, PRODUCT_ID))
                .thenReturn(Optional.of(productDetail(
                        List.of(new ProductDetailView.VariantView(
                                VARIANT_ID,
                                "TSHIRT-RED-L",
                                ProductVariantStatus.ACTIVE.name(),
                                true,
                                List.of(),
                                null,
                                List.of(new ProductDetailView.VariationView("opt-red", "type-color"))
                        ))
                )));
        when(variantOptionQueryPort.findAllByUuidIn(List.of("opt-red")))
                .thenReturn(List.of(new VariantOptionView("opt-red", "Red", "type-color", "Color")));
        when(matrixKeyGenerator.generateKey(anyList())).thenReturn(MATRIX_KEY);

        GetVariantResult result = service.execute(new GetVariantQuery(PRODUCT_ID, PRODUCT_ID, VARIANT_ID));

        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.productName()).isEqualTo("Classic T-Shirt");
        assertThat(result.variantId()).isEqualTo(VARIANT_ID);
        assertThat(result.sku()).isEqualTo("TSHIRT-RED-L");
        assertThat(result.status()).isEqualTo(ProductVariantStatus.ACTIVE.name());
        assertThat(result.matrixKey()).isEqualTo(MATRIX_KEY);
        assertThat(result.manageInventory()).isTrue();
        assertThat(result.mediaIds()).isEmpty();
        assertThat(result.thumbnailMediaId()).isNull();
        assertThat(result.variations()).containsExactly(
                new GetVariantResult.Variation("opt-red", "Red", "type-color", "Color")
        );
    }

    @Test
    void handle_standaloneVariant_returnsEmptyVariations() {
        when(productQueryPort.findDetailByIdAndMerchantId(PRODUCT_ID, PRODUCT_ID))
                .thenReturn(Optional.of(productDetail(
                        List.of(new ProductDetailView.VariantView(
                                VARIANT_ID,
                                "STANDALONE",
                                ProductVariantStatus.ACTIVE.name(),
                                true,
                                List.of(),
                                null,
                                List.of(new ProductDetailView.VariationView(
                                        StandaloneVariationFactory.OPTION_ID,
                                        StandaloneVariationFactory.TYPE_ID
                                ))
                        ))
                )));
        when(variantOptionQueryPort.findAllByUuidIn(anyList())).thenReturn(Collections.emptyList());
        when(matrixKeyGenerator.generateKey(anyList())).thenReturn("standalone");

        GetVariantResult result = service.execute(new GetVariantQuery(PRODUCT_ID, PRODUCT_ID, VARIANT_ID));

        assertThat(result.sku()).isEqualTo("STANDALONE");
        assertThat(result.variations()).isEmpty();
    }

    @Test
    void handle_productNotFound_throws() {
        when(productQueryPort.findDetailByIdAndMerchantId(PRODUCT_ID, PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetVariantQuery(PRODUCT_ID, PRODUCT_ID, VARIANT_ID)))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.product.not_found");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.NOT_FOUND);
                });
    }

    @Test
    void handle_variantNotFound_throws() {
        when(productQueryPort.findDetailByIdAndMerchantId(PRODUCT_ID, PRODUCT_ID))
                .thenReturn(Optional.of(productDetail(List.of())));

        assertThatThrownBy(() -> service.execute(new GetVariantQuery(PRODUCT_ID, PRODUCT_ID, VARIANT_ID)))
                .isInstanceOf(CatalogServiceException.class)
                .satisfies(exception -> {
                    CatalogServiceException typed = (CatalogServiceException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.service.variant.not_found");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.NOT_FOUND);
                });
    }

    @Test
    void handle_deletedVariant_isReturned() {
        when(productQueryPort.findDetailByIdAndMerchantId(PRODUCT_ID, PRODUCT_ID))
                .thenReturn(Optional.of(productDetail(
                        List.of(new ProductDetailView.VariantView(
                                VARIANT_ID,
                                "TSHIRT-RED-L",
                                ProductVariantStatus.DELETED.name(),
                                true,
                                List.of(),
                                null,
                                List.of(new ProductDetailView.VariationView("opt-red", "type-color"))
                        ))
                )));
        when(variantOptionQueryPort.findAllByUuidIn(anyList())).thenReturn(Collections.emptyList());
        when(matrixKeyGenerator.generateKey(anyList())).thenReturn(MATRIX_KEY);

        GetVariantResult result = service.execute(new GetVariantQuery(PRODUCT_ID, PRODUCT_ID, VARIANT_ID));

        assertThat(result.status()).isEqualTo(ProductVariantStatus.DELETED.name());
        assertThat(result.variantId()).isEqualTo(VARIANT_ID);
    }

    @Test
    void handle_returnsVariantMediaIds() {
        when(productQueryPort.findDetailByIdAndMerchantId(PRODUCT_ID, PRODUCT_ID))
                .thenReturn(Optional.of(productDetail(
                        List.of(new ProductDetailView.VariantView(
                                VARIANT_ID,
                                "TSHIRT-RED-L",
                                ProductVariantStatus.ACTIVE.name(),
                                true,
                                List.of("media-1", "media-2"),
                                "media-2",
                                List.of(new ProductDetailView.VariationView("opt-red", "type-color"))
                        ))
                )));
        when(variantOptionQueryPort.findAllByUuidIn(List.of("opt-red")))
                .thenReturn(List.of(new VariantOptionView("opt-red", "Red", "type-color", "Color")));
        when(matrixKeyGenerator.generateKey(anyList())).thenReturn(MATRIX_KEY);

        GetVariantResult result = service.execute(new GetVariantQuery(PRODUCT_ID, PRODUCT_ID, VARIANT_ID));

        assertThat(result.mediaIds()).containsExactly("media-1", "media-2");
        assertThat(result.thumbnailMediaId()).isEqualTo("media-2");
    }

    private ProductDetailView productDetail(List<ProductDetailView.VariantView> variants) {
        return new ProductDetailView(
                PRODUCT_ID,
                "Classic T-Shirt",
                PRODUCT_ID,
                ProductStatus.ACTIVE,
                "classic-t-shirt",
                null,
                CATEGORY_ID,
                false,
                List.of(),
                List.of(),
                variants
        );
    }
}
