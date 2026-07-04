package com.grab.store.merchant.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.merchant.internal.command.ChangeMerchantLifecycleCommand;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.event.MerchantApplicationSubmittedEvent;
import com.merchant.domain.repository.MerchantAccountRepository;
import com.merchant.domain.service.SystemDefaultMerchantApprovalPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MerchantAccountSubmitStatusEventListenerTest {

    private MerchantAccountSubmitStatusEventListener listener;
    private CommandBus commandBus;
    private IdGenerator idGenerator;
    private MerchantAccountRepository merchantAccountRepository;
    private SystemDefaultMerchantApprovalPolicy approvalPolicy;

    @BeforeEach
    void setUp() {
        commandBus = mock(CommandBus.class);
        idGenerator = mock(IdGenerator.class);
        merchantAccountRepository = mock(MerchantAccountRepository.class);
        approvalPolicy = mock(SystemDefaultMerchantApprovalPolicy.class);

        listener = new MerchantAccountSubmitStatusEventListener(
                commandBus,
                idGenerator,
                merchantAccountRepository,
                approvalPolicy
        );
    }

    @Test
    void shouldDispatchApproveCommandWhenPolicyAllowsAutoApprove() {
        // Arrange
        String merchantIdValue = "merchant-123";
        String applicantIdValue = "applicant-456";
        
        Id merchantId = mock(Id.class);
        Id applicantId = mock(Id.class);
        
        when(idGenerator.convertIdFrom(merchantIdValue)).thenReturn(merchantId);
        when(idGenerator.convertIdFrom(applicantIdValue)).thenReturn(applicantId);

        MerchantAccount merchant = mock(MerchantAccount.class);
        when(merchantAccountRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(approvalPolicy.canAutoApprove(merchant)).thenReturn(true);

        MerchantApplicationSubmittedEvent event = new MerchantApplicationSubmittedEvent(
                "event-1", merchantIdValue, applicantIdValue, "PENDING_REVIEW", applicantIdValue, 1L, Instant.now()
        );

        // Act
        listener.handleMerchantApplicationSubmitted(event);

        // Assert
        verify(commandBus, times(1)).dispatch(any(ChangeMerchantLifecycleCommand.class));
    }

    @Test
    void shouldNotDispatchApproveCommandWhenPolicyDeniesAutoApprove() {
        // Arrange
        String merchantIdValue = "merchant-123";
        String applicantIdValue = "applicant-456";

        Id merchantId = mock(Id.class);
        Id applicantId = mock(Id.class);

        when(idGenerator.convertIdFrom(merchantIdValue)).thenReturn(merchantId);
        when(idGenerator.convertIdFrom(applicantIdValue)).thenReturn(applicantId);

        MerchantAccount merchant = mock(MerchantAccount.class);
        when(merchantAccountRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(approvalPolicy.canAutoApprove(merchant)).thenReturn(false);

        MerchantApplicationSubmittedEvent event = new MerchantApplicationSubmittedEvent(
                "event-1", merchantIdValue, applicantIdValue, "PENDING_REVIEW", applicantIdValue, 1L, Instant.now()
        );

        // Act
        listener.handleMerchantApplicationSubmitted(event);

        // Assert
        verify(commandBus, never()).dispatch(any(ChangeMerchantLifecycleCommand.class));
    }

    @Test
    void shouldNotDispatchApproveCommandWhenMerchantNotFound() {
        // Arrange
        String merchantIdValue = "merchant-123";
        String applicantIdValue = "applicant-456";

        Id merchantId = mock(Id.class);
        Id applicantId = mock(Id.class);

        when(idGenerator.convertIdFrom(merchantIdValue)).thenReturn(merchantId);
        when(idGenerator.convertIdFrom(applicantIdValue)).thenReturn(applicantId);

        when(merchantAccountRepository.findById(merchantId)).thenReturn(Optional.empty());

        MerchantApplicationSubmittedEvent event = new MerchantApplicationSubmittedEvent(
                "event-1", merchantIdValue, applicantIdValue, "PENDING_REVIEW", applicantIdValue, 1L, Instant.now()
        );

        // Act
        listener.handleMerchantApplicationSubmitted(event);

        // Assert
        verify(commandBus, never()).dispatch(any(ChangeMerchantLifecycleCommand.class));
        verify(approvalPolicy, never()).canAutoApprove(any());
    }
}
