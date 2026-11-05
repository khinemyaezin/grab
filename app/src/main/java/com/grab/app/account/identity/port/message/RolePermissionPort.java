package com.grab.app.account.identity.port.message;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Set;

/**
 * Universal Java Port Interface for RBAC Roles and Authority Definitions.
 * Unifies RoleAdminController and SecurityCatalogQuery.
 */
public interface RolePermissionPort {

    RoleItemResult createRole(CreateRoleMessage message);

    void grantAuthorityToRole(String roleCode, String authorityCode);

    void revokeAuthorityFromRole(String roleCode, String authorityCode);

    List<RoleItemResult> listRoles(String name, int page, int size);

    List<RoleSuggestionResult> searchRoles(String name);

    record CreateRoleMessage(
            String roleCode,
            String name,
            String description,
            Set<String> authorityCodes
    ) {}

    record RoleItemResult(
            String code,
            String name,
            String description,
            String kind,
            boolean active,
            boolean assignable,
            Set<String> authorities
    ) {}

    record RoleSuggestionResult(
            Long id,
            String name,
            String code
    ) {}
}
