package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.view.VariantOptionView;

import java.util.List;

public interface VariantOptionQueryRepository {
    List<VariantOptionView> findAllByUuidIn(List<String> uuids);
    List<VariantOptionView> findByNameAndTypeId(String name, String typeUuid);
}
