package com.catalog.application.model.read;

public record CheckProductPublishableResult(
        boolean found,
        boolean owned,
        boolean active
) {
    public boolean publishable() {
        return found && owned && active;
    }

    public static CheckProductPublishableResult missing() {
        return new CheckProductPublishableResult(false, false, false);
    }
}
