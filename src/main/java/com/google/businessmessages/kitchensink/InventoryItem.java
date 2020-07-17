package com.google.businessmessages.kitchensink;

import java.util.UUID;

public class InventoryItem {
    private String itemId;
    private String itemTitle;
    private String itemMediaURL;
    private double itemPrice = 0;

    public InventoryItem(String itemTitle, String itemMediaURL) {
        this.itemId = UUID.randomUUID().toString();
        this.itemTitle = itemTitle;
        this.itemMediaURL = itemMediaURL;
    }

    public InventoryItem(String itemTitle, String itemMediaURL, double itemPrice) {
        this(itemTitle, itemMediaURL);
        this.itemPrice = itemPrice;
    }

    /**
     * Gets the item's unique identifier.
     * @return itemId The unique id associated with this item.
     */
    public String getInventoryItemId() {
        return this.itemId;
    }

    /**
     * Gets the item's title.
     * @return itemTitle The title of the item.
     */
    public String getInventoryItemTitle() {
        return this.itemTitle;
    }

    /**
     * Gets the item's price.
     * @return itemPrice The price of the item.
     */
    public double getInventoryItemPrice() {
        return this.itemPrice;
    }

    /**
     * Gets the url at which the item's image is located.
     * @return itemMediaURL The url that leads to the image of this item.
     */
    public String getInventoryItemURL() {
        return this.itemMediaURL;
    }
}