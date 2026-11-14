package com.grab.app.architecture;

import com.catalog.application.security.CatalogSecurityManifest;
import com.grab.framework.security.SecurityManifest;
import com.grab.framework.security.role.RoleDeclaration;
import com.identity.application.security.IdentitySecurityManifest;
import com.inventory.application.security.InventorySecurityManifest;
import com.merchant.application.security.MerchantAdminAccessProfile;
import com.merchant.application.security.MerchantSecurityManifest;
import com.saleschannel.application.security.SalesChannelSecurityManifest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards against changing a security manifest or role declaration without bumping its revision. Identity treats
 * "same revision, different digest" as a CONFLICT, which fails the publishing module's bootstrap task in every
 * environment that already holds the old digest.
 * <p>
 * When this test fails after an intentional change: bump the revision constant, then add the new
 * {@code <key>.<revision>=<digest>} line printed below to {@code security-manifest-digests.properties}.
 * Never edit the digest of an existing revision.
 */
class SecurityManifestDigestSnapshotTest {

    private static final String SNAPSHOT = "/security-manifest-digests.properties";

    @Test
    void everyPublishedRevisionMatchesItsRecordedDigest() throws IOException {
        Properties snapshot = loadSnapshot();
        Map<String, String> current = currentDigests();
        List<String> problems = new ArrayList<>();
        for (Map.Entry<String, String> entry : current.entrySet()) {
            String recorded = snapshot.getProperty(entry.getKey());
            if (recorded == null) {
                problems.add("missing snapshot, add: " + entry.getKey() + "=" + entry.getValue());
            } else if (!recorded.equals(entry.getValue())) {
                problems.add("digest changed without a revision bump: " + entry.getKey()
                        + " recorded=" + recorded + " current=" + entry.getValue());
            }
        }
        assertTrue(problems.isEmpty(), String.join(System.lineSeparator(), problems));
    }

    private static Map<String, String> currentDigests() {
        Map<String, String> digests = new LinkedHashMap<>();
        manifest(digests, CatalogSecurityManifest.CURRENT);
        manifest(digests, InventorySecurityManifest.CURRENT);
        manifest(digests, IdentitySecurityManifest.CURRENT);
        manifest(digests, MerchantSecurityManifest.CURRENT);
        manifest(digests, SalesChannelSecurityManifest.CURRENT);
        roleDeclaration(digests, MerchantAdminAccessProfile.DECLARATION);
        return digests;
    }

    private static void manifest(Map<String, String> digests, SecurityManifest manifest) {
        String key = "manifest." + manifest.moduleKey() + "." + manifest.securityRevision();
        digests.put(key, manifest.contentDigest());
    }

    private static void roleDeclaration(Map<String, String> digests, RoleDeclaration declaration) {
        String key = "role." + declaration.owner() + "." + declaration.roleCode() + "."
                + declaration.declarationRevision();
        digests.put(key, declaration.contentDigest());
    }

    private static Properties loadSnapshot() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = SecurityManifestDigestSnapshotTest.class.getResourceAsStream(SNAPSHOT)) {
            assertNotNull(input, SNAPSHOT + " is missing");
            properties.load(input);
        }
        return properties;
    }
}
