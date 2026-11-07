package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

public record UpdateMemberRoleRequest(
        String role,
        String newRole
) {
    public String resolveRole() {
        if (role != null && !role.isBlank()) {
            return role;
        }
        return newRole;
    }
}
