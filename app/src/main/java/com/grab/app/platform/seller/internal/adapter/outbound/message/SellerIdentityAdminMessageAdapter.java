package com.grab.app.platform.seller.internal.adapter.outbound.message;

import com.grab.app.account.identity.port.message.AccessAssignmentPort;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.*;
import com.grab.app.account.identity.port.message.IdentityAdminPort;
import com.grab.app.account.identity.port.message.IdentityAdminPort.UserDetailResult;
import com.grab.app.account.identity.port.message.IdentityAdminPort.UserSummaryResult;
import com.grab.app.account.identity.port.message.RolePermissionPort;
import com.grab.app.account.identity.port.message.RolePermissionPort.CreateRoleMessage;
import com.grab.app.account.identity.port.message.RolePermissionPort.RoleItemResult;
import com.grab.app.account.identity.port.message.RolePermissionPort.RoleSuggestionResult;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateRoleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.GrantAccessRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.*;
import com.grab.app.platform.seller.port.outbound.SellerIdentityAdminOutboundPort;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class SellerIdentityAdminMessageAdapter implements SellerIdentityAdminOutboundPort {

    private final IdentityAdminPort identityAdminPort;
    private final RolePermissionPort rolePermissionPort;
    private final AccessAssignmentPort accessAssignmentPort;
    private final IdGenerator idGenerator;

    public SellerIdentityAdminMessageAdapter(
            IdentityAdminPort identityAdminPort,
            RolePermissionPort rolePermissionPort,
            AccessAssignmentPort accessAssignmentPort,
            IdGenerator idGenerator
    ) {
        this.identityAdminPort = identityAdminPort;
        this.rolePermissionPort = rolePermissionPort;
        this.accessAssignmentPort = accessAssignmentPort;
        this.idGenerator = idGenerator;
    }

    @Override
    public Optional<UserProfileDetailResponse> findUserById(String userIdStr) {
        Id userId = idGenerator.convertIdFrom(userIdStr);
        return identityAdminPort.findUserById(userId).map(this::toUserDetailResponse);
    }

    @Override
    public List<SellerUserProfileResponse> listUsers(int page, int size) {
        List<UserSummaryResult> results = identityAdminPort.listUsers(page, size);
        return results.stream()
                .map(u -> new SellerUserProfileResponse(u.id(), u.email(), u.status(), u.createdAt()))
                .toList();
    }

    @Override
    public void changeUserStatus(String userIdStr, String status) {
        Id userId = idGenerator.convertIdFrom(userIdStr);
        identityAdminPort.changeUserStatus(userId, status);
    }

    @Override
    public RoleResponse createRole(CreateRoleRequest request) {
        CreateRoleMessage message = new CreateRoleMessage(
                request.code(),
                request.name(),
                request.description(),
                request.authorityCodes()
        );
        RoleItemResult result = rolePermissionPort.createRole(message);
        return toRoleResponse(result);
    }

    @Override
    public void grantAuthorityToRole(String roleCode, String authorityCode) {
        rolePermissionPort.grantAuthorityToRole(roleCode, authorityCode);
    }

    @Override
    public void revokeAuthorityFromRole(String roleCode, String authorityCode) {
        rolePermissionPort.revokeAuthorityFromRole(roleCode, authorityCode);
    }

    @Override
    public List<RoleResponse> listRoles(String name, int page, int size) {
        List<RoleItemResult> results = rolePermissionPort.listRoles(name, page, size);
        return results.stream().map(this::toRoleResponse).toList();
    }

    @Override
    public List<RoleSearchResponse> searchRoles(String name) {
        List<RoleSuggestionResult> results = rolePermissionPort.searchRoles(name);
        return results.stream()
                .map(r -> new RoleSearchResponse(r.id(), r.name(), r.code()))
                .toList();
    }

    @Override
    public AccessAssignmentResponse grantAccess(
            GrantAccessRequest request,
            String actorUserIdStr,
            String actorScopeKey,
            String actorScopeId,
            Set<String> actorRoles
    ) {
        Id userId = idGenerator.convertIdFrom(request.userId());
        Id actorUserId = (actorUserIdStr != null && !actorUserIdStr.isBlank())
                ? idGenerator.convertIdFrom(actorUserIdStr)
                : null;

        AssignAccessMessage message = new AssignAccessMessage(
                userId,
                request.roleCode(),
                request.scopeKey(),
                request.scopeId(),
                request.expiresAt(),
                actorUserId,
                actorScopeKey,
                actorScopeId,
                actorRoles
        );
        AssignmentMessageResult result = accessAssignmentPort.assignAccess(message);
        return toAssignmentResponse(result);
    }

    @Override
    public AccessAssignmentResponse changeAssignmentStatus(
            String assignmentIdStr,
            String status,
            String actorUserIdStr,
            String actorScopeKey,
            String actorScopeId,
            Set<String> actorRoles
    ) {
        Id assignmentId = idGenerator.convertIdFrom(assignmentIdStr);
        Id actorUserId = (actorUserIdStr != null && !actorUserIdStr.isBlank())
                ? idGenerator.convertIdFrom(actorUserIdStr)
                : null;

        ChangeAssignmentStatusMessage message = new ChangeAssignmentStatusMessage(
                assignmentId,
                status,
                actorScopeKey,
                actorScopeId,
                actorUserId,
                actorRoles
        );
        AssignmentMessageResult result = accessAssignmentPort.changeAssignmentStatus(message);
        return toAssignmentResponse(result);
    }

    @Override
    public List<AccessAssignmentResponse> listAssignmentsForUser(String userIdStr, String actorScopeKey, String actorScopeId) {
        Id userId = idGenerator.convertIdFrom(userIdStr);
        List<AssignmentMessageResult> results = accessAssignmentPort.listAssignmentsForUser(userId, actorScopeKey, actorScopeId);
        return results.stream().map(this::toAssignmentResponse).toList();
    }

    @Override
    public List<AccessContextResponse> listAvailableContexts(String userIdStr) {
        Id userId = idGenerator.convertIdFrom(userIdStr);
        List<ContextMessageResult> results = accessAssignmentPort.listAvailableContexts(userId);
        return results.stream()
                .map(c -> new AccessContextResponse(
                        c.assignmentId(),
                        c.roleCodes(),
                        c.scopeKey(),
                        c.scopeId(),
                        c.expiresAt(),
                        c.title(),
                        c.subtitle()
                ))
                .toList();
    }

    @Override
    public SellerAuthResponse switchAccessContext(String userIdStr, String targetAssignmentIdStr, String currentRefreshToken) {
        Id userId = idGenerator.convertIdFrom(userIdStr);
        Id targetAssignmentId = idGenerator.convertIdFrom(targetAssignmentIdStr);
        SwitchContextMessage message = new SwitchContextMessage(userId, targetAssignmentId, currentRefreshToken);
        AuthTokenResult result = accessAssignmentPort.switchAccessContext(message);
        return new SellerAuthResponse(
                result.accessToken(),
                result.refreshToken(),
                result.expiresInMs(),
                result.userId(),
                result.email(),
                result.roles(),
                result.status(),
                result.contextSelectionRequired()
        );
    }

    private UserProfileDetailResponse toUserDetailResponse(UserDetailResult result) {
        List<UserProfileDetailResponse.AccessContextInfo> contexts = (result.accessContexts() == null)
                ? List.of()
                : result.accessContexts().stream()
                .map(c -> new UserProfileDetailResponse.AccessContextInfo(
                        c.assignmentId(),
                        c.roleCode(),
                        c.scopeKey(),
                        c.scopeId(),
                        c.status()
                ))
                .toList();

        return new UserProfileDetailResponse(
                result.id(),
                result.email(),
                result.status(),
                result.createdAt(),
                contexts
        );
    }

    private RoleResponse toRoleResponse(RoleItemResult result) {
        return new RoleResponse(
                result.code(),
                result.name(),
                result.description(),
                result.kind(),
                result.active(),
                result.assignable(),
                result.authorities()
        );
    }

    private AccessAssignmentResponse toAssignmentResponse(AssignmentMessageResult result) {
        return new AccessAssignmentResponse(
                result.id(),
                result.userId(),
                result.roleCode(),
                result.scopeKey(),
                result.scopeId(),
                result.status(),
                result.assignedBy(),
                result.createdAt(),
                result.updatedAt(),
                result.expiresAt()
        );
    }
}

