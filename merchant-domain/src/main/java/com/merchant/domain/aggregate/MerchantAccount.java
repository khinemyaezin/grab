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
import java.util.Locale;
import java.util.Objects;

@Getter
public class MerchantAccount extends AggregateRoot<Id> {
    private final Id applicantUserId;
    private final MerchantType type;
    private final String primaryMarketCountryCode;
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
            String primaryMarketCountryCode,
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
        this.primaryMarketCountryCode = normalizeCountryCode(primaryMarketCountryCode);
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
            String primaryMarketCountryCode,
            String displayName,
            Instant now
    ) {
        MerchantAccount merchant = new MerchantAccount(
                id, applicantUserId,
                type,
                primaryMarketCountryCode,
                new MerchantName(null, displayName),
                null,
                null,
                null,
                MerchantStatus.DRAFT,
                null,
                null,
                null,
                now,
                now,
                0
        );
        merchant.addEvent(new MerchantApplicationStartedEvent(
                id.getValue(),
                displayName,
                applicantUserId.getValue(),
                MerchantStatus.DRAFT.name(),
                applicantUserId.getValue(),
                1,
                now
        ));
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
        this.registeredAddress = registeredAddress;
        this.lifecycleReason = null;
        this.updatedAt = now;
        addEvent(new MerchantProfileUpdatedEvent(
            getId().getValue(), 
            this.name.displayName(), 
            version + 1, 
            now));
    }

    public void submit(Id actorId, Instant now) {
        requireStatus(MerchantStatus.DRAFT, MerchantStatus.CHANGES_REQUESTED);
        requireCompleteProfile();
        transition(MerchantStatus.PENDING_REVIEW, null, null, now);
        addEvent(new MerchantApplicationSubmittedEvent(
                getId().getValue(),
                this.name.displayName(),
                applicantUserId.getValue(),
                status.name(),
                actorId.getValue(),
                version + 1,
                now
        ));
    }

    public void requestChanges(Id reviewerId, LifecycleReason reason, Instant now) {
        requireStatus(MerchantStatus.PENDING_REVIEW);
        transition(MerchantStatus.CHANGES_REQUESTED, reason, reviewerId, now);
        addEvent(new MerchantChangesRequestedEvent(
                getId().getValue(),
                this.name.displayName(),
                applicantUserId.getValue(),
                status.name(),
                reviewerId.getValue(),
                version + 1,
                now
        ));
    }

    public void approve(Id reviewerId, Instant now) {
        requireStatus(MerchantStatus.PENDING_REVIEW);
        transition(MerchantStatus.ACTIVE, null, reviewerId, now);
        addEvent(new MerchantApprovedEvent(
                getId().getValue(),
                this.name.displayName(),
                applicantUserId.getValue(),
                status.name(),
                reviewerId.getValue(),
                version + 1,
                now,
                primaryMarketCountryCode
        ));
    }

    public void reject(Id reviewerId, LifecycleReason reason, Instant now) {
        requireStatus(MerchantStatus.PENDING_REVIEW);
        transition(MerchantStatus.REJECTED, reason, reviewerId, now);
        addEvent(new MerchantRejectedEvent(
                getId().getValue(),
                this.name.displayName(),
                applicantUserId.getValue(),
                status.name(),
                reviewerId.getValue(),
                version + 1,
                now
        ));
    }

    public void suspend(Id operatorId, LifecycleReason reason, Instant now) {
        requireStatus(MerchantStatus.ACTIVE);
        transition(MerchantStatus.SUSPENDED, reason, operatorId, now);
        addEvent(new MerchantSuspendedEvent(
                getId().getValue(),
                this.name.displayName(),
                applicantUserId.getValue(),
                status.name(),
                operatorId.getValue(),
                version + 1,
                now
        ));
    }

    public void reactivate(Id operatorId, Instant now) {
        requireStatus(MerchantStatus.SUSPENDED);
        transition(MerchantStatus.ACTIVE, null, operatorId, now);
        addEvent(new MerchantReactivatedEvent(
                getId().getValue(),
                this.name.displayName(),
                applicantUserId.getValue(),
                status.name(),
                operatorId.getValue(),
                version + 1,
                now
        ));
    }

    public void close(Id operatorId, LifecycleReason reason, Instant now) {
        requireStatus(MerchantStatus.ACTIVE, MerchantStatus.SUSPENDED);
        transition(MerchantStatus.CLOSED, reason, operatorId, now);
        addEvent(new MerchantClosedEvent(
                getId().getValue(),
                this.name.displayName(),
                applicantUserId.getValue(),
                status.name(),
                operatorId.getValue(),
                version + 1,
                now
        ));
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

        if (type.requiresBusinessRegistration() && registration == null) {
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

    private static String normalizeCountryCode(String countryCode) {
        if (countryCode == null || !countryCode.trim().matches("[A-Za-z]{2}")) {
            throw new MerchantDomainException(
                    new MerchantDomainError.InvalidField("primaryMarketCountryCode"),
                    "primaryMarketCountryCode is invalid"
            );
        }
        return countryCode.trim().toUpperCase(Locale.ROOT);
    }
}
