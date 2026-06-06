package com.inventory.infrastructure.repository.jpa;

import com.inventory.infrastructure.view.ZoneView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ZoneQueryRepository {
    Page<ZoneView> queryByLocationId(String locationId, Pageable pageable);
    Page<ZoneView> queryByLocationIdAndActive(String locationId, boolean active, Pageable pageable);
}
