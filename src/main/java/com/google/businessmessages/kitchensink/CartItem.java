package com.google.businessmessages.kitchensink;

import com.google.appengine.api.datastore.Entity;

public class CartItem {
    private String itemId;
    private String itemTitle;
    private int itemCount;

    public CartItem(Entity itemEntity) {
        //Extracting relevant information from Datastore Entity
        this.itemId = (String) itemEntity.getProperty("item_id");
        this.itemTitle = (String) itemEntity.getProperty("item_title");
        this.itemCount = ((Long)itemEntity.getProperty("count")).intValue();
    }

    /**
     * Gets the unique item's unique identifier.
     * @return itemId The item's unique identifier.
     */
    public String getItemId() {
        return this.itemId;
    }

    /**
     * Gets the title of the item.
     * @return itemTitle The title of the item.
     */
    public String getItemTitle() {
        return this.itemTitle;
    }

    /**
     * Gets the count of this item in the cart it belongs to.
     * @return itemCount The count of the item.
     */
    public int getItemCount() {
        return this.itemCount;
    }
    
}