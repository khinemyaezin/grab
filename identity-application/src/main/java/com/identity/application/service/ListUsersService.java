package com.identity.application.service;

import com.identity.application.port.inbound.ListUsersUseCase;

import com.identity.application.model.read.ListUsersQuery;
import com.identity.application.model.read.ListUsersResult;
import com.identity.application.port.outbound.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
public class ListUsersService implements ListUsersUseCase {

    private final UserQueryPort userQueryPort;
    public Page<ListUsersResult> execute(ListUsersQuery query) {
        return userQueryPort.findAll(query.pageable()).map(row -> new ListUsersResult(
                row.uuid(),
                row.email(),
                row.status(),
                row.createdAt()
        ));
    }
}
