package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.store.identity.internal.query.ListRolesQuery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class ListRolesMapper {
    public ListRolesQuery toQuery() {
        return new ListRolesQuery();
    }
}
