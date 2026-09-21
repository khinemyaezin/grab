package com.inventory.application.port.outbound;

import com.inventory.application.model.read.BinSearchCriteria;
import com.inventory.application.model.read.BinView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BinQueryPort {
    Page<BinView> queryByZoneId(String zoneId, Pageable pageable);
    Page<BinView> queryByZoneIdAndActive(String zoneId, boolean active, Pageable pageable);
    Page<BinView> search(BinSearchCriteria criteria, Pageable pageable);

    Optional<BinView> findById(String binId);
}
