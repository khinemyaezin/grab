package com.grab.store.shared;

import org.springframework.data.domain.Pageable;

public interface PageableQueryRequest {
    Pageable pageable();
}
