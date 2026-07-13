package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.LocationType;
import com.inventory.infrastructure.view.LocationView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LocationQueryRepository {
    Page<LocationView> queryAll(String merchantId, Pageable pageable);
    Page<LocationView> queryByActive(String merchantId, Pageable pageable);
    Page<LocationView> queryByType(String merchantId, LocationType type, Pageable pageable);
}
