package com.grab.service.contract.message.account.identity;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.util.List;

/**
 * Universal Java Port Interface for User Administration and Profile Lookups.
 * Unifies UserAdminController and UserProfileQuery.
 */
public interface IdentityAdminPort extends Remote {

    UserDetailResult findUserById(Id userId) throws RemoteException;

    List<UserSummaryResult> listUsers(int page, int size) throws RemoteException;

    void changeUserStatus(Id userId, String status) throws RemoteException;

    record UserDetailResult(
            String id,
            String email,
            String status,
            String createdAt,
            List<AccessContextInfo> accessContexts
    ) implements Serializable {
        public record AccessContextInfo(
                String assignmentId,
                String roleCode,
                String scopeKey,
                String scopeId,
                String status
        ) {}
    }

    record UserSummaryResult(
            String id,
            String email,
            String status,
            String createdAt
    ) implements Serializable {}
}
