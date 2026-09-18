package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.repository.jpa.ProductQueryRepository;
import com.catalog.infrastructure.repository.jpa.VariantOptionQueryRepository;
import com.catalog.infrastructure.view.ProductPublicationView;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.storage.FileStoragePort;
import com.grab.store.catalog.internal.query.GetProductQuery;
import com.grab.store.catalog.internal.query.GetProductResult;
import com.grab.store.catalog.internal.query.ProductMediaQueryMapper;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductQueryHandlerTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductQueryRepository productQueryRepository;
    @Mock
    private VariantOptionQueryRepository variantOptionQueryRepository;
    @Mock
    private CategoryQueryRepository categoryQueryRepository;
    @Mock
    private IdGenerator idGenerator;
    @Mock
    private MatrixKeyGenerator matrixKeyGenerator;
    @Mock
    private FileStoragePort fileStoragePort;

    private GetProductQueryHandler getProductQueryHandler;

    @BeforeEach
    void setUp() {
        getProductQueryHandler = new GetProductQueryHandler(productRepository,
                productQueryRepository,
                variantOptionQueryRepository,
                idGenerator,
                categoryQueryRepository,
                matrixKeyGenerator,
                new ProductMediaQueryMapper(fileStoragePort));
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
                                        standAloneVariation,
                                        true,
                                        List.of(),
                                        null
                                )
                        )
                )));

        GetProductResult result = getProductQueryHandler.handle(query);

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
                                        customVariation,
                                        true,
                                        List.of(),
                                        null
                                )
                        )
                )));

        GetProductResult result = getProductQueryHandler.handle(query);

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

        when(variantOptionQueryRepository.findAllByUuidIn(anyList()))
                .thenReturn(Collections.emptyList());
        when(idGenerator.convertIdFrom(anyString()))
                .thenAnswer(invocationOnMock ->
                        new CommonId(invocationOnMock.getArgument(0)));
        when(fileStoragePort.resolvePublicUrl(anyString())).thenAnswer(invocation ->
                "http://localhost:8333/grab-media/" + invocation.getArgument(0));

        List<ProductVariation> standAloneVariation = StandaloneVariationFactory.create(idGenerator);
        when(productRepository.find(productId, productId)).thenReturn(Optional.of(
                new Product(
                        productId,
                        productId,
                        "Shirt",
                        new CommonId(),
                        null,
                        ProductStatus.ACTIVE,
                        null,
                        null,
                        List.of(
                                new ProductMedia(
                                        new CommonId("media-2"),
                                        secondaryKey,
                                        "stale-url",
                                        "image/jpeg",
                                        1
                                ),
                                new ProductMedia(
                                        new CommonId("media-1"),
                                        heroKey,
                                        "stale-url",
                                        "image/jpeg",
                                        0
                                )
                        ),
                        List.of(
                                new ProductVariant(
                                        variantId,
                                        "STANDALONE",
                                        ProductVariantStatus.ACTIVE,
                                        standAloneVariation,
                                        true,
                                        List.of(new CommonId("media-1")),
                                        new CommonId("media-1")
                                )
                        )
                )));

        GetProductResult result = getProductQueryHandler.handle(query);

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
                        "Shirt",
                        new CommonId(),
                        null,
                        ProductStatus.ACTIVE,
                        null,
                        List.of(
                                new Description(
                                        new CommonId("desc-1"),
                                        "overview",
                                        "Overview",
                                        "Soft cotton shirt"
                                )
                        ),
                        null,
                        List.of(
                                new ProductVariant(
                                        variantId,
                                        "STANDALONE",
                                        ProductVariantStatus.ACTIVE,
                                        standAloneVariation,
                                        true,
                                        List.of(),
                                        null
                                )
                        )
                )));

        GetProductResult result = getProductQueryHandler.handle(query);

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

        when(variantOptionQueryRepository.findAllByUuidIn(anyList()))
                .thenReturn(Collections.emptyList());
        when(idGenerator.convertIdFrom(anyString()))
                .thenAnswer(invocationOnMock ->
                        new CommonId(invocationOnMock.getArgument(0)));
        when(productQueryRepository.findPublicationsByProductIds(List.of(productId.getValue())))
                .thenReturn(List.of(
                        new ProductPublicationView(
                                productId.getValue(),
                                variantId.getValue(),
                                "channel-1"
                        )
                ));

        List<ProductVariation> standAloneVariation = StandaloneVariationFactory.create(idGenerator);
        when(productRepository.find(productId, productId)).thenReturn(Optional.of(
                new Product(
                        productId,
                        productId,
                        "Shirt",
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
                                        standAloneVariation,
                                        true,
                                        List.of(),
                                        null
                                )
                        )
                )));

        GetProductResult result = getProductQueryHandler.handle(query);

        assertThat(result.variants()).hasSize(1);
        assertThat(result.variants().getFirst().publications())
                .containsExactly(new GetProductResult.Publication("channel-1"));
    }

}
