package com.identity.application.port.inbound;

import com.identity.application.model.read.SearchRolesQuery;
import com.identity.application.model.read.SearchRolesResult;

public interface SearchRolesUseCase {
    SearchRolesResult execute(SearchRolesQuery query);
}
