package com.grab.store.identity.internal.event;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.security.AuthorityDefinition;
import com.grab.store.shared.events.identity.IdentityAuthorityManifestDeclaredIntegrationEvent;
import com.identity.domain.model.Authority;
import com.identity.domain.port.outbound.AuthorityRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityAuthorityManifestRegistrationListenerTest {

    @Test
    void registrationUsesManifestModuleKeyAsAuthorityCategory() {
        CapturingAuthorityRepository repository = new CapturingAuthorityRepository();
        IdentityAuthorityManifestRegistrationListener listener = new IdentityAuthorityManifestRegistrationListener(
                repository,
                new FixedIdGenerator()
        );

        listener.onAuthorityManifestDeclared(new IdentityAuthorityManifestDeclaredIntegrationEvent(
                1,
                List.of(new AuthorityDefinition("USER_READ", "User read", "Read users"))
        ));

        assertThat(repository.saved).singleElement().satisfies(authority -> {
            assertThat(authority.getId().getValue()).isEqualTo("authority-1");
            assertThat(authority.getCode()).isEqualTo("USER_READ");
            assertThat(authority.getCategory()).isEqualTo("identity");
            assertThat(authority.isActive()).isTrue();
        });
    }

    private static final class CapturingAuthorityRepository implements AuthorityRepository {
        private final List<Authority> saved = new ArrayList<>();

        @Override
        public Set<Authority> findActiveByCodes(Set<String> codes) {
            return Set.of();
        }

        @Override
        public void upsertAll(List<Authority> authorities) {
            saved.addAll(authorities);
        }
    }

    private static final class FixedIdGenerator implements IdGenerator {
        private int sequence;

        @Override
        public Id generateId() {
            return () -> "authority-" + (++sequence);
        }

        @Override
        public Id convertIdFrom(String id) {
            return () -> id;
        }
    }
}
