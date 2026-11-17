package com.grab.service.contract.message.account.identity;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.List;
import java.util.Set;

/**
 * Universal Java Port Interface for RBAC Roles and Authority Definitions.
 * Unifies RoleAdminController and SecurityCatalogQuery.
 */
public interface RolePermissionPort extends Remote {

    RoleItemResult createRole(CreateRoleMessage message) throws RemoteException;

    void grantAuthorityToRole(String roleCode, String authorityCode) throws RemoteException;

    void revokeAuthorityFromRole(String roleCode, String authorityCode) throws RemoteException;

    List<RoleItemResult> listRoles(String name, int page, int size) throws RemoteException;

    List<RoleSuggestionResult> searchRoles(String name) throws RemoteException;

    record CreateRoleMessage(
            String roleCode,
            String name,
            String description,
            Set<String> authorityCodes
    ) implements Serializable {}

    record RoleItemResult(
            String code,
            String name,
            String description,
            String kind,
            boolean active,
            boolean assignable,
            Set<String> authorities
    ) implements Serializable {}

    record RoleSuggestionResult(
            Long id,
            String name,
            String code
    ) implements Serializable {}
}
