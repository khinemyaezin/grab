package com.inventory.application.model.read;

import com.grab.framework.id.Id;

public record ListZonesResult(
        Id id,
        Id locationId,
        String code,
        String name,
        String type,
        boolean active
) {

}
