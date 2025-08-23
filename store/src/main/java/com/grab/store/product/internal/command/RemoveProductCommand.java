package com.grab.store.product.internal.command;

import com.grab.framework.id.Id;
import jakarta.validation.constraints.NotEmpty;

public record RemoveProductCommand(
        Id productId
) {
}
