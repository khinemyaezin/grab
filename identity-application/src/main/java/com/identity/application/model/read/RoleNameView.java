package com.identity.application.model.read;

public record RoleNameView(Long id, String name, String code) implements RoleView {
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCode() {
        return code;
    }
}
