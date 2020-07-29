package com.google.businessmessages.Cart;

import java.util.UUID;

/**
 * The item stored inside Inventory. Each item sold by a business can be encapsulated 
 * in an instance of this class. InventoryItem contains vital metadata about the item being
 * sold by the business.
 */
public class InventoryItem {
    private String id;
    private String title;
    private String mediaUrl;
    private double price;

    public InventoryItem(String itemTitle, String itemMediaURL) {
        this.id = UUID.nameUUIDFromBytes(itemTitle.getBytes()).toString();
        this.title = itemTitle;
        this.mediaUrl = itemMediaURL;
    }

    public InventoryItem(String itemTitle, String itemMediaURL, double itemPrice) {
        this(itemTitle, itemMediaURL);
        this.price = itemPrice;
    }

    /**
     * Gets the item's unique identifier.
     * @return id The unique id associated with this item.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the item's title.
     * @return title The title of the item.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Gets the item's price.
     * @return price The price of the item.
     */
    public double getPrice() {
        return this.price;
    }

    /**
     * Gets the url at which the item's image is located.
     * @return mediaUrl The url that leads to the image of this item.
     */
    public String getMediaUrl() {
        return this.mediaUrl;
    }
}