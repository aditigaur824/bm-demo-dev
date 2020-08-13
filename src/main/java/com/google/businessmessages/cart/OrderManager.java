package com.google.businessmessages.cart;

public class OrderManager {
    
    public static List<Order> getAllOrdrs(String conversationId) {
        List<Order> allOrders = new ArrayList<>();
        DataManager dataManager = DataManager.getInstance();
        List<Entity> orders = dataManager.getOrdersFromData(conversationId);
        for (Entity ent : orders) {
            String orderId = (String) ent.getProperty(DataManager.PROPERTY_ORDER_ID);
            allOrders.add(new Order(orderId));
        }
        return allOrders;
    }
}