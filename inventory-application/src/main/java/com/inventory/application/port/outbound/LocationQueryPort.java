package com.inventory.application.port.outbound;

import com.inventory.domain.enums.LocationType;
import com.inventory.application.model.read.LocationSearchCriteria;
import com.inventory.application.model.read.LocationView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LocationQueryPort {
    Page<LocationView> queryAll(String merchantId, Pageable pageable);
    Page<LocationView> queryByActive(String merchantId, Pageable pageable);
    Page<LocationView> queryByType(String merchantId, LocationType type, Pageable pageable);
    Page<LocationView> search(LocationSearchCriteria criteria, Pageable pageable);

    Optional<LocationView> findById(String locationId);

    Optional<LocationView> findByCode(String code);
}
