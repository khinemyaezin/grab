package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.repository.jpa.VariantOptionQueryRepository;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.query.GetProductQuery;
import com.grab.store.catalog.internal.query.GetProductResult;
import com.grab.store.catalog.internal.util.StandaloneVariationFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductQueryHandlerTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private VariantOptionQueryRepository variantOptionQueryRepository;
    @Mock
    private CategoryQueryRepository categoryQueryRepository;
    @Mock
    private IdGenerator idGenerator;
    @Mock
    private MatrixKeyGenerator matrixKeyGenerator;

    private GetProductQueryHandler getProductQueryHandler;

    @BeforeEach
    void setUp() {
        getProductQueryHandler = new GetProductQueryHandler(productRepository,
                variantOptionQueryRepository,
                idGenerator,
                categoryQueryRepository,
                matrixKeyGenerator);
    }

    @Test
    public void handle_withStandAloneId_shouldReturnStandAloneProduct() {
        Id productId = new CommonId();
        Id variantId = new CommonId();
        GetProductQuery query = new GetProductQuery(productId.getValue(), productId.getValue());

        when(variantOptionQueryRepository.findAllByUuidIn(anyList()))
                .thenReturn(Collections.emptyList());
        when(idGenerator.convertIdFrom(anyString()))
                .thenAnswer(invocationOnMock ->
                        new CommonId(invocationOnMock.getArgument(0)));

        List<ProductVariation> standAloneVariation = StandaloneVariationFactory.create(idGenerator);
        when(productRepository.find(productId, productId)).thenReturn(Optional.of(
                new Product(
                        productId,
                        productId,
                        "",
                        new CommonId(),
                        null,
                        ProductStatus.ACTIVE,
                        null,
                        null,
                        null,
                        List.of(
                                new ProductVariant(
                                        variantId,
                                        "STANDALONE",
                                        ProductVariantStatus.ACTIVE,
                                        standAloneVariation
                                )
                        )
                )));

        GetProductResult result = getProductQueryHandler.handle(query);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(productId.getValue());
        assertThat(result.variants()).hasSize(1);
        assertThat(result.variants().getFirst().variations()).hasSize(0);
        assertThat(result.variantTypes()).hasSize(0);
    }

    @Test
    public void handle_withNoVariationPersisted_shouldReturnProduct() {
        Id productId = new CommonId();
        Id variantId = new CommonId();
        GetProductQuery query = new GetProductQuery(productId.getValue(), productId.getValue());

        when(variantOptionQueryRepository.findAllByUuidIn(anyList()))
                .thenReturn(Collections.emptyList());
        when(idGenerator.convertIdFrom(anyString()))
                .thenAnswer(invocationOnMock ->
                        new CommonId(invocationOnMock.getArgument(0)));

        List<ProductVariation> customVariation = List.of(
                new ProductVariation(new CommonId(), new CommonId()));

        when(productRepository.find(productId, productId)).thenReturn(Optional.of(
                new Product(
                        productId,
                        productId,
                        "",
                        new CommonId(),
                        null,
                        ProductStatus.ACTIVE,
                        null,
                        null,
                        null,
                        List.of(
                                new ProductVariant(
                                        variantId,
                                        "CUSTOM_VARIATION",
                                        ProductVariantStatus.ACTIVE,
                                        customVariation
                                )
                        )
                )));

        GetProductResult result = getProductQueryHandler.handle(query);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(productId.getValue());
        assertThat(result.variants()).hasSize(1);
        assertThat(result.variantTypes()).hasSize(0);
    }

}
