package com.identity.application.model.read;

public record UserListView(
        String uuid,
        String email,
        String status,
        String createdAt
) {
}
