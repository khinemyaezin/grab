package com.merchant.application.port.inbound;

import com.merchant.application.model.write.SyncMerchantMemberRoleAccessCommand;

public interface SyncMerchantMemberRoleAccessUseCase {
    void execute(SyncMerchantMemberRoleAccessCommand command);
}
