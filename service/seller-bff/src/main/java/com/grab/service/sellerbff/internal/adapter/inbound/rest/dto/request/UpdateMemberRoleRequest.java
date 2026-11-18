package com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request;

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
