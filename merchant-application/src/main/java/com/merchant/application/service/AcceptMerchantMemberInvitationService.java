package com.merchant.application.service;

import com.grab.framework.id.Id;
import com.merchant.application.exception.MerchantServiceError;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.application.model.write.AcceptMerchantMemberInvitationCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.port.inbound.AcceptMerchantMemberInvitationUseCase;
import com.merchant.domain.aggregate.MerchantMember;
import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class AcceptMerchantMemberInvitationService implements AcceptMerchantMemberInvitationUseCase {
    private final MerchantMemberRepository members;

    @Override
    public MerchantMemberResult execute(AcceptMerchantMemberInvitationCommand command) {
        MerchantMember member = members.findById(command.memberId())
                .orElseThrow(() -> memberNotFound(command.memberId()));

        if (!member.getMerchantId().equals(command.merchantId())) {
            throw new MerchantDomainException(
                    new MerchantDomainError.ApplicantAccessForbidden(command.merchantId().getValue()),
                    "Member does not belong to specified merchant"
            );
        }

        member.accept(command.actorUserId(), Instant.now());
        MerchantMember saved = members.save(member);
        return MerchantMemberResult.from(saved);
    }

    private MerchantServiceException memberNotFound(Id memberId) {
        return new MerchantServiceException(
                new MerchantServiceError.MemberNotFound(memberId.getValue()),
                "Merchant member not found"
        );
    }
}
