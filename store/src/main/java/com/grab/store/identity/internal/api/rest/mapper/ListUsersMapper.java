package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.store.identity.internal.query.ListUsersQuery;
import org.springframework.data.domain.Pageable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class ListUsersMapper {
    public abstract ListUsersQuery toQuery(Pageable pageable);
}
