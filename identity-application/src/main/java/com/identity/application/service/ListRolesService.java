package com.identity.application.service;

import com.identity.application.port.inbound.ListRolesUseCase;
import com.identity.application.port.outbound.RoleQueryPort;
import com.identity.application.model.read.ListRolesQuery;
import com.identity.application.model.read.ListRolesResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
public class ListRolesService implements ListRolesUseCase {

    private final RoleQueryPort roleQueryPort;

    public Page<ListRolesResult> execute(ListRolesQuery query) {
        return roleQueryPort.findAll(query.pageable()).map(role -> new ListRolesResult(
                role.code(),
                role.name(),
                role.description(),
                role.kind(),
                role.active(),
                role.assignable(),
                role.authorityCodes(),
                role.platformCodes()
        ));
    }
}
