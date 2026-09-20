package com.identity.application.service;

import com.identity.application.port.inbound.SearchRolesUseCase;

import com.identity.application.model.read.SearchRolesQuery;
import com.identity.application.model.read.SearchRolesResult;
import com.identity.application.port.outbound.RoleQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SearchRolesService implements SearchRolesUseCase {

    private final RoleQueryPort roleQueryPort;
    public SearchRolesResult execute(SearchRolesQuery query) {
        return roleQueryPort.queryByName(query.name())
                .stream()
                .map(view -> new SearchRolesResult.Role(view.getId(), view.getName(), view.getCode()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), SearchRolesResult::new));
    }
}
