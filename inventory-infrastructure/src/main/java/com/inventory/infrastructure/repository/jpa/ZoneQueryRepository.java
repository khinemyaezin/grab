package com.inventory.infrastructure.repository.jpa;

import com.grab.framework.id.Id;
import com.inventory.domain.enums.ZoneType;
import com.inventory.infrastructure.view.ZoneView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ZoneQueryRepository {
    Page<ZoneView> queryByLocationId(Id locationId, Pageable pageable);
    Page<ZoneView> queryByLocationIdAndActive(Id locationId, boolean active, Pageable pageable);
}
