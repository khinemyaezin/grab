package com.grab.store.identity.internal.query;

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
