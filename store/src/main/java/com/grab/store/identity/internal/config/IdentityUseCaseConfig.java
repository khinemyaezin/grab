package com.grab.store.identity.internal.config;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.security.PlatformIdentityResolver;
import com.identity.application.port.inbound.*;
import com.identity.application.port.outbound.AccessAssignmentQueryPort;
import com.identity.application.port.outbound.IdentityLookupQueryPort;
import com.identity.application.port.outbound.RoleQueryPort;
import com.identity.application.port.outbound.ScopeOwnershipPort;
import com.identity.application.port.outbound.UserQueryPort;
import com.identity.application.service.*;
import com.identity.domain.policy.RoleDelegationPolicy;
import com.identity.domain.policy.impl.RoleAdministrationPolicy;
import com.identity.domain.policy.impl.RuleBasedRoleDelegationPolicy;
import com.identity.domain.port.outbound.*;
import com.identity.domain.service.InvitationTokenService;
import com.identity.domain.service.PasswordHasher;
import com.identity.domain.service.TokenLifeCycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityUseCaseConfig {

    @Bean
    public InvitationTokenService invitationTokenService() {
        return new InvitationTokenService();
    }

    @Bean
    public RoleDelegationPolicy roleDelegationPolicy(RoleDelegationRuleRepository rules) {
        return new RuleBasedRoleDelegationPolicy(rules);
    }

    @Bean
    public RoleAdministrationPolicy roleAdministrationPolicy(AuthorityRepository authorities) {
        return new RoleAdministrationPolicy(authorities);
    }

    @Bean
    public AcceptAccessInvitationUseCase acceptAccessInvitationUseCase(
            AccessInvitationRepository invitations,
            AccessAssignmentRepository assignments,
            RoleRepository roles,
            InvitationTokenService invitationTokens,
            IdGenerator ids
    ) {
        return new AcceptAccessInvitationService(invitations, assignments, roles, invitationTokens, ids);
    }

    @Bean
    public CancelAccessInvitationUseCase cancelAccessInvitationUseCase(
            AccessInvitationRepository invitations,
            RoleDelegationPolicy delegationPolicy
    ) {
        return new CancelAccessInvitationService(invitations, delegationPolicy);
    }

    @Bean
    public ChangeAccessStatusUseCase changeAccessStatusUseCase(
            AccessAssignmentRepository assignments,
            SessionStore sessions,
            RoleDelegationPolicy delegationPolicy
    ) {
        return new ChangeAccessStatusService(assignments, sessions, delegationPolicy);
    }

    @Bean
    public ChangeUserStatusUseCase changeUserStatusUseCase(
            UserRepository userRepository,
            TokenLifeCycle tokenLifeCycle
    ) {
        return new ChangeUserStatusService(userRepository, tokenLifeCycle);
    }

    @Bean
    public CreateAccessInvitationUseCase createAccessInvitationUseCase(
            RoleRepository roles,
            UserRepository users,
            AccessInvitationRepository invitations,
            RoleDelegationPolicy delegationPolicy,
            InvitationTokenService invitationTokens,
            IdGenerator ids
    ) {
        return new CreateAccessInvitationService(roles, users, invitations, delegationPolicy, invitationTokens, ids);
    }

    @Bean
    public CreateRoleUseCase createRoleUseCase(
            RoleRepository roleRepository,
            RoleAdministrationPolicy roleAdministrationPolicy,
            IdGenerator idGenerator
    ) {
        return new CreateRoleService(roleRepository, roleAdministrationPolicy, idGenerator);
    }

    @Bean
    public GetUserProfileUseCase getUserProfileUseCase(UserQueryPort userQueryPort) {
        return new GetUserProfileService(userQueryPort);
    }

    @Bean
    public GrantAccessUseCase grantAccessUseCase(
            UserRepository users,
            RoleRepository roles,
            AccessAssignmentRepository assignments,
            RoleDelegationPolicy delegationPolicy,
            IdGenerator ids,
            ScopeOwnershipPort scopeOwnershipPort
    ) {
        return new GrantAccessService(users, roles, assignments, delegationPolicy, ids, scopeOwnershipPort);
    }

    @Bean
    public ListAccessAssignmentsUseCase listAccessAssignmentsUseCase(AccessAssignmentQueryPort assignments) {
        return new ListAccessAssignmentsService(assignments);
    }

    @Bean
    public ListAccessContextsUseCase listAccessContextsUseCase(
            AccessAssignmentQueryPort assignments
    ) {
        return new ListAccessContextsService(assignments);
    }

    @Bean
    public ListRolesUseCase listRolesUseCase(RoleQueryPort roleQueryPort) {
        return new ListRolesService(roleQueryPort);
    }

    @Bean
    public ListUsersUseCase listUsersUseCase(UserQueryPort userQueryPort) {
        return new ListUsersService(userQueryPort);
    }

    @Bean
    public LoginUseCase loginUseCase(
            UserRepository userRepository,
            AccessAssignmentRepository accessAssignments,
            PasswordHasher passwordHasher,
            TokenLifeCycle tokenLifeCycle,
            PlatformIdentityResolver identityResolver
    ) {
        return new LoginService(userRepository, accessAssignments, passwordHasher, tokenLifeCycle, identityResolver);
    }

    @Bean
    public LogoutUseCase logoutUseCase(TokenLifeCycle tokenLifeCycle) {
        return new LogoutService(tokenLifeCycle);
    }

    @Bean
    public ManageAuthorityUseCase manageAuthorityUseCase(
            RoleRepository roleRepository,
            RoleAdministrationPolicy roleAdministrationPolicy
    ) {
        return new ManageAuthorityService(roleRepository, roleAdministrationPolicy);
    }

    @Bean
    public RegisterAuthorityManifestUseCase registerAuthorityManifestUseCase(
            AuthorityRepository authorityRepository,
            IdGenerator idGenerator,
            AuthorityManifestVersionRepository versionRepository
    ) {
        return new RegisterAuthorityManifestService(authorityRepository, idGenerator, versionRepository);
    }

    @Bean
    public RegisterScopeManifestUseCase registerScopeManifestUseCase(ScopeManifestRepository repository) {
        return new RegisterScopeManifestService(repository);
    }

    @Bean
    public RegisterSecurityManifestUseCase registerSecurityManifestUseCase(
            SecurityManifestInboxRepository inbox,
            ScopeManifestRepository scopeManifestRepository,
            SecurityCatalogLock securityCatalogLock,
            SecurityManifestRevisionRepository securityManifestRevisionRepository,
            AuthorityRepository authorityRepository,
            SecurityManifestModuleRepository securityManifestModuleRepository,
            SecurityManifestCatalogRepository securityManifestCatalogRepository
    ) {
        return new RegisterSecurityManifestService(
                inbox,
                scopeManifestRepository,
                securityCatalogLock,
                securityManifestRevisionRepository,
                authorityRepository,
                securityManifestModuleRepository,
                securityManifestCatalogRepository
        );
    }

    @Bean
    public RevalidateWaitingSecurityManifestsUseCase revalidateWaitingSecurityManifestsUseCase(
            SecurityManifestRevisionRepository revisions,
            RegisterSecurityManifestUseCase registration
    ) {
        return new RevalidateWaitingSecurityManifestsService(revisions, registration);
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(TokenLifeCycle tokenLifeCycle) {
        return new RefreshTokenService(tokenLifeCycle);
    }

    @Bean
    public RegisterUseCase registerUseCase(
            UserRepository users,
            AccessAssignmentRepository accessAssignments,
            PasswordHasher passwordHasher,
            IdGenerator idGenerator
    ) {
        return new RegisterService(users, accessAssignments, passwordHasher, idGenerator);
    }

    @Bean
    public ReplaceAccessUseCase replaceAccessUseCase(
            UserRepository users,
            RoleRepository roles,
            AuthorityRepository authorities,
            AccessAssignmentRepository assignments,
            SessionStore sessions,
            IdGenerator ids
    ) {
        return new ReplaceAccessService(users, roles, authorities, assignments, sessions, ids);
    }

    @Bean
    public SearchRolesUseCase searchRolesUseCase(RoleQueryPort roleQueryPort) {
        return new SearchRolesService(roleQueryPort);
    }

    @Bean
    public SwitchAccessContextUseCase switchAccessContextUseCase(
            UserRepository users,
            AccessAssignmentRepository assignments,
            PlatformIdentityResolver identityResolver,
            TokenLifeCycle tokenLifeCycle
    ) {
        return new SwitchAccessContextService(users, assignments, identityResolver, tokenLifeCycle);
    }

    @Bean
    public IdentityLookupUseCase identityLookupUseCase(IdentityLookupQueryPort identityLookupQueryPort) {
        return new IdentityLookupService(identityLookupQueryPort);
    }

    @Bean
    public RevokeSessionsByScopeUseCase revokeSessionsByScopeUseCase(SessionStore sessionStore) {
        return new RevokeSessionsByScopeService(sessionStore);
    }
}
