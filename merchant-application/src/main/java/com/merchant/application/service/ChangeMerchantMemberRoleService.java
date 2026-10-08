package com.merchant.application.service;

import com.grab.framework.id.Id;
import com.merchant.application.exception.MerchantServiceError;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.application.model.write.ChangeMerchantMemberRoleCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.port.inbound.ChangeMerchantMemberRoleUseCase;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.aggregate.MerchantMember;
import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.policy.MerchantOwnershipPolicy;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import com.merchant.domain.valueobject.MerchantRole;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class ChangeMerchantMemberRoleService implements ChangeMerchantMemberRoleUseCase {
    private final MerchantAccountRepository merchants;
    private final MerchantMemberRepository members;
    private final MerchantOwnershipPolicy ownershipPolicy;

    @Override
    public MerchantMemberResult execute(ChangeMerchantMemberRoleCommand command) {
        MerchantAccount merchant = merchants.findById(command.merchantId())
                .orElseThrow(() -> notFound(command.merchantId()));

        requireAdmin(merchant, command.actorId());

        MerchantMember member = members.findById(command.memberId())
                .orElseThrow(() -> memberNotFound(command.memberId()));

        if (!member.getMerchantId().equals(command.merchantId())) {
            throw new MerchantDomainException(
                    new MerchantDomainError.ApplicantAccessForbidden(command.merchantId().getValue()),
                    "Member does not belong to specified merchant"
            );
        }

        if (member.isAdmin() && !command.newRole().isAdmin()) {
            long activeAdminCount = members.countActiveAdmins(command.merchantId());
            ownershipPolicy.requireNotSoleAdmin(member, activeAdminCount);
        }

        member.changeRole(command.newRole(), Instant.now());
        MerchantMember saved = members.save(member);
        return MerchantMemberResult.from(saved);
    }

    private void requireAdmin(MerchantAccount merchant, Id actorId) {
        if (merchant.isApplicant(actorId)) {
            return;
        }

        MerchantMember actorMember = members.findByMerchantIdAndUserId(merchant.getId(), actorId)
                .filter(m -> m.getStatus().isActive())
                .orElseThrow(() -> unauthorized(actorId, "CHANGE_ROLE"));

        if (!actorMember.isAdmin()) {
            throw unauthorized(actorId, "CHANGE_ROLE");
        }
    }

    private MerchantServiceException notFound(Id merchantId) {
        return new MerchantServiceException(
                new MerchantServiceError.MerchantNotFound(merchantId.getValue()),
                "Merchant account not found"
        );
    }

    private MerchantServiceException memberNotFound(Id memberId) {
        return new MerchantServiceException(
                new MerchantServiceError.MemberNotFound(memberId.getValue()),
                "Merchant member not found"
        );
    }

    private MerchantServiceException unauthorized(Id actorId, String operation) {
        return new MerchantServiceException(
                new MerchantServiceError.UnauthorizedMemberOperation(actorId.getValue(), operation),
                "Actor is not authorized for operation: " + operation
        );
    }
}
