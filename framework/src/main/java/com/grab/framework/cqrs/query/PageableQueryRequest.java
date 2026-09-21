package com.grab.framework.cqrs.query;

import org.springframework.data.domain.Pageable;

public interface PageableQueryRequest {
    Pageable pageable();
}
