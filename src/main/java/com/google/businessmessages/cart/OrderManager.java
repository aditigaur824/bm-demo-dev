package com.google.businessmessages.cart;

import java.util.ArrayList;
import java.util.List;
import com.google.appengine.api.datastore.Entity;

/**
 * Manages all database operations to create and retrieve order instances.
 */
public class OrderManager {
    
    /**
     * Gets all orders associated with the user from the database.
     * @param conversationId The unique id mapping between the agent and the user.
     * @return All orders associated with the user.
     */
    public static List<Order> getAllOrders(String conversationId) {
        List<Order> allOrders = new ArrayList<>();
        DataManager dataManager = DataManager.getInstance();
        List<Entity> orders = dataManager.getOrdersFromData(conversationId);
        for (Entity ent : orders) {
            String orderId = (String) ent.getProperty(DataManager.PROPERTY_ORDER_ID);
            allOrders.add(new Order(orderId));
        }
        return allOrders;
    }

    /**
     * Gets all orders associated with the user that have not been scheduled for 
     * pickup yet.
     * @param conversationId The unique id mapping between the agent and the user.
     * @return All user orders not scheduled for pickup yet.
     */
    public static List<Order> getUnscheduledOrders(String conversationId) {
        List<Order> unscheduledOrders = new ArrayList<>();
        List<Order> allOrders = getAllOrders(conversationId);
        for (Order order : allOrders) {
            if (DataManager.getInstance().getExistingPickup(conversationId, order.getId()) == null) {
                unscheduledOrders.add(order);
            }
        }
        return unscheduledOrders;
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