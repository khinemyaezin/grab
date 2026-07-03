package com.grab.store.identity.internal.config;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.merchant.internal.api.rest.config.MerchantAuthorityCodes;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.Role;
import com.identity.domain.enums.RoleKind;
import com.identity.domain.repository.PlatformRepository;
import com.identity.domain.repository.RoleRepository;
import com.identity.domain.service.IdentityAccessProfile;
import com.identity.domain.service.MerchantAccessProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SystemAccessCatalogValidator implements ApplicationRunner {
    private static final Logger log = Loggers.getLogger(SystemAccessCatalogValidator.class);

    private final RoleRepository roles;
    private final PlatformRepository platforms;

    @Override
    @IdentityReadTransactional
    public void run(ApplicationArguments args) {
        requiredRoles().forEach(this::requireSystemRole);
        log.info("Validated system role and authority catalog");
    }

    private void requireSystemRole(RequiredRole required) {
        Role role = roles.findByCode(required.roleCode()).orElseThrow(() -> invalidCatalog(
                "Missing system role " + required.roleCode()
        ));
        if (role.getKind() != RoleKind.SYSTEM || !role.isActive() || !role.isAssignable()) {
            throw invalidCatalog("Invalid system role state for " + required.roleCode());
        }
        if (!role.getAuthorityCodes().equals(required.authorityCodes())) {
            throw invalidCatalog("Invalid authority mapping for " + required.roleCode());
        }
        Platform platform = platforms.findByCode(required.platformCode()).orElseThrow(() -> invalidCatalog(
                "Missing platform " + required.platformCode()
        ));
        if (!platform.supportsRole(required.roleCode())) {
            throw invalidCatalog(required.roleCode() + " is not available on " + required.platformCode());
        }
        if (!platform.getAuthorityCodes().containsAll(required.authorityCodes())) {
            throw invalidCatalog("Platform authority catalog is incomplete for " + required.platformCode());
        }
    }

    private IllegalStateException invalidCatalog(String detail) {
        return new IllegalStateException("Identity access catalog validation failed: " + detail);
    }

    private List<RequiredRole> requiredRoles() {
        return List.of(
                new RequiredRole(
                        IdentityAccessProfile.CUSTOMER_ROLE_CODE,
                        IdentityAccessProfile.CUSTOMER_PLATFORM_CODE,
                        Set.of(MerchantAuthorityCodes.APPLICATION_WRITE)
                ),
                new RequiredRole(
                        MerchantAccessProfile.APPLICANT_ROLE_CODE,
                        MerchantAccessProfile.SELLER_PLATFORM_CODE,
                        Set.of(
                                MerchantAuthorityCodes.APPLICATION_WRITE,
                                MerchantAuthorityCodes.PROFILE_WRITE,
                                MerchantAuthorityCodes.PROFILE_READ
                        )
                ),
                new RequiredRole(
                        MerchantAccessProfile.OWNER_ROLE_CODE,
                        MerchantAccessProfile.SELLER_PLATFORM_CODE,
                        Set.of(
                                IdentityAuthorityCodes.ACCESS_ASSIGNMENT_READ,
                                IdentityAuthorityCodes.ACCESS_ASSIGNMENT_WRITE,
                                IdentityAuthorityCodes.ACCESS_INVITATION_WRITE,
                                MerchantAuthorityCodes.PROFILE_WRITE,
                                MerchantAuthorityCodes.PROFILE_READ
                        )
                ),
                new RequiredRole(
                        IdentityAccessProfile.USER_ADMIN_ROLE_CODE,
                        IdentityAccessProfile.ADMIN_PLATFORM_CODE,
                        Set.of(
                                IdentityAuthorityCodes.USER_READ,
                                IdentityAuthorityCodes.USER_WRITE,
                                IdentityAuthorityCodes.ROLE_READ,
                                IdentityAuthorityCodes.ROLE_WRITE,
                                IdentityAuthorityCodes.ACCESS_ASSIGNMENT_READ,
                                IdentityAuthorityCodes.ACCESS_ASSIGNMENT_WRITE,
                                IdentityAuthorityCodes.ACCESS_INVITATION_WRITE,
                                MerchantAuthorityCodes.GLOBAL_READ,
                                MerchantAuthorityCodes.LIFECYCLE_WRITE,
                                MerchantAuthorityCodes.PROFILE_READ
                        )
                )
        );
    }

    private record RequiredRole(String roleCode, String platformCode, Set<String> authorityCodes) {
    }
}
