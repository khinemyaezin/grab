package com.grab.service.contract.message.merchandising.catalog;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.util.List;

/**
 * Universal Java Port Interface for Catalog Categories.
 * Replaces the legacy Category REST Controller.
 */
public interface CatalogCategoryPort extends Remote {

    CategoryMessageResult createCategory(CreateCategoryMessage message) throws RemoteException;

    void deleteCategory(Id categoryId) throws RemoteException;

    CategoryMessageResult findCategoryById(Id categoryId) throws RemoteException;

    CategoryTreeNodeResult getCategoryTree(Id rootCategoryId) throws RemoteException;

    List<CategoryMessageResult> getChildCategories(Id categoryId) throws RemoteException;

    List<CategoryMessageResult> getLeafCategories(String name) throws RemoteException;

    record CreateCategoryMessage(
            String name,
            Id parentId,
            Boolean active,
            Boolean listingAllowed,
            Boolean c2cAllowed
    ) implements Serializable {}

    record CategoryMessageResult(
            String id,
            String name,
            String parentId,
            boolean active,
            boolean listingAllowed,
            boolean c2cAllowed
    ) implements Serializable {}

    record CategoryTreeNodeResult(
            String id,
            String name,
            String parentId,
            List<CategoryTreeNodeResult> children
    ) implements Serializable {}
}
