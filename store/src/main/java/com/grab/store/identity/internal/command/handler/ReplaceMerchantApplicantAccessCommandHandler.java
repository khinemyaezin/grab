package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.command.AccessAssignmentResult;
import com.grab.store.identity.internal.command.ReplaceMerchantApplicantAccessCommand;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.repository.PlatformRepository;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.service.MerchantAccessProfile;
import com.identity.domain.service.MerchantAccountAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReplaceMerchantApplicantAccessCommandHandler
        implements CommandHandler<ReplaceMerchantApplicantAccessCommand, AccessAssignmentResult> {
    private final UserRepository users;
    private final PlatformRepository platforms;
    private final MerchantAccountAccessPolicy merchantAccessPolicy;

    @Override
    @IdentityTransactional
    public AccessAssignmentResult handle(ReplaceMerchantApplicantAccessCommand command) {
        users.findById(command.applicantUserId()).orElseThrow(() -> new IdentityServiceException(
                new IdentityServiceError.UserNotFound(command.applicantUserId().getValue()),
                "Merchant applicant user not found"
        ));

        Platform sellerPlatform = platforms.findByCode(MerchantAccessProfile.SELLER_PLATFORM_CODE)
                .orElseThrow(() -> new IdentityServiceException(
                        new IdentityServiceError.PlatformNotFound(MerchantAccessProfile.SELLER_PLATFORM_CODE),
                        "Seller platform not found"
                ));

        AccessAssignment owner = merchantAccessPolicy.replaceApplicantWithOwner(
                command.applicantUserId(),
                command.merchantId(),
                sellerPlatform
        );
        return AccessAssignmentResult.from(owner);
    }

    @Override
    public Class<ReplaceMerchantApplicantAccessCommand> getCommandType() {
        return ReplaceMerchantApplicantAccessCommand.class;
    }
}
