package com.grab.store.merchant.internal.config;

import com.catalog.application.security.CatalogAuthorityManifest;
import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.role.RoleDeclaration;
import com.grab.framework.security.role.RolePermissionReference;
import com.inventory.application.security.InventoryAuthorityManifest;
import com.merchant.application.security.MerchantAdminAccessProfile;
import com.merchant.application.security.MerchantAuthorityManifest;
import com.saleschannel.application.security.SalesChannelAuthorityManifest;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantAdminDeclarationContractTest {

    @Test
    void verifyMerchantAdminDeclarationMatchesUpstreamManifests() {
        RoleDeclaration declaration = MerchantAdminAccessProfile.DECLARATION;
        assertThat(declaration.roleCode()).isEqualTo("MERCHANT_ADMIN");
        assertThat(declaration.owner()).isEqualTo("merchant");
        assertThat(declaration.assignmentScopeKey()).isEqualTo("merchant.account");
        assertThat(declaration.permissions()).hasSize(10);

        // Catalog
        Set<String> catalogCodes = CatalogAuthorityManifest.CURRENT.definitions().stream()
                .map(AuthorityDefinition::code).collect(Collectors.toSet());
        declaration.permissions().stream()
                .filter(ref -> "catalog".equals(ref.owner()))
                .forEach(ref -> assertThat(catalogCodes).contains(ref.code()));

        // Inventory
        Set<String> inventoryCodes = InventoryAuthorityManifest.CURRENT.definitions().stream()
                .map(AuthorityDefinition::code).collect(Collectors.toSet());
        declaration.permissions().stream()
                .filter(ref -> "inventory".equals(ref.owner()))
                .forEach(ref -> assertThat(inventoryCodes).contains(ref.code()));

        // SalesChannel
        Set<String> salesChannelCodes = SalesChannelAuthorityManifest.CURRENT.definitions().stream()
                .map(AuthorityDefinition::code).collect(Collectors.toSet());
        declaration.permissions().stream()
                .filter(ref -> "saleschannel".equals(ref.owner()))
                .forEach(ref -> assertThat(salesChannelCodes).contains(ref.code()));

        // Merchant
        Set<String> merchantCodes = MerchantAuthorityManifest.CURRENT.definitions().stream()
                .map(AuthorityDefinition::code).collect(Collectors.toSet());
        declaration.permissions().stream()
                .filter(ref -> "merchant".equals(ref.owner()))
                .forEach(ref -> assertThat(merchantCodes).contains(ref.code()));
    }
}
