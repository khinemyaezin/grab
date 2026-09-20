package com.catalog.adapter.persistence.specification;

import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.adapter.persistence.entity.*;
import com.catalog.adapter.persistence.repository.config.ProductRepositoryTestConfig;
import com.catalog.application.model.read.ProductSearchCriteria;
import com.catalog.application.model.read.ProductHeroMediaView;
import com.catalog.application.model.read.ProductPublicationView;
import com.catalog.application.model.read.ProductVariantRefView;
import com.catalog.application.model.read.ProductView;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductSearchSpecificationTest extends ProductRepositoryTestConfig {

    private static final String MERCHANT_ID = "merchant-1";

    @Autowired
    private EntityManager entityManager;

    private ProductSearchSpecification specification;

    @BeforeEach
    void setUp() {
        specification = new ProductSearchSpecification(entityManager);

        persistProduct("Premium Cotton T-Shirt", "cat-1",
                variant("TSH-RED-XS", "ACTIVE",
                        variation("opt-red-1", "type-color-1", "Red", "Color"),
                        variation("opt-xs-1", "type-size-1", "XS", "Size")),
                variant("TSH-BLUE-L", "ACTIVE",
                        variation("opt-blue-1", "type-color-2", "Blue", "Color"),
                        variation("opt-l-1", "type-size-2", "L", "Size")),
                variant("TSH-RED-L", "DELETED",
                        variation("opt-red-2", "type-color-3", "Red", "Color"),
                        variation("opt-l-2", "type-size-3", "L", "Size"))
        );

        persistProduct("Slim Fit Jeans", "cat-2",
                variant("JNS-BLACK-M", "ACTIVE",
                        variation("opt-black-1", "type-color-4", "Black", "Color"),
                        variation("opt-m-1", "type-size-4", "M", "Size"))
        );

        persistProduct("Running Shoes", "cat-3",
                variant("SHO-WHITE-42", "ACTIVE",
                        variation("opt-white-1", "type-color-5", "White", "Color"),
                        variation("opt-42-1", "type-shoesize-1", "42", "Shoe Size"))
        );

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void search_withMerchantOnly_returnAllProductsForMerchant() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .merchantId(MERCHANT_ID)
                .build();

        Page<ProductView> page = specification.search(criteria, PageRequest.of(0, 10));

        assertEquals(3, page.getTotalElements());
        assertEquals(3, page.getContent().size());
    }

    @Test
    void search_withQueryMatchingProductName_returnMatchedProduct() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .merchantId(MERCHANT_ID)
                .query("cotton")
                .build();

        Page<ProductView> page = specification.search(criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Premium Cotton T-Shirt", page.getContent().getFirst().name());
    }

    @Test
    void search_withQueryMatchingSku_returnMatchedProduct() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .merchantId(MERCHANT_ID)
                .query("JNS")
                .build();

        Page<ProductView> page = specification.search(criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Slim Fit Jeans", page.getContent().getFirst().name());
    }

    @Test
    void search_withVariantStatus_returnProductsHavingMatchingVariant() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .merchantId(MERCHANT_ID)
                .variantStatus("DELETED")
                .build();

        Page<ProductView> page = specification.search(criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Premium Cotton T-Shirt", page.getContent().getFirst().name());
    }

    @Test
    void search_withCategoryId_returnProductsUnderGivenCategoryId() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .merchantId(MERCHANT_ID)
                .categoryId("cat-2")
                .build();

        Page<ProductView> page = specification.search(criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Slim Fit Jeans", page.getContent().getFirst().name());
        assertEquals("cat-2", page.getContent().getFirst().categoryId());
    }

    @Test
    void search_withPageNumberAndPageSize_returnCorrectProductPage() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .merchantId(MERCHANT_ID)
                .build();

        Page<ProductView> page = specification.search(criteria, PageRequest.of(0, 2));

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void search_shouldSortByUpdatedAtDescByDefault() {
        ProductEntity p1 = new ProductEntity();
        p1.setUuid(UUID.randomUUID().toString());
        p1.setName("Oldest Product");
        p1.setCategoryId("cat-sort");
        p1.setMerchantId("merchant-sort");
        p1.setCreatedAt(java.time.Instant.parse("2026-01-01T10:00:00Z"));
        p1.setUpdatedAt(java.time.Instant.parse("2026-01-01T10:00:00Z"));
        entityManager.persist(p1);

        ProductEntity p2 = new ProductEntity();
        p2.setUuid(UUID.randomUUID().toString());
        p2.setName("Newest Product");
        p2.setCategoryId("cat-sort");
        p2.setMerchantId("merchant-sort");
        p2.setCreatedAt(java.time.Instant.parse("2026-01-01T10:00:00Z"));
        p2.setUpdatedAt(java.time.Instant.parse("2026-01-02T10:00:00Z"));
        entityManager.persist(p2);

        entityManager.flush();
        entityManager.clear();

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .merchantId("merchant-sort")
                .build();

        Page<ProductView> page = specification.search(criteria, PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
        assertEquals("Newest Product", page.getContent().get(0).name());
        assertEquals("Oldest Product", page.getContent().get(1).name());
    }

    @Test
    void findHeroMediasByProductIds_returnsRankZeroMediaOnly() {
        ProductEntity withHero = persistProduct("Hero Shirt", "cat-hero");
        ProductEntity withoutMedia = persistProduct("Plain Shirt", "cat-hero");
        persistMedia(withHero, "hero-1", "merchants/m/products/hero.jpg", 0);
        persistMedia(withHero, "side-1", "merchants/m/products/side.jpg", 1);

        entityManager.flush();
        entityManager.clear();

        List<ProductHeroMediaView> heroes = specification.findHeroMediasByProductIds(
                List.of(withHero.getUuid(), withoutMedia.getUuid())
        );

        assertEquals(1, heroes.size());
        ProductHeroMediaView hero = heroes.getFirst();
        assertEquals(withHero.getUuid(), hero.productId());
        assertEquals("hero-1", hero.mediaId());
        assertEquals("merchants/m/products/hero.jpg", hero.storageKey());
        assertEquals("image/jpeg", hero.contentType());
        assertEquals(0, hero.rank());
    }

    @Test
    void findHeroMediasByProductIds_withEmptyIds_returnsEmptyList() {
        assertTrue(specification.findHeroMediasByProductIds(List.of()).isEmpty());
    }

    @Test
    void search_storefrontVisible_returnsActiveProductsOfActiveMerchantsOnly() {
        ProductEntity visible = persistProduct("Visible Shirt", "cat-sf",
                variant("VIS-1", "ACTIVE"));
        visible.setStatus(ProductStatus.ACTIVE);
        visible.setFeatured(true);
        visible.setListingCondition("NEW");
        persistAvailability(MERCHANT_ID, "ACTIVE", "FIRST_PARTY_RETAILER");

        ProductEntity draft = persistProduct("Draft Shirt", "cat-sf");
        draft.setStatus(ProductStatus.DRAFT);

        ProductEntity suspendedMerchantProduct = persistProduct("Hidden Shirt", "cat-sf", "merchant-suspended",
                variant("HID-1", "ACTIVE"));
        suspendedMerchantProduct.setStatus(ProductStatus.ACTIVE);
        persistAvailability("merchant-suspended", "SUSPENDED", "C2C");

        entityManager.flush();
        entityManager.clear();

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .storefrontVisible(true)
                .build();

        Page<ProductView> page = specification.search(criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        ProductView view = page.getContent().getFirst();
        assertEquals("Visible Shirt", view.name());
        assertEquals(MERCHANT_ID, view.merchantId());
        assertTrue(view.featured());
        assertEquals("NEW", view.condition());
    }

    @Test
    void search_storefrontVisible_doesNotFilterByStorefrontStatus() {
        ProductEntity visible = persistProduct("Still Listed", "cat-sf",
                variant("STILL-1", "ACTIVE"));
        visible.setStatus(ProductStatus.ACTIVE);
        persistAvailability(MERCHANT_ID, "ACTIVE", "FIRST_PARTY_RETAILER");
        entityManager.flush();
        entityManager.clear();

        Page<ProductView> page = specification.search(
                ProductSearchCriteria.builder().storefrontVisible(true).build(),
                PageRequest.of(0, 10)
        );

        assertEquals(1, page.getTotalElements());
        assertEquals("Still Listed", page.getContent().getFirst().name());
    }

    @Test
    void search_withSalesChannelId_returnsOnlyProductsPublishedToThatChannel() {
        ProductEntity websiteOnly = persistProduct("Website Only Shirt", "cat-ch",
                variant("WEB-SKU", "ACTIVE"));
        persistPublication(websiteOnly.getProductVariants().iterator().next(), "website-1");
        ProductEntity marketplaceOnly = persistProduct("Marketplace Only Shirt", "cat-ch",
                variant("MKT-SKU", "ACTIVE"));
        persistPublication(marketplaceOnly.getProductVariants().iterator().next(), "marketplace-1");
        entityManager.flush();
        entityManager.clear();

        Page<ProductView> website = specification.search(
                ProductSearchCriteria.builder()
                        .merchantId(MERCHANT_ID)
                        .salesChannelId("website-1")
                        .build(),
                PageRequest.of(0, 10)
        );

        assertTrue(website.getContent().stream().anyMatch(product -> product.name().equals("Website Only Shirt")));
        assertTrue(website.getContent().stream().noneMatch(product -> product.name().equals("Marketplace Only Shirt")));
    }

    @Test
    void findActiveVariantsByProductIds_returnsActiveVariantRefs() {
        ProductEntity product = persistProduct("Variant Shirt", "cat-var",
                variant("VAR-ACTIVE", "ACTIVE"),
                variant("VAR-DELETED", "DELETED"));
        entityManager.flush();
        entityManager.clear();

        List<ProductVariantRefView> refs = specification.findActiveVariantsByProductIds(List.of(product.getUuid()));

        assertEquals(1, refs.size());
        assertEquals(product.getUuid(), refs.getFirst().productId());
        assertEquals("VAR-ACTIVE", refs.getFirst().sku());
    }

    @Test
    void findPublicationsByProductIds_returnsVariantAndChannelIdsFromPublicationTable() {
        ProductEntity product = persistProduct("Published Shirt", "cat-pub",
                variant("PUB-SKU", "ACTIVE"));
        ProductVariantEntity variant = product.getProductVariants().iterator().next();
        persistPublication(variant, "website-1");
        persistPublication(variant, "marketplace-1");
        entityManager.flush();
        entityManager.clear();

        List<ProductPublicationView> publications = specification.findPublicationsByProductIds(List.of(product.getUuid()));

        assertEquals(2, publications.size());
        assertTrue(publications.stream().allMatch(view -> view.productId().equals(product.getUuid())));
        assertTrue(publications.stream().allMatch(view -> view.variantId().equals(variant.getUuid())));
        assertTrue(publications.stream().anyMatch(view -> view.salesChannelId().equals("website-1")));
        assertTrue(publications.stream().anyMatch(view -> view.salesChannelId().equals("marketplace-1")));
    }

    private void persistPublication(ProductVariantEntity variant, String salesChannelId) {
        ProductPublicationEntity publication = new ProductPublicationEntity();
        publication.setVariantId(variant.getId());
        publication.setSalesChannelId(salesChannelId);
        entityManager.persist(publication);
    }

    private void persistAvailability(String merchantId, String status, String merchantType) {
        CatalogMerchantAvailabilityEntity availability = new CatalogMerchantAvailabilityEntity();
        availability.setMerchantId(merchantId);
        availability.setStatus(status);
        availability.setMerchantType(merchantType);
        availability.setUpdatedAt(java.time.Instant.now());
        entityManager.persist(availability);
    }

    private void persistMedia(ProductEntity product, String uuid, String storageKey, int rank) {
        MediaEntity media = new MediaEntity();
        media.setUuid(uuid);
        media.setType("image/jpeg");
        media.setContentType("image/jpeg");
        media.setPath(storageKey);
        media.setStorageKey(storageKey);
        media.setUrl("stale-url");
        media.setRank(rank);
        entityManager.persist(media);
        product.addMedia(media);
    }

    private ProductEntity persistProduct(String name, String categoryId, VariantData... variants) {
        return persistProduct(name, categoryId, MERCHANT_ID, variants);
    }

    private ProductEntity persistProduct(String name, String categoryId, String merchantId, VariantData... variants) {
        ProductEntity product = new ProductEntity();
        product.setUuid(UUID.randomUUID().toString());
        product.setName(name);
        product.setCategoryId(categoryId);
        product.setMerchantId(merchantId);
        entityManager.persist(product);

        for (VariantData v : variants) {
            ProductVariantEntity variantEntity = new ProductVariantEntity();
            variantEntity.setUuid(UUID.randomUUID().toString());
            variantEntity.setSku(v.sku);
            variantEntity.setStatus(v.status);
            product.addVariant(variantEntity);
            entityManager.persist(variantEntity);

            for (VariationData vd : v.variations) {
                ProductVariationEntity variationEntity = new ProductVariationEntity();
                variationEntity.setId(new ProductVariationEntity.ProductVariationId(
                        vd.optionId, vd.typeId, null));
                variantEntity.addProductVariation(variationEntity);
                entityManager.persist(variationEntity);
            }
        }
        return product;
    }

    private static VariantData variant(String sku, String status, VariationData... variations) {
        return new VariantData(sku, status, List.of(variations));
    }

    private static VariationData variation(String optionId, String typeId,
                                           String optionValue, String typeValue) {
        return new VariationData(optionId, typeId, optionValue, typeValue);
    }

    private record VariantData(String sku, String status, List<VariationData> variations) {}

    private record VariationData(String optionId, String typeId,
                                 String optionValue, String typeValue) {}
}
