package com.identity.application;

import com.grab.framework.security.ScopeDeclaration;
import com.identity.domain.valueobject.ScopeHierarchy;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;

public class ScopeHierarchyTestExtension implements BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) {
        ScopeHierarchy.registerAll(List.of(
                new ScopeDeclaration("merchant.account", null),
                new ScopeDeclaration("merchant.storefront", "merchant.account"),
                new ScopeDeclaration("inventory.fulfillment-location", "merchant.account")
        ));
    }
}
