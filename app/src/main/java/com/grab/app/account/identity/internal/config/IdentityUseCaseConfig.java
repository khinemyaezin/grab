package com.grab.app.account.identity.internal.config;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.security.PlatformIdentityResolver;
import com.identity.application.port.inbound.AcceptAccessInvitationUseCase;
import com.identity.application.port.inbound.CancelAccessInvitationUseCase;
import com.identity.application.port.inbound.ChangeAccessStatusUseCase;
import com.identity.application.port.inbound.ChangeUserStatusUseCase;
import com.identity.application.port.inbound.CreateAccessInvitationUseCase;
import com.identity.application.port.inbound.CreateRoleUseCase;
import com.identity.application.port.inbound.EnsureSecurityCatalogStateUseCase;
import com.identity.application.port.inbound.GetRoleDeclarationStatusUseCase;
import com.identity.application.port.inbound.GetSecurityCatalogStatusUseCase;
import com.identity.application.port.inbound.GetUserProfileUseCase;
import com.identity.application.port.inbound.GrantAccessUseCase;
import com.identity.application.port.inbound.IdentityLookupUseCase;
import com.identity.application.port.inbound.ListAccessAssignmentsUseCase;
import com.identity.application.port.inbound.ListAccessContextsUseCase;
import com.identity.application.port.inbound.ListRolesUseCase;
import com.identity.application.port.inbound.ListUsersUseCase;
import com.identity.application.port.inbound.ListWaitingRoleDeclarationsUseCase;
import com.identity.application.port.inbound.ListWaitingSecurityManifestCandidatesUseCase;
import com.identity.application.port.inbound.LoginUseCase;
import com.identity.application.port.inbound.LogoutUseCase;
import com.identity.application.port.inbound.ManageAuthorityUseCase;
import com.identity.application.port.inbound.RefreshTokenUseCase;
import com.identity.application.port.inbound.RegisterRoleDeclarationUseCase;
import com.identity.application.port.inbound.RegisterSecurityManifestUseCase;
import com.identity.application.port.inbound.RegisterUseCase;
import com.identity.application.port.inbound.ReplaceAccessUseCase;
import com.identity.application.port.inbound.RevalidateSecurityManifestUseCase;
import com.identity.application.port.inbound.RevokeSessionsByScopeUseCase;
import com.identity.application.port.inbound.SearchRolesUseCase;
import com.identity.application.port.inbound.SwitchAccessContextUseCase;
import com.identity.application.port.inbound.UpdateScopeDisplayUseCase;
import com.identity.application.port.outbound.AccessAssignmentQueryPort;
import com.identity.application.port.outbound.IdentityLookupQueryPort;
import com.identity.application.port.outbound.RoleDeclarationQueryPort;
import com.identity.application.port.outbound.RoleQueryPort;
import com.identity.application.port.outbound.ScopeCatalogQueryPort;
import com.identity.application.port.outbound.ScopeDisplayViewRepository;
import com.identity.application.port.outbound.ScopeOwnershipPort;
import com.identity.application.port.outbound.SecurityManifestQueryPort;
import com.identity.application.port.outbound.UserQueryPort;
import com.identity.application.service.AcceptAccessInvitationService;
import com.identity.application.service.CancelAccessInvitationService;
import com.identity.application.service.ChangeAccessStatusService;
import com.identity.application.service.ChangeUserStatusService;
import com.identity.application.service.CreateAccessInvitationService;
import com.identity.application.service.CreateRoleService;
import com.identity.application.service.EnsureSecurityCatalogStateService;
import com.identity.application.service.GetRoleDeclarationStatusService;
import com.identity.application.service.GetSecurityCatalogStatusService;
import com.identity.application.service.GetUserProfileService;
import com.identity.application.service.GrantAccessService;
import com.identity.application.service.IdentityLookupService;
import com.identity.application.service.ListAccessAssignmentsService;
import com.identity.application.service.ListAccessContextsService;
import com.identity.application.service.ListRolesService;
import com.identity.application.service.ListUsersService;
import com.identity.application.service.ListWaitingRoleDeclarationsService;
import com.identity.application.service.ListWaitingSecurityManifestCandidatesService;
import com.identity.application.service.LoginService;
import com.identity.application.service.LogoutService;
import com.identity.application.service.ManageAuthorityService;
import com.identity.application.service.RefreshTokenService;
import com.identity.application.service.RegisterRoleDeclarationService;
import com.identity.application.service.RegisterSecurityManifestService;
import com.identity.application.service.RegisterService;
import com.identity.application.service.ReplaceAccessService;
import com.identity.application.service.RevalidateSecurityManifestService;
import com.identity.application.service.RevokeSessionsByScopeService;
import com.identity.application.service.SearchRolesService;
import com.identity.application.service.SwitchAccessContextService;
import com.identity.application.service.UpdateScopeDisplayService;
import com.identity.domain.policy.RoleDelegationPolicy;
import com.identity.domain.policy.impl.RoleAdministrationPolicy;
import com.identity.domain.policy.impl.RuleBasedRoleDelegationPolicy;
import com.identity.domain.port.outbound.AccessAssignmentRepository;
import com.identity.domain.port.outbound.AccessInvitationRepository;
import com.identity.domain.port.outbound.AuthorityRepository;
import com.identity.domain.port.outbound.RoleDeclarationRepository;
import com.identity.domain.port.outbound.RoleDelegationRuleRepository;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.port.outbound.SecurityCatalogRepository;
import com.identity.domain.port.outbound.SecurityManifestInboxRepository;
import com.identity.domain.port.outbound.SecurityManifestRevisionRepository;
import com.identity.domain.port.outbound.SessionStore;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.service.InvitationTokenService;
import com.identity.domain.service.PasswordHasher;
import com.identity.domain.service.TokenLifeCycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
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
            UserRepository users,
            SessionStore sessions,
            SecurityCatalogRepository catalogs
    ) {
        return new AcceptAccessInvitationService(
                catalogs, invitations, assignments, roles, invitationTokens, ids, users, sessions);
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
            UserRepository users,
            SecurityCatalogRepository catalogs
    ) {
        return new ChangeAccessStatusService(catalogs, assignments, sessions, delegationPolicy, users);
    }

    @Bean
    public ChangeUserStatusUseCase changeUserStatusUseCase(
            UserRepository userRepository,
            @Autowired(required = false) TokenLifeCycle tokenLifeCycle
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
    public ScopeOwnershipPort scopeOwnershipPort() {
        return (actorScopeKey, actorScopeId, targetScopeKey, targetScopeId) -> actorScopeKey.equals(targetScopeKey);
    }

    @Bean
    public GrantAccessUseCase grantAccessUseCase(
            UserRepository users,
            RoleRepository roles,
            AccessAssignmentRepository assignments,
            RoleDelegationPolicy delegationPolicy,
            IdGenerator ids,
            ScopeOwnershipPort scopeOwnershipPort,
            SecurityCatalogRepository catalogs,
            SessionStore sessions
    ) {
        return new GrantAccessService(
                users, roles, assignments, delegationPolicy, ids, scopeOwnershipPort, catalogs, sessions);
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
    public UpdateScopeDisplayUseCase updateScopeDisplayUseCase(ScopeDisplayViewRepository displays) {
        return new UpdateScopeDisplayService(displays);
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
            @Autowired(required = false) TokenLifeCycle tokenLifeCycle,
            @Autowired(required = false) PlatformIdentityResolver identityResolver
    ) {
        return new LoginService(userRepository, accessAssignments, passwordHasher, tokenLifeCycle, identityResolver);
    }

    @Bean
    public LogoutUseCase logoutUseCase(@Autowired(required = false) TokenLifeCycle tokenLifeCycle) {
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
    public RefreshTokenUseCase refreshTokenUseCase(@Autowired(required = false) TokenLifeCycle tokenLifeCycle) {
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
            @Autowired(required = false) PlatformIdentityResolver identityResolver,
            @Autowired(required = false) TokenLifeCycle tokenLifeCycle
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
            AuthorityRepository authorities,
            RoleDeclarationRepository declarations,
            IdGenerator ids
    ) {
        return new RegisterRoleDeclarationService(catalogs, roles, authorities, declarations, ids);
    }

    @Bean
    public ListWaitingRoleDeclarationsUseCase listWaitingRoleDeclarationsUseCase(
            RoleDeclarationQueryPort declarations
    ) {
        return new ListWaitingRoleDeclarationsService(declarations);
    }

    @Bean
    public GetRoleDeclarationStatusUseCase getRoleDeclarationStatusUseCase(
            RoleDeclarationQueryPort declarations
    ) {
        return new GetRoleDeclarationStatusService(declarations);
    }
}

