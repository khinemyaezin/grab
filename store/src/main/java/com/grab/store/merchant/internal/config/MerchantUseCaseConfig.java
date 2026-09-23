package com.grab.store.merchant.internal.config;

import com.grab.framework.id.IdGenerator;
import com.merchant.application.port.inbound.*;
import com.merchant.application.port.outbound.MerchantAccountQueryPort;
import com.merchant.application.port.outbound.MerchantMemberQueryPort;
import com.merchant.application.port.outbound.StorefrontQueryPort;
import com.merchant.application.service.*;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import com.merchant.domain.port.outbound.StorefrontRepository;
import com.merchant.domain.policy.MerchantApprovalPolicy;
import com.merchant.domain.policy.MerchantOwnershipPolicy;
import com.merchant.domain.service.MerchantRegistrationPolicy;
import com.merchant.domain.service.StorefrontProvisioningService;
import com.merchant.domain.service.StorefrontSlugPolicy;
import com.merchant.domain.policy.impl.SystemDefaultMerchantApprovalPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MerchantUseCaseConfig {

    @Bean
    MerchantRegistrationPolicy merchantRegistrationPolicy(MerchantAccountRepository merchants) {
        return new MerchantRegistrationPolicy(merchants);
    }

    @Bean
    MerchantApprovalPolicy merchantApprovalPolicy() {
        return new SystemDefaultMerchantApprovalPolicy();
    }

    @Bean
    StorefrontSlugPolicy storefrontSlugPolicy(StorefrontRepository storefronts) {
        return new StorefrontSlugPolicy(storefronts);
    }

    @Bean
    StorefrontProvisioningService storefrontProvisioningService() {
        return new StorefrontProvisioningService();
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
            IdGenerator ids
    ) {
        return new StartMerchantApplicationService(merchants, registrationPolicy, ids);
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
    MerchantOwnershipPolicy merchantOwnershipPolicy() {
        return new MerchantOwnershipPolicy();
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
            IdGenerator ids
    ) {
        return new ProvisionMerchantAdminService(members, ids);
    }
}
