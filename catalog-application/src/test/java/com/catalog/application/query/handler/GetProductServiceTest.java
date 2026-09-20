package com.catalog.application.query.handler;

import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.port.outbound.VariantOptionQueryPort;
import com.catalog.application.model.read.GetProductQuery;
import com.catalog.application.model.read.GetProductResult;
import com.catalog.application.service.ProductMediaConverter;
import com.catalog.application.model.read.ProductDetailView;
import com.catalog.application.model.read.ProductPublicationView;
import com.catalog.application.service.GetProductService;
import com.catalog.application.service.StandaloneVariationFactory;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.valueobject.ProductStatus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.storage.FileStoragePort;
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
class GetProductServiceTest {
    @Mock
    private ProductQueryPort productQueryPort;
    @Mock
    private VariantOptionQueryPort variantOptionQueryPort;
    @Mock
    private CategoryQueryPort categoryQueryPort;
    @Mock
    private IdGenerator idGenerator;
    @Mock
    private MatrixKeyGenerator matrixKeyGenerator;
    @Mock
    private FileStoragePort fileStoragePort;

    private GetProductService getProductService;

    @BeforeEach
    void setUp() {
        getProductService = new GetProductService(
                productQueryPort,
                variantOptionQueryPort,
                idGenerator,
                categoryQueryPort,
                matrixKeyGenerator,
                new ProductMediaConverter(fileStoragePort));
    }

    @Test
    public void handle_withStandAloneId_shouldReturnStandAloneProduct() {
        Id productId = new CommonId();
        Id variantId = new CommonId();
        GetProductQuery query = new GetProductQuery(productId.getValue(), productId.getValue());

        when(variantOptionQueryPort.findAllByUuidIn(anyList()))
                .thenReturn(Collections.emptyList());
        when(idGenerator.convertIdFrom(anyString()))
                .thenAnswer(invocationOnMock ->
                        new CommonId(invocationOnMock.getArgument(0)));
        when(matrixKeyGenerator.generateKey(anyList())).thenReturn("standalone");

        when(productQueryPort.findDetailByIdAndMerchantId(productId.getValue(), productId.getValue()))
                .thenReturn(Optional.of(minimalDetail(
                        productId.getValue(),
                        variantId.getValue(),
                        "STANDALONE",
                        List.of(new ProductDetailView.VariationView(
                                StandaloneVariationFactory.OPTION_ID,
                                StandaloneVariationFactory.TYPE_ID
                        ))
                )));

        GetProductResult result = getProductService.execute(query);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(productId.getValue());
        assertThat(result.descriptions()).isEmpty();
        assertThat(result.medias()).isEmpty();
        assertThat(result.variants()).hasSize(1);
        assertThat(result.variants().getFirst().variations()).hasSize(0);
        assertThat(result.variants().getFirst().mediaIds()).isEmpty();
        assertThat(result.variants().getFirst().thumbnailMediaId()).isNull();
        assertThat(result.variantTypes()).hasSize(0);
    }

    @Test
    public void handle_withNoVariationPersisted_shouldReturnProduct() {
        Id productId = new CommonId();
        Id variantId = new CommonId();
        GetProductQuery query = new GetProductQuery(productId.getValue(), productId.getValue());

        when(variantOptionQueryPort.findAllByUuidIn(anyList()))
                .thenReturn(Collections.emptyList());
        when(idGenerator.convertIdFrom(anyString()))
                .thenAnswer(invocationOnMock ->
                        new CommonId(invocationOnMock.getArgument(0)));
        when(matrixKeyGenerator.generateKey(anyList())).thenReturn("custom");

        when(productQueryPort.findDetailByIdAndMerchantId(productId.getValue(), productId.getValue()))
                .thenReturn(Optional.of(minimalDetail(
                        productId.getValue(),
                        variantId.getValue(),
                        "CUSTOM_VARIATION",
                        List.of(new ProductDetailView.VariationView("opt-1", "type-1"))
                )));

        GetProductResult result = getProductService.execute(query);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(productId.getValue());
        assertThat(result.medias()).isEmpty();
        assertThat(result.variants()).hasSize(1);
        assertThat(result.variantTypes()).hasSize(0);
    }

