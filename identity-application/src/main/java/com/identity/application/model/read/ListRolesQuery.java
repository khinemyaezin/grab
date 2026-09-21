package com.identity.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.cqrs.query.PageableQueryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record ListRolesQuery(
        String name,
        Pageable pageable
)
        implements Query<Page<ListRolesResult>>, PageableQueryRequest {
}
