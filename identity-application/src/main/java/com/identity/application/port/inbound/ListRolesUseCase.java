package com.identity.application.port.inbound;

import com.identity.application.model.read.ListRolesQuery;
import org.springframework.data.domain.Page;
import com.identity.application.model.read.ListRolesResult;

public interface ListRolesUseCase {
    Page<ListRolesResult> execute(ListRolesQuery query);
}
