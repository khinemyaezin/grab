package com.grab.store.product.internal.command;

import com.grab.framework.id.Id;

public record VariationCommand(
        Id optionId,
        String optionName,
        Id typeId,
        String typeName
) {
}
