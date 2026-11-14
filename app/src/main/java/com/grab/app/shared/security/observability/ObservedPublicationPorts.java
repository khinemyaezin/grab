package com.grab.app.shared.security.observability;

import com.grab.framework.security.SecurityManifest;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.framework.security.role.RoleDeclaration;
import com.grab.framework.security.role.RoleDeclarationPublicationPort;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Counts {@code security.manifest.publication.result} (tags {@code module}, {@code kind}, {@code result})
 * for every publish attempt: bootstrap, scheduled repair and event-triggered. Alert on {@code result="CONFLICT"}.
 */
final class ObservedPublicationPorts {

    private static final String METRIC = "security.manifest.publication.result";

    private ObservedPublicationPorts() {
    }

    static SecurityManifestPublicationPort manifest(
            SecurityManifestPublicationPort delegate, ObjectProvider<MeterRegistry> meterRegistry) {
        return manifest -> {
            SecurityManifestPublicationPort.PublicationResult result = delegate.enqueue(manifest);
            String module = moduleOf(manifest);
            record(meterRegistry, module, "manifest", result);
            return result;
        };
    }

    static RoleDeclarationPublicationPort role(
            RoleDeclarationPublicationPort delegate, ObjectProvider<MeterRegistry> meterRegistry) {
        return declaration -> {
            RoleDeclarationPublicationPort.PublicationResult result = delegate.enqueue(declaration);
            String module = ownerOf(declaration);
            record(meterRegistry, module, "role", result);
            return result;
        };
    }

    private static String moduleOf(SecurityManifest manifest) {
        return manifest == null ? "unknown" : manifest.moduleKey();
    }

    private static String ownerOf(RoleDeclaration declaration) {
        return declaration == null ? "unknown" : declaration.owner();
    }

    private static void record(ObjectProvider<MeterRegistry> meterRegistry, String module, String kind, Enum<?> result) {
        String resultName = result == null ? "NONE" : result.name();
        meterRegistry.ifAvailable(registry ->
                registry.counter(METRIC, "module", module, "kind", kind, "result", resultName).increment());
    }
}
