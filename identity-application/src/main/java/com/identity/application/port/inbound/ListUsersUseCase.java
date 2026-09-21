package com.identity.application.port.inbound;

import com.identity.application.model.read.ListUsersQuery;
import org.springframework.data.domain.Page;
import com.identity.application.model.read.ListUsersResult;

public interface ListUsersUseCase {
    Page<ListUsersResult> execute(ListUsersQuery query);
}
