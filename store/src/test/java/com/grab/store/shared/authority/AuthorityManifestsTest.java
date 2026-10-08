package com.grab.store.shared.authority;

import com.catalog.application.security.CatalogAuthorityManifest;
import com.grab.framework.security.AuthorityManifest;
import com.identity.application.security.IdentityAuthorityManifest;
import com.inventory.application.security.InventoryAuthorityManifest;
import com.merchant.application.security.MerchantAuthorityManifest;
import com.saleschannel.application.security.SalesChannelAuthorityManifest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorityManifestsTest {

    @Test
    void declaresEverySeededAuthorityExactlyOnceInItsOwningModule() {
        Map<String, AuthorityManifest> manifests = List.of(
                        IdentityAuthorityManifest.CURRENT,
                        MerchantAuthorityManifest.CURRENT,
                        InventoryAuthorityManifest.CURRENT,
                        CatalogAuthorityManifest.CURRENT,
                        SalesChannelAuthorityManifest.CURRENT
                ).stream()
                .collect(Collectors.toMap(AuthorityManifest::moduleKey, Function.identity()));

        assertThat(manifests).containsOnlyKeys("identity", "merchant", "inventory", "catalog", "saleschannel");
        assertThat(codes(manifests.get("identity"))).containsExactlyInAnyOrder(
                "USER_READ", "USER_WRITE", "ROLE_READ", "ROLE_WRITE",
                "ACCESS_ASSIGNMENT_READ", "ACCESS_ASSIGNMENT_WRITE", "ACCESS_INVITATION_WRITE"
        );
        assertThat(codes(manifests.get("merchant"))).containsExactlyInAnyOrder(
                "MERCHANT_GLOBAL_READ", "MERCHANT_LIFECYCLE_WRITE", "MERCHANT_APPLICATION_WRITE",
                "MERCHANT_PROFILE_WRITE", "MERCHANT_PROFILE_READ", "MERCHANT_STOREFRONT_READ",
                "MERCHANT_STOREFRONT_WRITE"
        );
        assertThat(codes(manifests.get("inventory"))).containsExactlyInAnyOrder("INVENTORY_READ", "INVENTORY_WRITE");
        assertThat(codes(manifests.get("catalog"))).containsExactlyInAnyOrder("CATALOG_READ", "CATALOG_WRITE");
        assertThat(codes(manifests.get("saleschannel"))).containsExactlyInAnyOrder(
                "SALES_CHANNEL_READ", "SALES_CHANNEL_WRITE"
        );

        List<String> allCodes = manifests.values().stream()
                .flatMap(manifest -> manifest.definitions().stream())
                .map(definition -> definition.code())
                .toList();
        assertThat(new HashSet<>(allCodes)).hasSameSizeAs(allCodes);
        assertThat(allCodes).hasSize(20);
    }

    @Test
    void contentDigestIsStableAcrossDefinitionOrderAndChangesWithMetadata() {
        var definition = new com.grab.framework.security.AuthorityDefinition("A", "Alpha", "first");
        var other = new com.grab.framework.security.AuthorityDefinition("B", "Beta", "second");
        AuthorityManifest ordered = new AuthorityManifest("test", 1, List.of(definition, other));
        AuthorityManifest reordered = new AuthorityManifest("test", 1, List.of(other, definition));
        AuthorityManifest changed = new AuthorityManifest("test", 1, List.of(
                definition, new com.grab.framework.security.AuthorityDefinition("B", "Beta", "updated")));

        assertThat(ordered.contentDigest()).isEqualTo(reordered.contentDigest());
        assertThat(ordered.contentDigest()).isNotEqualTo(changed.contentDigest());
    }

    private List<String> codes(AuthorityManifest manifest) {
        return manifest.definitions().stream().map(definition -> definition.code()).toList();
    }
}
