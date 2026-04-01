package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.VariantOptionEntity;
import com.catalog.infrastructure.entity.entity.VariantTypeEntity;
import com.catalog.infrastructure.view.VariantOptionView;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class VariantOptionQueryRepoTest extends CategoryRepositoryTestConfig {

    @Autowired
    private VariantOptionQueryRepository variantOptionJpaRepo;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        persistTypeWithOptions(
                "type-color",
                "Color",
                option("opt-red", "Red"),
                option("opt-blue", "Blue")
        );
        persistTypeWithOptions(
                "type-size",
                "Size",
                option("opt-small", "Small")
        );

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findAllByUuidIn_returnsProjectedVariantOptionsWithOwningTypeData() {
        List<VariantOptionView> result = variantOptionJpaRepo.findAllByUuidIn(List.of(
                "opt-blue",
                "opt-small",
                "missing"
        ));

        assertThat(result)
                .extracting(
                        VariantOptionView::optionId,
                        VariantOptionView::optionName,
                        VariantOptionView::typeId,
                        VariantOptionView::typeName
                )
                .containsExactlyInAnyOrder(
                        tuple("opt-blue", "Blue", "type-color", "Color"),
                        tuple("opt-small", "Small", "type-size", "Size")
                );
    }

    private void persistTypeWithOptions(String typeUuid, String typeName, VariantOptionEntity... options) {
        VariantTypeEntity typeEntity = new VariantTypeEntity();
        typeEntity.setUuid(typeUuid);
        typeEntity.setName(typeName);
        typeEntity.setStatus("ACTIVE");

        LinkedHashSet<VariantOptionEntity> optionEntities = new LinkedHashSet<>();
        for (VariantOptionEntity option : options) {
            option.setVariantType(typeEntity);
            optionEntities.add(option);
        }
        typeEntity.setVariantOptions(optionEntities);

        entityManager.persist(typeEntity);
    }

    private VariantOptionEntity option(String uuid, String name) {
        VariantOptionEntity entity = new VariantOptionEntity();
        entity.setUuid(uuid);
        entity.setName(name);
        return entity;
    }
}
