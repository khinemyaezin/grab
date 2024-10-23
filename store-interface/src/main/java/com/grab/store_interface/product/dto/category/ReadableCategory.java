package com.grab.store_interface.product.dto.category;

import java.util.Set;

public record ReadableCategory(
        String uuid,
        String name,
        Integer depth,
        String parentUuid,
        Set<ReadableCategory> children
) {
}
