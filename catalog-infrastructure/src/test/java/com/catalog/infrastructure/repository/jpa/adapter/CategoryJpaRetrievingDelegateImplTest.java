package com.catalog.infrastructure.repository.jpa.adapter;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRetrievingDelegate;
import com.catalog.infrastructure.repository.jpa.config.CategoryRepositoryTestConfig;
import com.nestedset.app.config.JpaNestedSetRepositoryConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CategoryJpaRetrievingDelegateImplTest extends CategoryRepositoryTestConfig {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JpaContext jpaContext;

    private CategoryJpaRetrievingDelegate retrievingDelegate;

    @BeforeEach
    void setUp() {
        JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> config =
                new JpaNestedSetRepositoryConfiguration<>(jpaContext, CategoryEntity.class);
        retrievingDelegate = new CategoryJpaRetrievingDelegateImpl(config);
        persistCategory("root", "Root", 1, 29, 0);

        persistCategory("root-electronics", "Electronics", 1, 10, 1);
        persistCategory("phones", "Phones", 2, 7, 2);
        persistCategory("android", "Android", 3, 4, 3);
        persistCategory("ios", "iOS", 5, 6, 3);
        persistCategory("laptops", "Laptops", 8, 9, 2);

        persistCategory("root-elegant", "Elegant Home", 11, 16, 1);
        persistCategory("decor", "Decor", 12, 13, 2);
        persistCategory("furniture", "Furniture", 14, 15, 2);

        persistCategory("root-percent", "Elec%Deals", 17, 20, 1);
        persistCategory("promo", "Promo", 18, 19, 1);

        persistCategory("root-underscore", "Elec_Deals", 21, 24, 1);
        persistCategory("under-leaf", "Under", 22, 23, 1);

        persistCategory("root-backslash", "Elec\\Deals", 25, 28, 1);
        persistCategory("slash-leaf", "Slash", 26, 27, 1);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void getLeafNodesByName_withNullOrBlankInput_returnsEmpty() {
        assertThat(retrievingDelegate.getLeafNodesByName(null)).isEmpty();
        assertThat(retrievingDelegate.getLeafNodesByName("   ")).isEmpty();
    }

    @Test
    void getLeafNodesByName_withParentName_returnsLeafNodesInLeftOrder() {
        List<CategoryEntity> leaves = retrievingDelegate.getLeafNodesByName("Elect  ");

        assertThat(leaves)
                .extracting(CategoryEntity::getUuid)
                .containsExactly("android", "ios", "laptops");
    }

    @Test
    void getLeafNodesByName_withParentNameSecondDepth_returnsLeafNodesInLeftOrder() {
        List<CategoryEntity> leaves = retrievingDelegate.getLeafNodesByName("Pho");

        assertThat(leaves)
                .extracting(CategoryEntity::getUuid)
                .containsExactly("android","ios");
    }

    @Test
    void getLeafNodesByName_withLeafName_returnsLeafNodesInLeftOrder() {
        List<CategoryEntity> leaves = retrievingDelegate.getLeafNodesByName("Android");

        assertThat(leaves)
                .extracting(CategoryEntity::getUuid)
                .containsExactly("android");
    }

    @Test
    void getLeafNodesByName_withRootName_returnsAllLeafNodesInLeftOrder() {
        List<CategoryEntity> leaves = retrievingDelegate.getLeafNodesByName("r");

        assertThat(leaves)
                .extracting(CategoryEntity::getUuid)
                .containsExactly("android", "ios", "laptops", "decor", "furniture", "promo", "under-leaf", "slash-leaf");
    }

    @Test
    void getLeafNodesByName_escapes_likeWildcardsAndBackslash() {
        assertThat(retrievingDelegate.getLeafNodesByName("Elec%"))
                .extracting(CategoryEntity::getUuid)
                .containsExactly("promo");

        assertThat(retrievingDelegate.getLeafNodesByName("Elec_"))
                .extracting(CategoryEntity::getUuid)
                .containsExactly("under-leaf");

        assertThat(retrievingDelegate.getLeafNodesByName("Elec\\"))
                .extracting(CategoryEntity::getUuid)
                .containsExactly("slash-leaf");
    }

    @Test
    void getLeafNodesByName_withMatchingRootHasNullBounds_returnsEmpty() {
        persistCategory("broken-root", "Broken", null, null, 0);
        entityManager.flush();
        entityManager.clear();

        assertThat(retrievingDelegate.getLeafNodesByName("Broken")).isEmpty();
    }

    private void persistCategory(String uuid, String name, Integer left, Integer right, Integer depth) {
        CategoryEntity entity = new CategoryEntity();
        entity.setUuid(uuid);
        entity.setName(name);
        entity.setLft(left);
        entity.setRgt(right);
        entity.setDepth(depth);
        entity.setActive(true);
        entity.setListingAllowed(true);
        entity.setReviewRequired(false);
        entity.setC2cAllowed(true);
        entityManager.persist(entity);
    }
}
