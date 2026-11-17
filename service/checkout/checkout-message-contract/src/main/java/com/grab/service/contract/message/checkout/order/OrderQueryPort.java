package com.grab.service.contract.message.checkout.order;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.util.List;

/**
 * Universal Java Port Interface for Order Queries and Customer History.
 */
public interface OrderQueryPort extends Remote {

    OrderPlacementPort.OrderResult findOrderById(Id orderId) throws RemoteException;

    OrderPlacementPort.OrderResult findOrderByOrderNumber(String orderNumber) throws RemoteException;

    List<OrderPlacementPort.OrderResult> listCustomerOrders(Id customerId, int page, int size) throws RemoteException;

    List<OrderPlacementPort.OrderResult> searchOrders(OrderSearchCriteria criteria) throws RemoteException;

    record OrderSearchCriteria(
            Id merchantId,
            String status,
            int page,
            int size
    ) implements Serializable {}
}

