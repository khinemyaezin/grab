package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.view.RoleView;

import java.util.List;

public interface RoleQueryRepository {
    List<RoleView> queryByName(String name);
}
