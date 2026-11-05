package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record InviteMemberRequest(
        @NotBlank(message = "Target user ID is required")
        String targetUserId,
        @NotBlank(message = "Role is required")
        String role,
        Instant expiresAt
) {}
