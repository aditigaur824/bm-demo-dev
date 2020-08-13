package com.google.businessmessages.cart;

/**
 * Represents all details of a user placed order needed to schedule a pickup/
 * track the status of the order.
 */
public class Order {
    private String id;

    public Order(String id) {
        this.id = id;
    }

    /**
     * Gets the order identifier.
     * @return id The order identifier.
     */
    public String getId() {
        return this.id;
    }
}