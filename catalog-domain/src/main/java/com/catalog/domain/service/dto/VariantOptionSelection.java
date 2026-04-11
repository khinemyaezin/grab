package com.catalog.domain.service.dto;

import com.grab.framework.id.Id;

public record VariantOptionSelection(
        Id valueId,
        Id typeId
) {
}
