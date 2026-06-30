package com.merchant.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.merchant.domain.enums.MerchantStatus;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.event.*;
import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.valueobject.*;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class MerchantAccount extends AggregateRoot<Id> {
    private final Id applicantUserId;
    private final MerchantType type;
    private MerchantName name;
    private BusinessRegistration registration;
    private ContactInformation contact;
    private RegisteredAddress registeredAddress;
    private MerchantStatus status;
    private LifecycleReason lifecycleReason;
    private Id reviewedBy;
    private Instant reviewedAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private final long version;

    public MerchantAccount(
            Id id,
            Id applicantUserId,
            MerchantType type,
            MerchantName name,
            BusinessRegistration registration,
            ContactInformation contact,
            RegisteredAddress registeredAddress,
            MerchantStatus status,
            LifecycleReason lifecycleReason,
            Id reviewedBy,
            Instant reviewedAt,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        super(id);
        this.applicantUserId = Objects.requireNonNull(applicantUserId, "applicantUserId is required");
        this.type = Objects.requireNonNull(type, "merchant type is required");
        this.name = Objects.requireNonNull(name, "merchant name is required");
        this.registration = registration;
        this.contact = contact;
        this.registeredAddress = registeredAddress;
        this.status = Objects.requireNonNull(status, "merchant status is required");
        this.lifecycleReason = lifecycleReason;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
        this.version = version;
    }

    public static MerchantAccount startDraft(
            Id id,
            Id applicantUserId,
            MerchantType type,
            String displayName,
            Instant now
    ) {
        MerchantAccount merchant = new MerchantAccount(
                id, applicantUserId, type, new MerchantName(null, displayName),
                null, null, null, MerchantStatus.DRAFT, null, null, null,
                now, now, 0
        );
        merchant.addEvent(merchant.event(MerchantApplicationStartedEvent::new, applicantUserId, now));
        return merchant;
    }

    public void updateProfile(
            MerchantName name,
            BusinessRegistration registration,
            ContactInformation contact,
            RegisteredAddress registeredAddress,
            Instant now
    ) {
        requireStatus(MerchantStatus.DRAFT, MerchantStatus.CHANGES_REQUESTED);
        this.name = Objects.requireNonNull(name, "merchant name is required");
        this.registration = registration;
        this.contact = Objects.requireNonNull(contact, "contact is required");
        this.registeredAddress = Objects.requireNonNull(registeredAddress, "registered address is required");
        this.lifecycleReason = null;
        this.updatedAt = now;
    }

    public void submit(Id actorId, Instant now) {
        requireStatus(MerchantStatus.DRAFT, MerchantStatus.CHANGES_REQUESTED);
        requireCompleteProfile();
        transition(MerchantStatus.PENDING_REVIEW, null, null, now);
        addEvent(event(MerchantApplicationSubmittedEvent::new, actorId, now));
    }

    public void requestChanges(Id reviewerId, LifecycleReason reason, Instant now) {
        requireStatus(MerchantStatus.PENDING_REVIEW);
        transition(MerchantStatus.CHANGES_REQUESTED, reason, reviewerId, now);
        addEvent(event(MerchantChangesRequestedEvent::new, reviewerId, now));
    }

    public void approve(Id reviewerId, Instant now) {
        requireStatus(MerchantStatus.PENDING_REVIEW);
        transition(MerchantStatus.ACTIVE, null, reviewerId, now);
        addEvent(event(MerchantApprovedEvent::new, reviewerId, now));
    }

    public void reject(Id reviewerId, LifecycleReason reason, Instant now) {
        requireStatus(MerchantStatus.PENDING_REVIEW);
        transition(MerchantStatus.REJECTED, reason, reviewerId, now);
        addEvent(event(MerchantRejectedEvent::new, reviewerId, now));
    }

    public void suspend(Id operatorId, LifecycleReason reason, Instant now) {
        requireStatus(MerchantStatus.ACTIVE);
        transition(MerchantStatus.SUSPENDED, reason, operatorId, now);
        addEvent(event(MerchantSuspendedEvent::new, operatorId, now));
    }

    public void reactivate(Id operatorId, Instant now) {
        requireStatus(MerchantStatus.SUSPENDED);
        transition(MerchantStatus.ACTIVE, null, operatorId, now);
        addEvent(event(MerchantReactivatedEvent::new, operatorId, now));
    }

    public void close(Id operatorId, LifecycleReason reason, Instant now) {
        requireStatus(MerchantStatus.ACTIVE, MerchantStatus.SUSPENDED);
        transition(MerchantStatus.CLOSED, reason, operatorId, now);
        addEvent(event(MerchantClosedEvent::new, operatorId, now));
    }

    public boolean isApplicant(Id userId) {
        return applicantUserId.equals(userId);
    }

    public void requireApplicant(Id userId) {
        if (!isApplicant(userId)) {
            throw new MerchantDomainException(
                    new MerchantDomainError.ApplicantAccessForbidden(getId().getValue()),
                    "Merchant application belongs to another user"
            );
        }
    }

    public boolean isOperational() {
        return status == MerchantStatus.ACTIVE;
    }

    private void requireCompleteProfile() {
        if (name.legalName() == null) throw incomplete("legalName");
        if (contact == null) throw incomplete("contact");
        if (registeredAddress == null) throw incomplete("registeredAddress");
        if (registration == null) {
            throw incomplete("businessRegistration");
        }
    }

    private void transition(
            MerchantStatus target,
            LifecycleReason reason,
            Id reviewer,
            Instant now
    ) {
        status = target;
        lifecycleReason = reason;
        reviewedBy = reviewer;
        reviewedAt = reviewer == null ? null : now;
        updatedAt = now;
    }

    private void requireStatus(MerchantStatus... allowed) {
        for (MerchantStatus candidate : allowed) {
            if (status == candidate) return;
        }
        String requested = allowed.length == 1 ? allowed[0].name() : "allowed lifecycle transition";
        throw new MerchantDomainException(
                new MerchantDomainError.InvalidStatusTransition(status.name(), requested),
                "Merchant cannot perform this transition from " + status
        );
    }

    private MerchantDomainException incomplete(String field) {
        return new MerchantDomainException(
                new MerchantDomainError.IncompleteProfile(field),
                "Merchant profile is incomplete: " + field
        );
    }

    private MerchantLifecycleEvent event(EventFactory factory, Id actorId, Instant now) {
        return factory.create(
                UUID.randomUUID().toString(),
                getId().getValue(),
                applicantUserId.getValue(),
                status.name(),
                actorId.getValue(),
                version + 1,
                now
        );
    }

    @FunctionalInterface
    private interface EventFactory {
        MerchantLifecycleEvent create(
                String eventId,
                String merchantId,
                String applicantUserId,
                String status,
                String actorId,
                long aggregateVersion,
                Instant occurredAt
        );
    }
}
