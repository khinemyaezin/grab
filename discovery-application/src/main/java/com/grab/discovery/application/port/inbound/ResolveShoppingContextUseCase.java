package com.grab.discovery.application.port.inbound;

import com.grab.discovery.application.model.read.ResolveShoppingContextQuery;
import com.grab.discovery.application.model.read.ResolveShoppingContextResult;

public interface ResolveShoppingContextUseCase {
    ResolveShoppingContextResult execute(ResolveShoppingContextQuery query);
}
