package com.grab.app.shared.outbox;

import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.security.SecurityManifestPublicationPort;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ObservabilityBeanPostProcessorTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final ObservabilityBeanPostProcessor postProcessor = new ObservabilityBeanPostProcessor(provider(registry));

    @Test
    void inboxDecorator_countsDuplicatesPerConsumer() {
        ProcessedEventRepository inbox = new ProcessedEventRepository() {
            private boolean seen;

            @Override
            public boolean markProcessed(String consumer, UUID eventId) {
                boolean first = !seen;
                seen = true;
                return first;
            }

            @Override
            public int deleteProcessedBefore(Instant cutoff, int limit) {
                return 0;
            }
        };
        ProcessedEventRepository observed = (ProcessedEventRepository) postProcessor
                .postProcessAfterInitialization(inbox, "catalogProcessedEventRepository");
        UUID eventId = UUID.randomUUID();

        assertThat(observed.markProcessed("customer.register", eventId)).isTrue();
        assertThat(observed.markProcessed("customer.register", eventId)).isFalse();

        assertThat(registry.counter("inbox.duplicate", "consumer", "customer.register").count()).isEqualTo(1.0);
    }

    @Test
    void manifestDecorator_countsResultByModule() {
        SecurityManifestPublicationPort port = manifest -> SecurityManifestPublicationPort.PublicationResult.CONFLICT;
        SecurityManifestPublicationPort observed = (SecurityManifestPublicationPort) postProcessor
                .postProcessAfterInitialization(port, "catalogSecurityManifestPublicationPort");

        SecurityManifestPublicationPort.PublicationResult result = observed.enqueue(null);

        assertThat(result).isEqualTo(SecurityManifestPublicationPort.PublicationResult.CONFLICT);
        double count = registry.counter("security.manifest.publication.result",
                "module", "unknown", "kind", "manifest", "result", "CONFLICT").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void otherBeans_areReturnedUnchanged() {
        Object bean = new Object();

        assertThat(postProcessor.postProcessAfterInitialization(bean, "x")).isSameAs(bean);
    }

    private static ObjectProvider<MeterRegistry> provider(MeterRegistry registry) {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        factory.registerSingleton("meterRegistry", registry);
        return factory.getBeanProvider(MeterRegistry.class);
    }
}
