package com.catalog.infrastructure.mapper.jpa.impl;

import com.catalog.domain.aggregate.VariantOption;
import com.catalog.domain.aggregate.VariantType;
import com.catalog.domain.valueobject.VariantTypeStatus;
import com.catalog.infrastructure.entity.entity.VariantOptionEntity;
import com.catalog.infrastructure.entity.entity.VariantTypeEntity;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VariantTypeJpaAssemblerImplTest {

    private VariantTypeJpaAssemblerImpl assembler;

    @BeforeEach
    void setUp() {
        assembler = new VariantTypeJpaAssemblerImpl(new UuidGenerator());
    }

    @Test
    void buildFullEntityGraph_mergesOptionsByUuid_andPreservesExistingDatabaseIds() {
        VariantType variantType = new VariantType(
                id("type-color"),
                "Color",
                VariantTypeStatus.ACTIVE,
                List.of(
                        VariantOption.create(id("opt-red"), "Red", id("type-color")),
                        VariantOption.create(id("opt-blue"), "Blue", id("type-color"))
                )
        );

        VariantTypeEntity existingEntity = new VariantTypeEntity();
        existingEntity.setId(10L);
        existingEntity.setUuid("type-color");
        existingEntity.setName("Old Color");
        existingEntity.setStatus("INACTIVE");
        existingEntity.setVariantOptions(new LinkedHashSet<>());

        VariantOptionEntity existingRed = optionRow(21L, "opt-red", "Old Red", existingEntity);
        VariantOptionEntity orphan = optionRow(22L, "opt-old", "Old", existingEntity);
        existingEntity.getVariantOptions().add(existingRed);
        existingEntity.getVariantOptions().add(orphan);

        VariantTypeEntity result = assembler.buildFullEntityGraph(variantType, existingEntity);

        assertThat(result).isSameAs(existingEntity);
        assertThat(result.getUuid()).isEqualTo("type-color");
        assertThat(result.getName()).isEqualTo("Color");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getVariantOptions())
                .extracting(VariantOptionEntity::getUuid)
                .containsExactly("opt-red", "opt-blue");

        VariantOptionEntity mergedRed = findOption(result, "opt-red");
        assertThat(mergedRed).isSameAs(existingRed);
        assertThat(mergedRed.getId()).isEqualTo(21L);
        assertThat(mergedRed.getName()).isEqualTo("Red");
        assertThat(mergedRed.getVariantType()).isSameAs(result);

        VariantOptionEntity addedBlue = findOption(result, "opt-blue");
        assertThat(addedBlue.getId()).isNull();
        assertThat(addedBlue.getName()).isEqualTo("Blue");
        assertThat(addedBlue.getVariantType()).isSameAs(result);
    }

    @Test
    void toFullDomainGraph_mapsUuidStatusAndOptions() {
        VariantTypeEntity entity = new VariantTypeEntity();
        entity.setId(99L);
        entity.setUuid("type-color");
        entity.setName("Color");
        entity.setStatus("inactive");
        entity.setVariantOptions(new LinkedHashSet<>());
        entity.getVariantOptions().add(optionRow(31L, "opt-red", "Red", entity));
        entity.getVariantOptions().add(optionRow(32L, "opt-blue", "Blue", entity));

        VariantType result = assembler.toFullDomainGraph(entity);

        assertThat(result.getId().getValue()).isEqualTo("type-color");
        assertThat(result.getName()).isEqualTo("Color");
        assertThat(result.getStatus()).isEqualTo(VariantTypeStatus.INACTIVE);
        assertThat(result.getOptions())
                .extracting(option -> option.getId().getValue())
                .containsExactly("opt-red", "opt-blue");
        assertThat(result.getOptions())
                .extracting(option -> option.getVariantTypeId().getValue())
                .containsOnly("type-color");
    }

    @Test
    void toFullDomainGraph_defaultsMissingStatusToActive() {
        VariantTypeEntity entity = new VariantTypeEntity();
        entity.setUuid("type-size");
        entity.setName("Size");
        entity.setVariantOptions(new LinkedHashSet<>());

        VariantType result = assembler.toFullDomainGraph(entity);

        assertThat(result.getStatus()).isEqualTo(VariantTypeStatus.ACTIVE);
    }

    private VariantOptionEntity optionRow(Long id, String uuid, String name, VariantTypeEntity owner) {
        VariantOptionEntity entity = new VariantOptionEntity();
        entity.setId(id);
        entity.setUuid(uuid);
        entity.setName(name);
        entity.setVariantType(owner);
        return entity;
    }

    private VariantOptionEntity findOption(VariantTypeEntity entity, String uuid) {
        return entity.getVariantOptions().stream()
                .filter(option -> uuid.equals(option.getUuid()))
                .findFirst()
                .orElseThrow();
    }

    private Id id(String value) {
        return new CommonId(value);
    }
}
