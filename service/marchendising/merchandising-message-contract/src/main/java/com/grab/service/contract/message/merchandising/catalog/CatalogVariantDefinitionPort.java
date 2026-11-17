package com.grab.service.contract.message.merchandising.catalog;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.util.List;

/**
 * Universal Java Port Interface for Variant Types and Options definitions.
 * Replaces legacy VariantTypeController and VariantOptionController.
 */
public interface CatalogVariantDefinitionPort extends Remote {

    List<VariantTypeMessageResult> listVariantTypes(String name) throws RemoteException;

    List<VariantOptionMessageResult> listVariantOptions(String name, Id variantTypeId) throws RemoteException;

    record VariantTypeMessageResult(
            String id,
            String name
    ) implements Serializable {}

    record VariantOptionMessageResult(
            String id,
            String name,
            String typeId,
            String typeName
    ) implements Serializable {}
}
