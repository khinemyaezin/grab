package com.inventory.domain.port.outbound;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.Location;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {

    Optional<Location> findById(Id id);
    Optional<Location> findByCode(String code);
    Location save(Location location);
    void delete(Id id);
    boolean existsByCode(String code);
    List<Location> findByMerchantId(Id merchantId);
}
