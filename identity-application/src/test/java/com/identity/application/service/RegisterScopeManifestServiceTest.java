package com.identity.application.service;

import com.grab.framework.security.ScopeDeclaration;
import com.identity.application.model.write.RegisterScopeManifestCommand;
import com.identity.domain.valueobject.ScopeHierarchy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterScopeManifestServiceTest {
    @Test
    void registersScopeDeclarationsWithoutIdentityKnowingModuleKeys() {
        var service = new RegisterScopeManifestService();
        var declarations = List.of(
                new ScopeDeclaration("test.account", null),
                new ScopeDeclaration("test.store", "test.account")
        );

        service.execute(new RegisterScopeManifestCommand("test", 1, declarations));

        assertThat(ScopeHierarchy.isRegistered("test.account")).isTrue();
        assertThat(ScopeHierarchy.parentOf("test.store")).contains("test.account");
        assertThat(ScopeHierarchy.isAncestorOrSelf("test.account", "test.store")).isTrue();
    }
}
