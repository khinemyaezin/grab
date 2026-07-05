package com.grab.store.identity.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.identity.internal.command.CreateRoleCommand;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.Role;
import com.identity.domain.repository.AuthorityRepository;
import com.identity.domain.repository.PlatformRepository;
import com.identity.domain.repository.RoleRepository;
import com.identity.domain.policy.impl.RoleAdministrationPolicy;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateRoleCommandHandlerTest {
    @Test
    void handle_withSupportedPlatformAuthorities_shouldCreateAndBindCustomRole() {
        InMemoryRoleRepository roles = new InMemoryRoleRepository();
        InMemoryPlatformRepository platforms = new InMemoryPlatformRepository(sellerPlatform());
        AuthorityRepository authorities = new FixedAuthorityRepository(Set.of(
                "MERCHANT_PROFILE_READ",
                "MERCHANT_PROFILE_WRITE"
        ));
        CreateRoleCommandHandler handler = new CreateRoleCommandHandler(
                roles,
                platforms,
                new RoleAdministrationPolicy(authorities),
                fixedIds()
        );
        CreateRoleCommand command = new CreateRoleCommand(
                "PROFILE_EDITOR",
                "Profile Editor",
                null,
                "SELLER_PORTAL",
                Set.of("MERCHANT_PROFILE_READ", "MERCHANT_PROFILE_WRITE")
        );

        var result = handler.handle(command);

        assertThat(result.kind()).isEqualTo("CUSTOM");
        assertThat(result.platformCodes()).containsExactly("SELLER_PORTAL");
        assertThat(result.authorities())
                .containsExactlyInAnyOrder("MERCHANT_PROFILE_READ", "MERCHANT_PROFILE_WRITE");
        assertThat(platforms.platform.getRoleCodes()).contains("PROFILE_EDITOR");
        assertThat(roles.findByCode("PROFILE_EDITOR")).isPresent();
    }

    private Platform sellerPlatform() {
        return new Platform(
                new CommonId("seller-platform"),
                "SELLER_PORTAL",
                "Seller Portal",
                true,
                Set.of(),
                Set.of("MERCHANT_PROFILE_READ", "MERCHANT_PROFILE_WRITE")
        );
    }

    private IdGenerator fixedIds() {
        return new IdGenerator() {
            @Override
            public Id generateId() {
                return new CommonId("role-1");
            }

            @Override
            public Id convertIdFrom(String id) {
                return new CommonId(id);
            }
        };
    }

    private static final class InMemoryRoleRepository implements RoleRepository {
        private final Map<String, Role> values = new LinkedHashMap<>();

        @Override
        public Optional<Role> findByCode(String code) {
            return Optional.ofNullable(values.get(code));
        }

        @Override
        public Set<Role> findByCodes(Set<String> codes) {
            LinkedHashSet<Role> found = new LinkedHashSet<>();
            codes.forEach(code -> findByCode(code).ifPresent(found::add));
            return Set.copyOf(found);
        }

        @Override
        public Role save(Role role) {
            values.put(role.getCode(), role);
            return role;
        }
    }

    private static final class InMemoryPlatformRepository implements PlatformRepository {
        private Platform platform;

        private InMemoryPlatformRepository(Platform platform) {
            this.platform = platform;
        }

        @Override
        public Optional<Platform> findByCode(String code) {
            return platform.getCode().equals(code) ? Optional.of(platform) : Optional.empty();
        }

        @Override
        public Set<Platform> findByRoleCode(String roleCode) {
            return platform.getRoleCodes().contains(roleCode) ? Set.of(platform) : Set.of();
        }

        @Override
        public Platform save(Platform platform) {
            this.platform = platform;
            return platform;
        }
    }

    private record FixedAuthorityRepository(Set<String> activeCodes) implements AuthorityRepository {
        @Override
        public boolean existsByCode(String code) {
            return activeCodes.contains(code);
        }

        @Override
        public Set<String> findActiveCodes(Set<String> codes) {
            LinkedHashSet<String> found = new LinkedHashSet<>(codes);
            found.retainAll(activeCodes);
            return Set.copyOf(found);
        }
    }
}
