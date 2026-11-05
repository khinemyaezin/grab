package com.grab.app.account.merchant.internal.config;

import com.grab.framework.id.IdGenerator;
import com.merchant.application.port.inbound.AcceptMerchantMemberInvitationUseCase;
import com.merchant.application.port.inbound.ChangeMerchantLifecycleUseCase;
import com.merchant.application.port.inbound.ChangeMerchantMemberRoleUseCase;
import com.merchant.application.port.inbound.ChangeStorefrontLifecycleUseCase;
import com.merchant.application.port.inbound.CreateStorefrontUseCase;
import com.merchant.application.port.inbound.GetC2CApplicationUseCase;
import com.merchant.application.port.inbound.GetFirstPartyRetailerApplicationUseCase;
import com.merchant.application.port.inbound.GetMerchantUseCase;
import com.merchant.application.port.inbound.GetStorefrontUseCase;
import com.merchant.application.port.inbound.InviteMerchantMemberUseCase;
import com.merchant.application.port.inbound.ListMerchantMembersUseCase;
import com.merchant.application.port.inbound.ListMerchantReviewQueueUseCase;
import com.merchant.application.port.inbound.ListMyMerchantsUseCase;
import com.merchant.application.port.inbound.ListStorefrontsByMerchantUseCase;
import com.merchant.application.port.inbound.ProvisionMerchantAdminUseCase;
import com.merchant.application.port.inbound.RemoveMerchantMemberUseCase;
import com.merchant.application.port.inbound.RevokeMerchantMemberAccessUseCase;
import com.merchant.application.port.inbound.StartMerchantApplicationUseCase;
import com.merchant.application.port.inbound.SubmitMerchantApplicationUseCase;
import com.merchant.application.port.inbound.SyncMerchantMemberRoleAccessUseCase;
import com.merchant.application.port.inbound.UpdateMerchantProfileUseCase;
import com.merchant.application.port.inbound.UpdateStorefrontProfileUseCase;
import com.merchant.application.port.outbound.IdentityAccessManagementPort;
import com.merchant.application.port.outbound.MerchantAccountQueryPort;
import com.merchant.application.port.outbound.MerchantMemberQueryPort;
import com.merchant.application.port.outbound.PrimaryMarketRegionPort;
import com.merchant.application.port.outbound.StorefrontQueryPort;
import com.merchant.application.service.AcceptMerchantMemberInvitationService;
import com.merchant.application.service.ChangeMerchantLifecycleService;
import com.merchant.application.service.ChangeMerchantMemberRoleService;
import com.merchant.application.service.ChangeStorefrontLifecycleService;
import com.merchant.application.service.CreateStorefrontService;
import com.merchant.application.service.GetC2CApplicationService;
import com.merchant.application.service.GetFirstPartyRetailerApplicationService;
import com.merchant.application.service.GetMerchantService;
import com.merchant.application.service.GetStorefrontService;
import com.merchant.application.service.InviteMerchantMemberService;
import com.merchant.application.service.ListMerchantMembersService;
import com.merchant.application.service.ListMerchantReviewQueueService;
import com.merchant.application.service.ListMyMerchantsService;
import com.merchant.application.service.ListStorefrontsByMerchantService;
import com.merchant.application.service.ProvisionMerchantAdminService;
import com.merchant.application.service.RemoveMerchantMemberService;
import com.merchant.application.service.RevokeMerchantMemberAccessService;
import com.merchant.application.service.StartMerchantApplicationService;
import com.merchant.application.service.SubmitMerchantApplicationService;
import com.merchant.application.service.SyncMerchantMemberRoleAccessService;
import com.merchant.application.service.UpdateMerchantProfileService;
import com.merchant.application.service.UpdateStorefrontProfileService;
import com.merchant.domain.policy.MerchantApprovalPolicy;
import com.merchant.domain.policy.MerchantOwnershipPolicy;
import com.merchant.domain.policy.impl.SystemDefaultMerchantApprovalPolicy;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import com.merchant.domain.port.outbound.StorefrontRepository;
import com.merchant.domain.service.MerchantRegistrationPolicy;
import com.merchant.domain.service.StorefrontProvisioningService;
import com.merchant.domain.service.StorefrontSlugPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MerchantUseCaseConfig {

    @Bean
    public MerchantRegistrationPolicy merchantRegistrationPolicy(MerchantAccountRepository merchants) {
        return new MerchantRegistrationPolicy(merchants);
    }

    @Bean
    public MerchantApprovalPolicy merchantApprovalPolicy() {
        return new SystemDefaultMerchantApprovalPolicy();
    }

    @Bean
    public StorefrontSlugPolicy storefrontSlugPolicy(StorefrontRepository storefronts) {
        return new StorefrontSlugPolicy(storefronts);
    }

    @Bean
    public StorefrontProvisioningService storefrontProvisioningService() {
        return new StorefrontProvisioningService();
    }

    @Bean
    public MerchantOwnershipPolicy merchantOwnershipPolicy() {
        return new MerchantOwnershipPolicy();
    }

    @Bean
    public ChangeMerchantLifecycleUseCase changeMerchantLifecycleUseCase(MerchantAccountRepository merchants) {
        return new ChangeMerchantLifecycleService(merchants);
    }

    @Bean
    public ChangeStorefrontLifecycleUseCase changeStorefrontLifecycleUseCase(
            MerchantAccountRepository merchants,
            StorefrontRepository storefronts,
            StorefrontProvisioningService provisioningService
    ) {
        return new ChangeStorefrontLifecycleService(merchants, storefronts, provisioningService);
    }

    @Bean
    public CreateStorefrontUseCase createStorefrontUseCase(
            MerchantAccountRepository merchants,
            StorefrontRepository storefronts,
            StorefrontProvisioningService provisioningService,
            StorefrontSlugPolicy slugPolicy,
            IdGenerator ids
    ) {
        return new CreateStorefrontService(merchants, storefronts, provisioningService, slugPolicy, ids);
    }

    @Bean
    public GetC2CApplicationUseCase getC2CApplicationUseCase(MerchantAccountQueryPort merchantAccountQueryPort) {
        return new GetC2CApplicationService(merchantAccountQueryPort);
    }

    @Bean
    public GetFirstPartyRetailerApplicationUseCase getFirstPartyRetailerApplicationUseCase(
            MerchantAccountQueryPort merchantAccountQueryPort
    ) {
        return new GetFirstPartyRetailerApplicationService(merchantAccountQueryPort);
    }

    @Bean
    public GetMerchantUseCase getMerchantUseCase(MerchantAccountQueryPort merchants) {
        return new GetMerchantService(merchants);
    }

    @Bean
    public GetStorefrontUseCase getStorefrontUseCase(StorefrontQueryPort storefrontQueryPort) {
        return new GetStorefrontService(storefrontQueryPort);
    }

    @Bean
    public ListMerchantReviewQueueUseCase listMerchantReviewQueueUseCase(MerchantAccountQueryPort merchants) {
        return new ListMerchantReviewQueueService(merchants);
    }

    @Bean
    public ListMyMerchantsUseCase listMyMerchantsUseCase(MerchantAccountQueryPort merchants) {
        return new ListMyMerchantsService(merchants);
    }

    @Bean
    public ListStorefrontsByMerchantUseCase listStorefrontsByMerchantUseCase(StorefrontQueryPort storefrontQueryPort) {
        return new ListStorefrontsByMerchantService(storefrontQueryPort);
    }

    @Bean
    public StartMerchantApplicationUseCase startMerchantApplicationUseCase(
            MerchantAccountRepository merchants,
            MerchantRegistrationPolicy registrationPolicy,
            IdGenerator ids,
            PrimaryMarketRegionPort primaryMarketRegionPort
    ) {
        return new StartMerchantApplicationService(merchants, registrationPolicy, ids, primaryMarketRegionPort);
    }

    @Bean
    public SubmitMerchantApplicationUseCase submitMerchantApplicationUseCase(MerchantAccountRepository merchants) {
        return new SubmitMerchantApplicationService(merchants);
    }

    @Bean
    public UpdateMerchantProfileUseCase updateMerchantProfileUseCase(MerchantAccountRepository merchants) {
        return new UpdateMerchantProfileService(merchants);
    }

    @Bean
    public UpdateStorefrontProfileUseCase updateStorefrontProfileUseCase(
            MerchantAccountRepository merchants,
            StorefrontRepository storefronts,
            StorefrontSlugPolicy slugPolicy
    ) {
        return new UpdateStorefrontProfileService(merchants, storefronts, slugPolicy);
    }

    @Bean
    public InviteMerchantMemberUseCase inviteMerchantMemberUseCase(
            MerchantAccountRepository merchants,
            MerchantMemberRepository members,
            IdGenerator ids
    ) {
        return new InviteMerchantMemberService(merchants, members, ids);
    }

    @Bean
    public AcceptMerchantMemberInvitationUseCase acceptMerchantMemberInvitationUseCase(
            MerchantMemberRepository members
    ) {
        return new AcceptMerchantMemberInvitationService(members);
    }

    @Bean
    public ChangeMerchantMemberRoleUseCase changeMerchantMemberRoleUseCase(
            MerchantAccountRepository merchants,
            MerchantMemberRepository members,
            MerchantOwnershipPolicy ownershipPolicy
    ) {
        return new ChangeMerchantMemberRoleService(merchants, members, ownershipPolicy);
    }

    @Bean
    public RemoveMerchantMemberUseCase removeMerchantMemberUseCase(
            MerchantAccountRepository merchants,
            MerchantMemberRepository members,
            MerchantOwnershipPolicy ownershipPolicy
    ) {
        return new RemoveMerchantMemberService(merchants, members, ownershipPolicy);
    }

    @Bean
    public ListMerchantMembersUseCase listMerchantMembersUseCase(MerchantMemberQueryPort memberQueryPort) {
        return new ListMerchantMembersService(memberQueryPort);
    }

    @Bean
    public ProvisionMerchantAdminUseCase provisionMerchantAdminUseCase(
            MerchantMemberRepository members,
            IdGenerator ids,
            IdentityAccessManagementPort identityAccessManagementPort
    ) {
        return new ProvisionMerchantAdminService(members, ids, identityAccessManagementPort);
    }

    @Bean
    public SyncMerchantMemberRoleAccessUseCase syncMerchantMemberRoleAccessUseCase(
            IdentityAccessManagementPort identityAccessManagementPort
    ) {
        return new SyncMerchantMemberRoleAccessService(identityAccessManagementPort);
    }

    @Bean
    public RevokeMerchantMemberAccessUseCase revokeMerchantMemberAccessUseCase(
            IdentityAccessManagementPort identityAccessManagementPort
    ) {
        return new RevokeMerchantMemberAccessService(identityAccessManagementPort);
    }
}

