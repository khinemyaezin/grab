package com.grab.store.product.internal.usecase;

import com.grab.store.product.internal.api.rest.dto.CreatedResponse;
import com.grab.store.product.internal.command.CreateProductCommand;

public interface CreateProductUseCase extends GenericUseCase<CreateProductCommand, CreatedResponse> {
}
