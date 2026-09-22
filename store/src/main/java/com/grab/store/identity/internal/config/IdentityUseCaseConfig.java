package com.grab.store.identity.internal.config;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.security.PlatformIdentityResolver;
import com.identity.application.port.inbound.*;
import com.identity.application.port.outbound.AccessAssignmentQueryPort;
import com.identity.application.port.outbound.IdentityLookupQueryPort;
import com.identity.application.port.outbound.RoleQueryPort;
import com.identity.application.port.outbound.UserQueryPort;
import com.identity.application.service.*;
import com.identity.domain.policy.AccessPlacementPolicyResolver;
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
            PlatformRepository platforms,
            RoleRepository roles,
            InvitationTokenService invitationTokens,
            IdGenerator ids
    ) {
        return new AcceptAccessInvitationService(invitations, assignments, platforms, roles, invitationTokens, ids);
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
            PlatformRepository platforms,
            RoleRepository roles,
            UserRepository users,
            AccessInvitationRepository invitations,
            RoleDelegationPolicy delegationPolicy,
            InvitationTokenService invitationTokens,
            IdGenerator ids
    ) {
        return new CreateAccessInvitationService(platforms, roles, users, invitations, delegationPolicy, invitationTokens, ids);
    }

    @Bean
    public CreateRoleUseCase createRoleUseCase(
            RoleRepository roleRepository,
            PlatformRepository platformRepository,
            RoleAdministrationPolicy roleAdministrationPolicy,
            IdGenerator idGenerator
    ) {
        return new CreateRoleService(roleRepository, platformRepository, roleAdministrationPolicy, idGenerator);
    }

    @Bean
    public GetUserProfileUseCase getUserProfileUseCase(UserQueryPort userQueryPort) {
        return new GetUserProfileService(userQueryPort);
    }

    @Bean
    public GrantAccessUseCase grantAccessUseCase(
            UserRepository users,
            PlatformRepository platforms,
            RoleRepository roles,
            AccessAssignmentRepository assignments,
            RoleDelegationPolicy delegationPolicy,
            IdGenerator ids
    ) {
        return new GrantAccessService(users, platforms, roles, assignments, delegationPolicy, ids);
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
            PlatformRepository platformRepository,
            RoleAdministrationPolicy roleAdministrationPolicy
    ) {
        return new ManageAuthorityService(roleRepository, platformRepository, roleAdministrationPolicy);
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(TokenLifeCycle tokenLifeCycle) {
        return new RefreshTokenService(tokenLifeCycle);
    }

    @Bean
    public RegisterUseCase registerUseCase(
            UserRepository users,
            PlatformRepository platforms,
            AccessAssignmentRepository accessAssignments,
            PasswordHasher passwordHasher,
            IdGenerator idGenerator
    ) {
        return new RegisterService(users, platforms, accessAssignments, passwordHasher, idGenerator);
    }

    @Bean
    public ReplaceAccessUseCase replaceAccessUseCase(
            UserRepository users,
            PlatformRepository platforms,
            AccessAssignmentRepository assignments,
            SessionStore sessions,
            IdGenerator ids,
            AccessPlacementPolicyResolver placementPolicies
    ) {
        return new ReplaceAccessService(users, platforms, assignments, sessions, ids, placementPolicies);
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
