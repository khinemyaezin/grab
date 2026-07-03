package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record ReplaceMerchantApplicantAccessCommand(
        Id applicantUserId,
        Id merchantId
) implements Command<AccessAssignmentResult> {
}
