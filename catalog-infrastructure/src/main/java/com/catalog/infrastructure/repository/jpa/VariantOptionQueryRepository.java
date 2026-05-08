package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.view.VariantOptionView;

import java.util.List;

/**
 * Read-side query repository for variant options.
 * Decouples the service layer from Spring Data JPA internals.
 */
public interface VariantOptionQueryRepository {
    List<VariantOptionView> findAllByUuidIn(List<String> uuids);
}
