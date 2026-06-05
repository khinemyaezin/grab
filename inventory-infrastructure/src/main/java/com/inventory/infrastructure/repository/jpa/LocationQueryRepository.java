package com.inventory.infrastructure.repository.jpa;

import com.grab.framework.id.Id;
import com.inventory.domain.enums.LocationType;
import com.inventory.infrastructure.view.LocationView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LocationQueryRepository {
    Page<LocationView> queryAll(Id sellerId, Pageable pageable);
    Page<LocationView> queryByActive(Id sellerId, Pageable pageable);
    Page<LocationView> queryByType(Id sellerId, LocationType type, Pageable pageable);
}
