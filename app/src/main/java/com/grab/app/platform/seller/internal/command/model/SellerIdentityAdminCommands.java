package com.grab.app.platform.seller.internal.command.model;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateRoleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.GrantAccessRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AccessAssignmentResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.RoleResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerAuthResponse;
import com.grab.framework.cqrs.command.Command;

import java.util.Set;

public final class SellerIdentityAdminCommands {

    private SellerIdentityAdminCommands() {}

    public record ChangeUserStatusCommand(
            String userId,
            String status
    ) implements Command<Void> {}

    public record CreateRoleCommand(
            CreateRoleRequest request
    ) implements Command<RoleResponse> {}

    public record GrantAuthorityCommand(
            String roleCode,
            String authorityCode
    ) implements Command<Void> {}

    public record RevokeAuthorityCommand(
            String roleCode,
            String authorityCode
    ) implements Command<Void> {}

    public record GrantAccessCommand(
            GrantAccessRequest request,
            String actorUserId,
            String actorScopeKey,
            String actorScopeId,
            Set<String> actorRoles
    ) implements Command<AccessAssignmentResponse> {}

    public record ChangeAssignmentStatusCommand(
            String assignmentId,
            String status,
            String actorUserId,
            String actorScopeKey,
            String actorScopeId,
            Set<String> actorRoles
    ) implements Command<AccessAssignmentResponse> {}

    public record SwitchAccessContextCommand(
            String userId,
            String targetAssignmentId,
            String currentRefreshToken
    ) implements Command<SellerAuthResponse> {}
}

