package com.inventory.application.port.outbound;

import com.inventory.application.model.read.ZoneSearchCriteria;
import com.inventory.application.model.read.ZoneView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ZoneQueryPort {
    Page<ZoneView> queryByLocationId(String locationId, Pageable pageable);
    Page<ZoneView> queryByLocationIdAndActive(String locationId, boolean active, Pageable pageable);
    Page<ZoneView> search(ZoneSearchCriteria criteria, Pageable pageable);

    Optional<ZoneView> findById(String zoneId);
}
