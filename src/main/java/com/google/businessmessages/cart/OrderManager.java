package com.google.businessmessages.cart;

import java.util.stream.Collectors;
import com.google.common.collect.ImmutableList;

/**
 * Manages all database operations to create and retrieve order instances.
 */
public class OrderManager {
    
    /**
     * Gets all orders associated with the user from the database.
     * @param conversationId The unique id mapping between the agent and the user.
     * @return All orders associated with the user.
     */
    public static ImmutableList<Order> getAllOrders(String conversationId) {
        return ImmutableList.copyOf(DataManager.getInstance().getOrdersFromData(conversationId)
            .stream()
            .map(ent->
                new Order((String) ent.getProperty(DataManager.PROPERTY_ORDER_ID))
            ).collect(Collectors.toList()));
    }

    /**
     * Gets all orders associated with the user that have not been scheduled for 
     * pickup yet.
     * @param conversationId The unique id mapping between the agent and the user.
     * @return All user orders not scheduled for pickup yet.
     */
    public static ImmutableList<Order> getUnscheduledOrders(String conversationId) {
        return ImmutableList.copyOf(getAllOrders(conversationId)
            .stream()
            .filter(order->
                DataManager.getInstance().getExistingPickup(conversationId, order.getId()) == null)
            .collect(Collectors.toList()));
    }

    /**
     * Adds the specified order to the database.
     * @param conversationId The unique id mapping between the agent and the user.
     * @param orderId The id of the order being added to the database.
     */
    public static void addOrder(String conversationId, String orderId) {
        DataManager dataManager = DataManager.getInstance();
        dataManager.addOrder(conversationId, orderId);
    }
}