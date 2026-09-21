package com.inventory.application.port.inbound;

import com.inventory.application.model.read.ListZonesByLocationQuery;
import org.springframework.data.domain.Page;
import com.inventory.application.model.read.ListZonesResult;

public interface ListZonesByLocationUseCase {
    Page<ListZonesResult> execute(ListZonesByLocationQuery query);
}
