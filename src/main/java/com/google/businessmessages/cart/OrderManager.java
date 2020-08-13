package com.google.businessmessages.cart;

import java.util.ArrayList;
import java.util.List;
import com.google.appengine.api.datastore.Entity;

public class OrderManager {
    
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

    public static List<Order> getUnscheduledOrders(String conversationId) {
        
    }
}