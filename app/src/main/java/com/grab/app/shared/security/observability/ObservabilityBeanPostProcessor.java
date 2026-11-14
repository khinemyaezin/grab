package com.grab.app.shared.security.observability;

import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.framework.security.role.RoleDeclarationPublicationPort;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Wraps each security publication port with a metrics decorator, so no handler or adapter has to
 * depend on Micrometer. The registry is resolved lazily to avoid initialising it inside the post-processor phase.
 */
class ObservabilityBeanPostProcessor implements BeanPostProcessor {

    private final ObjectProvider<MeterRegistry> meterRegistry;

    ObservabilityBeanPostProcessor(ObjectProvider<MeterRegistry> meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof SecurityManifestPublicationPort port) {
            return ObservedPublicationPorts.manifest(port, meterRegistry);
        }
        if (bean instanceof RoleDeclarationPublicationPort port) {
            return ObservedPublicationPorts.role(port, meterRegistry);
        }
        return bean;
    }
}
