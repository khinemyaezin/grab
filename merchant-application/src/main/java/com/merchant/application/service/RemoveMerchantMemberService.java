package com.merchant.application.service;

import com.grab.framework.id.Id;
import com.merchant.application.exception.MerchantServiceError;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.RemoveMerchantMemberCommand;
import com.merchant.application.port.inbound.RemoveMerchantMemberUseCase;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.aggregate.MerchantMember;
import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.policy.MerchantOwnershipPolicy;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class RemoveMerchantMemberService implements RemoveMerchantMemberUseCase {
    private final MerchantAccountRepository merchants;
    private final MerchantMemberRepository members;
    private final MerchantOwnershipPolicy ownershipPolicy;

    @Override
    public MerchantMemberResult execute(RemoveMerchantMemberCommand command) {
        MerchantAccount merchant = merchants.findById(command.merchantId())
                .orElseThrow(() -> notFound(command.merchantId()));

        MerchantMember member = members.findById(command.memberId())
                .orElseThrow(() -> memberNotFound(command.memberId()));

        if (!member.getMerchantId().equals(command.merchantId())) {
            throw new MerchantDomainException(
                    new MerchantDomainError.ApplicantAccessForbidden(command.merchantId().getValue()),
                    "Member does not belong to specified merchant"
            );
        }

        requireCanRemove(merchant, command.actorId(), member);

        if (member.isAdmin()) {
            long activeAdminCount = members.countActiveAdmins(command.merchantId());
            ownershipPolicy.requireNotSoleAdmin(member, activeAdminCount);
        }

        member.remove(Instant.now());
        MerchantMember saved = members.save(member);
        return MerchantMemberResult.from(saved);
    }

    private void requireCanRemove(MerchantAccount merchant, Id actorId, MerchantMember targetMember) {
        // Self-removal is allowed (leaving the merchant)
        if (targetMember.getUserId().equals(actorId)) {
            return;
        }

        if (merchant.isApplicant(actorId)) {
            return;
        }

        MerchantMember actorMember = members.findByMerchantIdAndUserId(merchant.getId(), actorId)
                .filter(m -> m.getStatus().isActive())
                .orElseThrow(() -> unauthorized(actorId, "REMOVE_MEMBER"));

        if (!actorMember.getRole().canManage(targetMember.getRole())) {
            throw unauthorized(actorId, "REMOVE_MEMBER_" + targetMember.getRole().name());
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
