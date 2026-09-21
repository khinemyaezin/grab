package com.identity.application.model.read;

import java.util.List;

public record SearchRolesResult(
        List<Role> roles
) {
    public record Role(
            Long id,
            String name,
            String code
    ){}
}
