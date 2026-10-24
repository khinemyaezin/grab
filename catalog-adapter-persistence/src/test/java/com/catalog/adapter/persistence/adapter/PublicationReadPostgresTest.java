package com.catalog.adapter.persistence.adapter;

import com.catalog.adapter.persistence.entity.ProductEntity;
import com.catalog.adapter.persistence.entity.ProductPublicationEntity;
import com.catalog.adapter.persistence.entity.ProductVariantEntity;
import com.catalog.adapter.persistence.repository.ProductPublicationJpaRepository;
import com.catalog.adapter.persistence.repository.ProductVariantJpaRepo;
import com.catalog.adapter.persistence.repository.config.CategoryRepositoryTestConfig;
import com.catalog.application.model.read.PublishedVariantChannelView;
import com.catalog.application.model.read.PublishedProductChannel;
import com.catalog.application.port.outbound.BuyabilityQueryPort;
import com.catalog.domain.valueobject.ProductStatus;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PublicationReadPostgresTest extends CategoryRepositoryTestConfig {

    @Autowired
    private EntityManager entityManager;

    private ProductPublicationJpaRepository publications;
    private PublishedProductChannelQueryAdapter cursorAdapter;
    private BuyabilityQueryAdapter buyabilityAdapter;

    @BeforeEach
    void setUp() {
        JpaRepositoryFactory factory = new JpaRepositoryFactory(entityManager);
        publications = factory.getRepository(ProductPublicationJpaRepository.class);
        ProductVariantJpaRepo variants = factory.getRepository(ProductVariantJpaRepo.class);
        UuidGenerator idGenerator = new UuidGenerator();
        CatalogPersistenceExecutor executor = new CatalogPersistenceExecutor();
        cursorAdapter = new PublishedProductChannelQueryAdapter(publications, executor);
        buyabilityAdapter = new BuyabilityQueryAdapter(variants, publications, idGenerator);
    }

    @Test
    void findAfter_multiplePublishedVariants_returnsDistinctOrderedPairs() {
        ProductEntity firstProduct = product("product-a");
        ProductEntity secondProduct = product("product-b");
        ProductVariantEntity firstVariant = variant(firstProduct, "variant-a");
        ProductVariantEntity secondVariant = variant(firstProduct, "variant-b");
        ProductVariantEntity thirdVariant = variant(secondProduct, "variant-c");
        publish(firstVariant, "channel-1");
        publish(secondVariant, "channel-1");
        publish(firstVariant, "channel-2");
        publish(thirdVariant, "channel-1");
        flushAndClear();

        List<PublishedProductChannel> firstPage = cursorAdapter.findAfter(null, null, 2);
        PublishedProductChannel cursor = firstPage.getLast();
        List<PublishedProductChannel> secondPage = cursorAdapter.findAfter(
                cursor.productId(), cursor.salesChannelId(), 2
        );

        Id firstProductId = new CommonId("product-a");
        Id secondProductId = new CommonId("product-b");
        Id firstChannelId = new CommonId("channel-1");
        Id secondChannelId = new CommonId("channel-2");
        PublishedProductChannel firstPair = new PublishedProductChannel(firstProductId, firstChannelId);
        PublishedProductChannel secondPair = new PublishedProductChannel(firstProductId, secondChannelId);
        PublishedProductChannel thirdPair = new PublishedProductChannel(secondProductId, firstChannelId);
        assertThat(firstPage).containsExactly(firstPair, secondPair);
        assertThat(secondPage).containsExactly(thirdPair);
    }

    @Test
    void findAllPublicationVariantUuids_multipleChannels_preservesVariantAndChannelIds() {
        ProductEntity product = product("product-a");
        ProductVariantEntity variant = variant(product, "variant-a");
        publish(variant, "channel-1");
        publish(variant, "channel-2");
        flushAndClear();

        List<PublishedVariantChannelView> rows = publications.findAllPublicationVariantUuids();
        List<BuyabilityQueryPort.PublicationSlice> slices = buyabilityAdapter.listPublications();

        PublishedVariantChannelView first = new PublishedVariantChannelView("variant-a", "channel-1");
        PublishedVariantChannelView second = new PublishedVariantChannelView("variant-a", "channel-2");
        assertThat(rows).containsExactlyInAnyOrder(first, second);
        assertThat(slices).extracting(BuyabilityQueryPort.PublicationSlice::variantId)
                .containsOnly("variant-a");
        assertThat(buyabilityAdapter.isPublished("variant-a", "channel-1")).isTrue();
        assertThat(buyabilityAdapter.isPublished("variant-a", "other-channel")).isFalse();
        assertThat(buyabilityAdapter.isPublished("missing-variant", "channel-1")).isFalse();
        assertThat(buyabilityAdapter.salesChannelIdsForVariant("variant-a"))
                .containsExactlyInAnyOrder("channel-1", "channel-2");
        assertThat(buyabilityAdapter.salesChannelIdsForVariant("missing-variant")).isEmpty();
    }

    private ProductEntity product(String productId) {
        ProductEntity product = new ProductEntity();
        product.setUuid(productId);
        product.setName(productId);
        product.setMerchantId("merchant-1");
        product.setCategoryId("category-1");
        product.setStatus(ProductStatus.ACTIVE);
        entityManager.persist(product);
        return product;
    }

    private ProductVariantEntity variant(ProductEntity product, String variantId) {
        ProductVariantEntity variant = new ProductVariantEntity();
        variant.setUuid(variantId);
        variant.setSku(variantId);
        variant.setMerchantId("merchant-1");
        variant.setStatus("ACTIVE");
        variant.setProduct(product);
        entityManager.persist(variant);
        return variant;
    }

    private void publish(ProductVariantEntity variant, String channelId) {
        ProductPublicationEntity publication = new ProductPublicationEntity();
        publication.setVariantId(variant.getId());
        publication.setSalesChannelId(channelId);
        entityManager.persist(publication);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
