package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.LocationType;
import com.inventory.infrastructure.view.LocationView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LocationQueryRepository {
    Page<LocationView> queryAll(String sellerId, Pageable pageable);
    Page<LocationView> queryByActive(String sellerId, Pageable pageable);
    Page<LocationView> queryByType(String sellerId, LocationType type, Pageable pageable);
}
