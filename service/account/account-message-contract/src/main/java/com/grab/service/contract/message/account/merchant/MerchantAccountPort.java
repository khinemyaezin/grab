package com.grab.service.contract.message.account.merchant;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;

/**
 * Universal Java Port Interface for Merchant Account lifecycle and Onboarding applications.
 * Replaces MerchantController, MerchantAdminController, and Retailer/C2C Application controllers.
 */
public interface MerchantAccountPort extends Remote {

    MerchantMessageResult startApplication(StartMerchantApplicationMessage message) throws RemoteException;

    MerchantMessageResult submitApplication(Id merchantId, Id applicantUserId) throws RemoteException;

    MerchantMessageResult approveMerchant(Id merchantId, Id actorId) throws RemoteException;

    MerchantMessageResult rejectMerchant(Id merchantId, Id actorId, String reason) throws RemoteException;

    MerchantMessageResult requestChanges(Id merchantId, Id actorId, String comments) throws RemoteException;

    MerchantMessageResult suspendMerchant(Id merchantId, Id actorId, String reason) throws RemoteException;

    MerchantMessageResult reactivateMerchant(Id merchantId, Id actorId) throws RemoteException;

    MerchantMessageResult closeMerchant(Id merchantId, Id actorId) throws RemoteException;

    MerchantMessageResult updateProfile(UpdateMerchantProfileMessage message) throws RemoteException;

    MerchantMessageResult findMerchantById(Id merchantId, Id actorId, boolean reviewerAccess, boolean scopedAccess) throws RemoteException;

    List<MerchantMessageResult> listMyMerchants(Id applicantUserId) throws RemoteException;

    List<MerchantMessageResult> listReviewQueue(String status) throws RemoteException;

    C2CApplicationResult getC2CApplication(Id applicantUserId) throws RemoteException;

    FirstPartyApplicationResult getFirstPartyApplication(Id applicantUserId) throws RemoteException;

    record StartMerchantApplicationMessage(
            Id applicantUserId,
            String businessType, // FIRST_PARTY, THIRD_PARTY, C2C
            String displayName,
            String primaryMarketCountryCode
    ) implements Serializable {}

    record UpdateMerchantProfileMessage(
            Id merchantId,
            Id applicantUserId,
            String legalName,
            String displayName,
            String registrationCountryCode,
            String registrationNumber,
            String contactEmail,
            String contactPhone,
            String addressLine1,
            String addressLine2,
            String addressCity,
            String addressRegion,
            String addressPostalCode,
            String addressCountryCode
    ) implements Serializable {}

    record MerchantMessageResult(
            String merchantId,
            String businessType,
            String status,
            String legalName,
            String displayName,
            Instant createdAt,
            Instant updatedAt
    ) implements Serializable {}

    record C2CApplicationResult(
            String merchantId,
            String applicantUserId,
            String type,
            String status,
            boolean completedContactInfo,
            boolean completedBasicInfo
    ) implements Serializable {}

    record FirstPartyApplicationResult(
            String merchantId,
            String applicantUserId,
            String type,
            String status,
            boolean completedContactInfo,
            boolean completedBasicInfo,
            boolean completedBusinessRegistration
    ) implements Serializable {}
}
