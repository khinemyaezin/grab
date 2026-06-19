package com.grab.store.identity.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.store.shared.PageableQueryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record ListRolesQuery(Pageable pageable)
        implements Query<Page<ListRolesResult>>, PageableQueryRequest {
}
