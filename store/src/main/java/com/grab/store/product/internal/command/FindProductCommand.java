package com.grab.store.product.internal.command;

import com.grab.framework.id.Id;

public record FindProductCommand(
        Id id
) {
}
