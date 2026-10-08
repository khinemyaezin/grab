package com.identity.application.service;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.security.AuthorityDefinition;
import com.identity.application.model.write.RegisterAuthorityManifestCommand;
import com.identity.domain.aggregate.Authority;
import com.identity.domain.port.outbound.AuthorityRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterAuthorityManifestServiceTest {

    @Test
    void mapsDefinitionsToAuthoritiesAndUpsertsThem() {
        CapturingAuthorityRepository repository = new CapturingAuthorityRepository();
        CountingIdGenerator ids = new CountingIdGenerator();
        RegisterAuthorityManifestService service = new RegisterAuthorityManifestService(repository, ids);
        var definitions = List.of(
                new AuthorityDefinition("USER_READ", "User read", "Read users"),
                new AuthorityDefinition("USER_WRITE", "User write", "Manage users")
        );

        service.execute(new RegisterAuthorityManifestCommand("identity", 3, definitions));

        List<Authority> saved = repository.saved;
        assertThat(saved).hasSize(2);
        assertThat(saved).extracting(Authority::getCode).containsExactly("USER_READ", "USER_WRITE");
        assertThat(saved).extracting(Authority::getCategory).containsOnly("identity");
        assertThat(saved).extracting(authority -> authority.getId().getValue())
                .containsExactly("authority-1", "authority-2");
        assertThat(saved).allSatisfy(authority -> assertThat(authority.isActive()).isTrue());
        assertThat(ids.sequence).isEqualTo(2);
    }

    @Test
    void upsertsAnEmptyManifestAsAnEmptyBatch() {
        CapturingAuthorityRepository repository = new CapturingAuthorityRepository();
        RegisterAuthorityManifestService service = new RegisterAuthorityManifestService(repository, new CountingIdGenerator());

        service.execute(new RegisterAuthorityManifestCommand("identity", 1, List.of()));

        assertThat(repository.upsertCount).isEqualTo(1);
        assertThat(repository.saved).isEmpty();
    }

    @Test
    void propagatesRepositoryFailures() {
        RuntimeException failure = new RuntimeException("authority persistence failed");
        AuthorityRepository repository = new AuthorityRepository() {
            @Override
            public Set<Authority> findActiveByCodes(Set<String> codes) {
                return Set.of();
            }

            @Override
            public void upsertAll(List<Authority> authorities) {
                throw failure;
            }
        };
        RegisterAuthorityManifestService service = new RegisterAuthorityManifestService(repository, new CountingIdGenerator());

        assertThatThrownBy(() -> service.execute(
                new RegisterAuthorityManifestCommand("identity", 1, List.of())
        )).isSameAs(failure);
    }

    private static final class CapturingAuthorityRepository implements AuthorityRepository {
        private List<Authority> saved = List.of();
        private int upsertCount;

        @Override
        public Set<Authority> findActiveByCodes(Set<String> codes) {
            return Set.of();
        }

        @Override
        public void upsertAll(List<Authority> authorities) {
            this.saved = new ArrayList<>(authorities);
            upsertCount++;
        }
    }

    private static final class CountingIdGenerator implements IdGenerator {
        private int sequence;

        @Override
        public com.grab.framework.id.Id generateId() {
            return () -> "authority-" + (++sequence);
        }

        @Override
        public com.grab.framework.id.Id convertIdFrom(String id) {
            return () -> id;
        }
    }
}
