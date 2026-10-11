package com.grab.store.merchant.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.merchant.application.model.write.RevokeMerchantMemberAccessCommand;
import com.merchant.application.model.write.SyncMerchantMemberRoleAccessCommand;
import com.merchant.domain.event.MerchantMemberRemovedEvent;
import com.merchant.domain.event.MerchantMemberRoleChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class MerchantMemberAccessSyncListener {
    private static final Logger log = Loggers.getLogger(MerchantMemberAccessSyncListener.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    @EventListener
    public void onMemberRoleChanged(MerchantMemberRoleChangedEvent event) {
        String memberIdString = event.memberId();
        Id memberId = idGenerator.convertIdFrom(memberIdString);
        String userIdString = event.userId();
        Id userId = idGenerator.convertIdFrom(userIdString);
        String merchantIdString = event.merchantId();
        Id merchantId = idGenerator.convertIdFrom(merchantIdString);
        String previousRole = event.previousRole();
        String newRole = event.newRole();
        Set<String> authorities = event.authorities();
        log.info(
                "Dispatching role access sync for memberId={} userId={} in merchantId={} from {} to {}",
                memberIdString,
                userIdString,
                merchantIdString,
                previousRole,
                newRole
        );
        SyncMerchantMemberRoleAccessCommand command = new SyncMerchantMemberRoleAccessCommand(
                merchantId,
                memberId,
                userId,
                previousRole,
                newRole,
                authorities
        );
        commandBus.dispatch(command);
    }

    @EventListener
    public void onMemberRemoved(MerchantMemberRemovedEvent event) {
        String memberIdString = event.memberId();
        Id memberId = idGenerator.convertIdFrom(memberIdString);
        String userIdString = event.userId();
        Id userId = idGenerator.convertIdFrom(userIdString);
        String merchantIdString = event.merchantId();
        Id merchantId = idGenerator.convertIdFrom(merchantIdString);
        String role = event.role();
        log.info(
                "Dispatching access revocation for memberId={} userId={} in merchantId={}",
                memberIdString,
                userIdString,
                merchantIdString
        );
        RevokeMerchantMemberAccessCommand command = new RevokeMerchantMemberAccessCommand(
                merchantId,
                memberId,
                userId,
                role
        );
        commandBus.dispatch(command);
    }
}
