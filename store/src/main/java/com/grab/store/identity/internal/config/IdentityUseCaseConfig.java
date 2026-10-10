package com.grab.store.identity.internal.config;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.security.PlatformIdentityResolver;
import com.identity.application.port.inbound.*;
import com.identity.application.port.outbound.AccessAssignmentQueryPort;
import com.identity.application.port.outbound.SecurityManifestQueryPort;
import com.identity.application.port.outbound.ScopeCatalogQueryPort;
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
    public EnsureSecurityCatalogStateUseCase ensureSecurityCatalogStateUseCase(
            SecurityCatalogRepository catalogs
    ) {
        return new EnsureSecurityCatalogStateService(catalogs);
    }

    @Bean
    public AcceptAccessInvitationUseCase acceptAccessInvitationUseCase(
            AccessInvitationRepository invitations,
            AccessAssignmentRepository assignments,
            RoleRepository roles,
            InvitationTokenService invitationTokens,
            IdGenerator ids,
            SecurityCatalogRepository catalogs
    ) {
        return new AcceptAccessInvitationService(catalogs, invitations, assignments, roles, invitationTokens, ids);
    }

    @Bean
    public CancelAccessInvitationUseCase cancelAccessInvitationUseCase(
            AccessInvitationRepository invitations,
            RoleDelegationPolicy delegationPolicy,
            SecurityCatalogRepository catalogs
    ) {
        return new CancelAccessInvitationService(catalogs, invitations, delegationPolicy);
    }

    @Bean
    public ChangeAccessStatusUseCase changeAccessStatusUseCase(
            AccessAssignmentRepository assignments,
            SessionStore sessions,
            RoleDelegationPolicy delegationPolicy,
            SecurityCatalogRepository catalogs
    ) {
        return new ChangeAccessStatusService(catalogs, assignments, sessions, delegationPolicy);
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
            IdGenerator ids,
            SecurityCatalogRepository catalogs
    ) {
        return new CreateAccessInvitationService(catalogs, roles, users, invitations, delegationPolicy, invitationTokens, ids);
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
            ScopeOwnershipPort scopeOwnershipPort,
            SecurityCatalogRepository catalogs
    ) {
        return new GrantAccessService(users, roles, assignments, delegationPolicy, ids, scopeOwnershipPort, catalogs);
    }

    @Bean
    public ListAccessAssignmentsUseCase listAccessAssignmentsUseCase(AccessAssignmentQueryPort assignments, ScopeCatalogQueryPort scopes) {
        return new ListAccessAssignmentsService(scopes, assignments);
    }

    @Bean
    public ListAccessContextsUseCase listAccessContextsUseCase(
            AccessAssignmentQueryPort assignments,
            ScopeCatalogQueryPort scopes
    ) {
        return new ListAccessContextsService(scopes, assignments);
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
    public RegisterSecurityManifestUseCase registerSecurityManifestUseCase(
            SecurityCatalogRepository catalogs, SecurityManifestRevisionRepository revisions,
            SecurityManifestInboxRepository inbox) {
        return new RegisterSecurityManifestService(catalogs, revisions, inbox);
    }

    @Bean
    public GetSecurityCatalogStatusUseCase getSecurityCatalogStatusUseCase(SecurityManifestQueryPort manifests) {
        return new GetSecurityCatalogStatusService(manifests);
    }

    @Bean
    public ListWaitingSecurityManifestCandidatesUseCase listWaitingSecurityManifestCandidatesUseCase(
            SecurityManifestQueryPort manifests) {
        return new ListWaitingSecurityManifestCandidatesService(manifests);
    }

    @Bean
    public RevalidateSecurityManifestUseCase revalidateSecurityManifestUseCase(
            SecurityManifestRevisionRepository revisions, RegisterSecurityManifestUseCase registration,
            SecurityCatalogRepository catalogs) {
        return new RevalidateSecurityManifestService(catalogs, revisions, registration);
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
            IdGenerator ids,
            SecurityCatalogRepository catalogs
    ) {
        return new ReplaceAccessService(users, roles, authorities, assignments, sessions, ids, catalogs);
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

    @Bean
    public RegisterRoleDeclarationUseCase registerRoleDeclarationUseCase(
            SecurityCatalogRepository catalogs,
            RoleRepository roles,
            AuthorityRepository authorities
    ) {
        return new RegisterRoleDeclarationService(catalogs, roles, authorities);
    }

    @Bean
    public FulfillAdminAccessAssignmentUseCase fulfillAdminAccessAssignmentUseCase(
            UserRepository users,
            RoleRepository roles,
            AccessAssignmentRepository assignments,
            IdGenerator ids
    ) {
        return new FulfillAdminAccessAssignmentService(users, roles, assignments, ids);
    }
}