    @Test
    public void handle_withMedias_shouldReturnRankedGalleryWithResolvedUrls() {
        Id productId = new CommonId("prod-1");
        Id variantId = new CommonId("var-1");
        GetProductQuery query = new GetProductQuery(productId.getValue(), productId.getValue());
        String secondaryKey = "merchants/m/products/prod-1/side.jpg";
        String heroKey = "merchants/m/products/prod-1/hero.jpg";

        when(variantOptionQueryPort.findAllByUuidIn(anyList()))
                .thenReturn(Collections.emptyList());
        when(idGenerator.convertIdFrom(anyString()))
                .thenAnswer(invocationOnMock ->
                        new CommonId(invocationOnMock.getArgument(0)));
        when(fileStoragePort.resolvePublicUrl(anyString())).thenAnswer(invocation ->
                "http://localhost:8333/grab-media/" + invocation.getArgument(0));
        when(matrixKeyGenerator.generateKey(anyList())).thenReturn("standalone");

        when(productQueryPort.findDetailByIdAndMerchantId(productId.getValue(), productId.getValue()))
                .thenReturn(Optional.of(new ProductDetailView(
                        productId.getValue(),
                        "Shirt",
                        productId.getValue(),
                        ProductStatus.ACTIVE,
                        "shirt",
                        null,
                        null,
                        false,
                        List.of(),
                        List.of(
                                new ProductDetailView.MediaView(
                                        "media-2",
                                        secondaryKey,
                                        null,
                                        "image/jpeg",
                                        1
                                ),
                                new ProductDetailView.MediaView(
                                        "media-1",
                                        heroKey,
                                        null,
                                        "image/jpeg",
                                        0
                                )
                        ),
                        List.of(new ProductDetailView.VariantView(
                                variantId.getValue(),
                                "STANDALONE",
                                "ACTIVE",
                                true,
                                List.of("media-1"),
                                "media-1",
                                List.of(new ProductDetailView.VariationView(
                                        StandaloneVariationFactory.OPTION_ID,
                                        StandaloneVariationFactory.TYPE_ID
                                ))
                        ))
                )));

        GetProductResult result = getProductService.execute(query);

        assertThat(result.descriptions()).isEmpty();
        assertThat(result.medias()).containsExactly(
                new GetProductResult.Media(
                        "media-1",
                        heroKey,
                        "http://localhost:8333/grab-media/" + heroKey,
                        "image/jpeg",
                        0
                ),
                new GetProductResult.Media(
                        "media-2",
                        secondaryKey,
                        "http://localhost:8333/grab-media/" + secondaryKey,
                        "image/jpeg",
                        1
                )
        );
        assertThat(result.variants()).hasSize(1);
        assertThat(result.variants().getFirst().mediaIds()).containsExactly("media-1");
        assertThat(result.variants().getFirst().thumbnailMediaId()).isEqualTo("media-1");
    }

    @Test
    public void handle_withDescriptions_shouldReturnDescriptionSections() {
        Id productId = new CommonId("prod-1");
        Id variantId = new CommonId("var-1");
        GetProductQuery query = new GetProductQuery(productId.getValue(), productId.getValue());

        when(variantOptionQueryPort.findAllByUuidIn(anyList()))
                .thenReturn(Collections.emptyList());
        when(idGenerator.convertIdFrom(anyString()))
                .thenAnswer(invocationOnMock ->
                        new CommonId(invocationOnMock.getArgument(0)));
        when(matrixKeyGenerator.generateKey(anyList())).thenReturn("standalone");

        when(productQueryPort.findDetailByIdAndMerchantId(productId.getValue(), productId.getValue()))
                .thenReturn(Optional.of(new ProductDetailView(
                        productId.getValue(),
                        "Shirt",
                        productId.getValue(),
                        ProductStatus.ACTIVE,
                        "shirt",
                        null,
                        null,
                        false,
                        List.of(new ProductDetailView.DescriptionView(
                                "desc-1",
                                "overview",
                                "Overview",
                                "Soft cotton shirt"
                        )),
                        List.of(),
                        List.of(new ProductDetailView.VariantView(
                                variantId.getValue(),
                                "STANDALONE",
                                "ACTIVE",
                                true,
                                List.of(),
                                null,
                                List.of(new ProductDetailView.VariationView(
                                        StandaloneVariationFactory.OPTION_ID,
                                        StandaloneVariationFactory.TYPE_ID
                                ))
                        ))
                )));

        GetProductResult result = getProductService.execute(query);

        assertThat(result.descriptions()).containsExactly(
                new GetProductResult.Description(
                        "desc-1",
                        "overview",
                        "Overview",
                        "Soft cotton shirt"
                )
        );
    }

    @Test
    public void handle_nestsPublicationsOnMatchingVariants() {
        Id productId = new CommonId("prod-1");
        Id variantId = new CommonId("var-1");
        GetProductQuery query = new GetProductQuery(productId.getValue(), productId.getValue());

        when(variantOptionQueryPort.findAllByUuidIn(anyList()))
                .thenReturn(Collections.emptyList());
        when(idGenerator.convertIdFrom(anyString()))
                .thenAnswer(invocationOnMock ->
                        new CommonId(invocationOnMock.getArgument(0)));
        when(matrixKeyGenerator.generateKey(anyList())).thenReturn("standalone");
        when(productQueryPort.findPublicationsByProductIds(List.of(productId.getValue())))
                .thenReturn(List.of(
                        new ProductPublicationView(
                                productId.getValue(),
                                variantId.getValue(),
                                "channel-1"
                        )
                ));

        when(productQueryPort.findDetailByIdAndMerchantId(productId.getValue(), productId.getValue()))
                .thenReturn(Optional.of(minimalDetail(
                        productId.getValue(),
                        variantId.getValue(),
                        "STANDALONE",
                        List.of(new ProductDetailView.VariationView(
                                StandaloneVariationFactory.OPTION_ID,
                                StandaloneVariationFactory.TYPE_ID
                        ))
                )));

        GetProductResult result = getProductService.execute(query);

        assertThat(result.variants()).hasSize(1);
        assertThat(result.variants().getFirst().publications())
                .containsExactly(new GetProductResult.Publication("channel-1"));
    }

    private ProductDetailView minimalDetail(
            String productId,
            String variantId,
            String sku,
            List<ProductDetailView.VariationView> variations
    ) {
        return new ProductDetailView(
                productId,
                "",
                productId,
                ProductStatus.ACTIVE,
                null,
                null,
                null,
                false,
                List.of(),
                List.of(),
                List.of(new ProductDetailView.VariantView(
                        variantId,
                        sku,
                        "ACTIVE",
                        true,
                        List.of(),
                        null,
                        variations
                ))
        );
    }
}
