package com.grab.store.merchant.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.shared.events.identity.IdentitySecurityCatalogActivatedIntegrationEvent;
import com.merchant.application.model.write.PublishMerchantRoleDeclarationCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class MerchantRoleDeclarationStartupPublisherTest {
    private static final class RecordingCommandBus implements CommandBus {
        private Command<?> dispatched;

        @Override
        public <R> R dispatch(Command<R> command) {
            dispatched = command;
            return null;
        }
    }

    @Test
    void onStartupDispatchesDurablePublicationCommand() {
        RecordingCommandBus commands = new RecordingCommandBus();
        MerchantRoleDeclarationStartupPublisher publisher = new MerchantRoleDeclarationStartupPublisher(commands);

        publisher.onStartup();

        assertInstanceOf(PublishMerchantRoleDeclarationCommand.class, commands.dispatched);
    }

    @Test
    void onCatalogActivatedDispatchesPublicationRepairCommand() {
        RecordingCommandBus commands = new RecordingCommandBus();
        MerchantRoleDeclarationStartupPublisher publisher = new MerchantRoleDeclarationStartupPublisher(commands);
        IdentitySecurityCatalogActivatedIntegrationEvent event =
                new IdentitySecurityCatalogActivatedIntegrationEvent(1, "catalog", 1, "digest");

        publisher.onCatalogActivated(event);

        assertInstanceOf(PublishMerchantRoleDeclarationCommand.class, commands.dispatched);
    }
}
