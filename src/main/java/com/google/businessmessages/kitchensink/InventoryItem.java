package com.google.businessmessages.kitchensink;

import java.util.UUID;

public class InventoryItem {
    private String itemId;
    private String itemTitle;
    private double itemPrice;
    private String itemMediaURL;

    public InventoryItem(String itemTitle, String itemMediaURL) {
        this.itemId = UUID.randomUUID().toString();
        this.itemTitle = itemTitle;
        this.itemMediaURL = itemMediaURL;
        this.itemPrice = 0;
    }

    public InventoryItem(String itemTitle, String itemMediaURL, double itemPrice) {
        this.itemId = UUID.randomUUID().toString();
        this.itemTitle = itemTitle;
        this.itemMediaURL = itemMediaURL;
        this.itemPrice = itemPrice;
    }

    public String getInventoryItemId() {
        return this.itemId;
    }

    public String getInventoryItemTitle() {
        return this.itemTitle;
    }

    public double getInventoryItemPrice() {
        return this.itemPrice;
    }

    public String getInventoryItemURL() {
        return this.itemMediaURL;
    }
}