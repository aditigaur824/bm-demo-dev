package com.google.businessmessages.kitchensink;

import com.google.appengine.api.datastore.Entity;

public class CartItem {
    private String id;
    private String title;
    private int count;

    public CartItem(Entity itemEntity) {
        //Extracting relevant information from Datastore Entity
        this.id = (String) itemEntity.getProperty(DataManager.PROPERTY_ITEM_ID);
        this.title = (String) itemEntity.getProperty(DataManager.PROPERTY_ITEM_TITLE);
        this.count = ((Long)itemEntity.getProperty(DataManager.PROPERTY_COUNT)).intValue();
    }

    /**
     * Gets the unique item's unique identifier.
     * @return itemId The item's unique identifier.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the title of the item.
     * @return itemTitle The title of the item.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Gets the count of this item in the cart it belongs to.
     * @return itemCount The count of the item.
     */
    public int getCount() {
        return this.count;
    }
    
}