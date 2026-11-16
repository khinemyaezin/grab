package com.grab.app.platform.seller.port.outbound;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateRoleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.GrantAccessRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Outbound port for User Admin, Role Governance, and Access Assignments in Seller Platform BFF.
 */
public interface SellerIdentityAdminOutboundPort {

    Optional<UserProfileDetailResponse> findUserById(String userId);

    List<SellerUserProfileResponse> listUsers(int page, int size);

    void changeUserStatus(String userId, String status);

    RoleResponse createRole(CreateRoleRequest request);

    void grantAuthorityToRole(String roleCode, String authorityCode);

    void revokeAuthorityFromRole(String roleCode, String authorityCode);

    List<RoleResponse> listRoles(String name, int page, int size);

    List<RoleSearchResponse> searchRoles(String name);

    AccessAssignmentResponse grantAccess(
            GrantAccessRequest request,
            String actorUserId,
            String actorScopeKey,
            String actorScopeId,
            Set<String> actorRoles
    );

    AccessAssignmentResponse changeAssignmentStatus(
            String assignmentId,
            String status,
            String actorUserId,
            String actorScopeKey,
            String actorScopeId,
            Set<String> actorRoles
    );

    List<AccessAssignmentResponse> listAssignmentsForUser(String userId, String actorScopeKey, String actorScopeId);

    List<AccessContextResponse> listAvailableContexts(String userId);

    SellerAuthResponse switchAccessContext(String userId, String targetAssignmentId, String currentRefreshToken);
}

