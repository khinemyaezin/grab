package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.view.VariantTypeView;

import java.util.List;

public interface VariantTypeQueryRepository {
    List<VariantTypeView> findByName(String name);
}
