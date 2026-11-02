package com.grab.app.account.identity.port.message;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Set;

/**
 * Universal Java Port Interface for RBAC Roles and Authority Definitions.
 * Unifies RoleAdminController and SecurityCatalogQuery.
 */
public interface RolePermissionPort {

    RoleResult createRole(CreateRoleMessage message);

    void grantAuthorityToRole(String roleCode, String authorityCode);

    void revokeAuthorityFromRole(String roleCode, String authorityCode);

    List<RoleResult> listRoles();

    List<String> listRoleSuggestions();

    List<AuthorityDefinitionResult> listAllAuthorities();

    record CreateRoleMessage(
            String roleCode,
            String name,
            String description,
            Set<String> initialAuthorities
    ) {}

    record RoleResult(
            Id id,
            String roleCode,
            String name,
            String description,
            Set<String> authorities
    ) {}

    record AuthorityDefinitionResult(
            String code,
            String name,
            String category,
            String description
    ) {}
}

