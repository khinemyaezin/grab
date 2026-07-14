package com.inventory.infrastructure.repository.jpa;

import com.inventory.infrastructure.specification.jpa.BinSearchCriteria;
import com.inventory.infrastructure.view.BinView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BinQueryRepository {
    Page<BinView> queryByZoneId(String zoneId, Pageable pageable);
    Page<BinView> queryByZoneIdAndActive(String zoneId, boolean active, Pageable pageable);
    Page<BinView> search(BinSearchCriteria criteria, Pageable pageable);
}
