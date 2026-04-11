package com.catalog.infrastructure.mapper.jpa.impl;

import com.catalog.domain.aggregate.VariantOption;
import com.catalog.domain.aggregate.VariantType;
import com.catalog.domain.valueobject.VariantTypeStatus;
import com.catalog.infrastructure.entity.entity.VariantOptionEntity;
import com.catalog.infrastructure.entity.entity.VariantTypeEntity;
import com.catalog.infrastructure.mapper.jpa.VariantTypeJpaAssembler;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public class VariantTypeJpaAssemblerImpl implements VariantTypeJpaAssembler {
    private static final VariantTypeStatus DEFAULT_STATUS = VariantTypeStatus.ACTIVE;

    private final IdGenerator idGenerator;

    @Override
    public VariantTypeEntity buildFullEntityGraph(VariantType variantType, VariantTypeEntity entity) {
        if (entity == null) {
            entity = new VariantTypeEntity();
        }

        entity.setUuid(variantType.getId().getValue());
        entity.setName(variantType.getName());
        entity.setStatus(toStatusName(variantType.getStatus()));
        mergeOptions(entity, variantType.getOptions());

        return entity;
    }

    private void mergeOptions(VariantTypeEntity entity, Set<VariantOption> options) {
        Set<VariantOptionEntity> currentOptions = entity.getVariantOptions();
        if (currentOptions == null) {
            currentOptions = new LinkedHashSet<>();
            entity.setVariantOptions(currentOptions);
        }

        if (options == null) {
            currentOptions.clear();
            return;
        }

        Map<String, VariantOptionEntity> existingByUuid = new LinkedHashMap<>();
        for (VariantOptionEntity optionEntity : currentOptions) {
            if (optionEntity.getUuid() != null) {
                existingByUuid.put(optionEntity.getUuid(), optionEntity);
            }
        }

        List<VariantOptionEntity> mergedOptions = new ArrayList<>();
        for (VariantOption option : options) {
            VariantOptionEntity optionEntity = existingByUuid.get(option.getId().getValue());
            if (optionEntity == null) {
                optionEntity = new VariantOptionEntity();
            }

            optionEntity.setUuid(option.getId().getValue());
            optionEntity.setName(option.getName());
            optionEntity.setVariantType(entity);
            mergedOptions.add(optionEntity);
        }

        currentOptions.clear();
        currentOptions.addAll(mergedOptions);
    }

    @Override
    public VariantType toFullDomainGraph(VariantTypeEntity entity) {
        Id typeId = idGenerator.convertIdFrom(entity.getUuid());
        List<VariantOption> options = entity.getVariantOptions() == null
                ? List.of()
                : entity.getVariantOptions().stream()
                .filter(option -> option.getUuid() != null)
                .map(option -> new VariantOption(
                        idGenerator.convertIdFrom(option.getUuid()),
                        option.getName(),
                        typeId
                ))
                .toList();

        return new VariantType(
                typeId,
                entity.getName(),
                toStatus(entity.getStatus()),
                options
        );
    }

    private String toStatusName(VariantTypeStatus status) {
        return status == null ? DEFAULT_STATUS.name() : status.name();
    }

    private VariantTypeStatus toStatus(String status) {
        if (status == null || status.isBlank()) {
            return DEFAULT_STATUS;
        }

        try {
            return VariantTypeStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return DEFAULT_STATUS;
        }
    }
}
