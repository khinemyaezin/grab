package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.config.CategoryRepositoryTestConfig;
import com.catalog.infrastructure.view.CategoryView;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CategoryJpaRepositoryTest extends CategoryRepositoryTestConfig {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CategoryJpaRepo repository;

    @BeforeEach
    void setUp() {
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
        persistCategory("under-leaf", "Under", 22, 23, 2);

        persistCategory("root-backslash", "Elec\\Deals", 25, 28, 1);
        persistCategory("slash-leaf", "Slash", 26, 27, 2);

        entityManager.flush();
        entityManager.clear();
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
        entity.setC2cAllowed(true);
        entityManager.persist(entity);
    }

    @Test
    public void findAllByCategoryUuid_shouldReturnViews() {
        List<String> categories = List.of("under-leaf", "root-underscore", "root-backslash");
        List<CategoryView> result = repository.findAllByUuids(categories);
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(categories.size(), result.size());
    }

    @Test
    public void findAllByCategoryUuid_withLeafCategory_shouldReturnCorrectedParentId() {
        List<String> categories = List.of("under-leaf");
        List<CategoryView> result = repository.findAllByUuids(categories);
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());

        CategoryView firstCategory = result.getFirst();
        Assertions.assertEquals("root-underscore", firstCategory.parentId(),
                "The leaf category did not resolve to the expected parent ID");
    }
}
