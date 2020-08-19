package com.google.businessmessages.cart;

import java.util.Date;

/**
 * Represents all information pertinent to a user's scheduled pickup. Each pickup corresponds to one
 * store order.
 */
public class Pickup {
    private String orderId;
    private String storeAddress;
    private Date date;
    private Status status;
    public enum Status {
        INCOMPLETE, SCHEDULED, CHECKED_IN, COMPLETE;
    }

    public Pickup(String orderId) {
        this.orderId = orderId;
        this.status = Status.INCOMPLETE;
    }

    public Pickup(String orderId, String storeAddress) {
        this.orderId = orderId;
        this.storeAddress = storeAddress;
        this.status = Status.INCOMPLETE;
    }

    public Pickup(String orderId, String storeAddress, Date date, Status status) {
        this.orderId = orderId;
        this.storeAddress = storeAddress;
        this.date = date;
        this.status = status;
    }

    /**
     * Gets the order id this pickup is scheduled for.
     * @return orderId The order this pickup corresponds to.
     */
    public String getOrderId() {
        return this.orderId;
    }

    /**
     * Gets the store address this pickup is scheduled at.
     * @return storeAddress The pickup location.
     */
    public String getStoreAddress() {
        return this.storeAddress;
    }

    /**
     * Gets the time the pickup is scheduled for.
     * @return time The pickup time.
     */
    public Date getTime() {
        return this.date;
    }

    /**
     * Gets the status of the pickup as defined in the Status enum.
     * @return status The status of the pickup (Scheduled, Checked In, Picked Up).
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * Sets the status of the pickup. (Scheduled, Checked In, Picked Up).
     */
    public void setStatus(Status status) {
        this.status = status;
    }
    
}