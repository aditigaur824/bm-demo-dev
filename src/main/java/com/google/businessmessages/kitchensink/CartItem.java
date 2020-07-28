package com.google.businessmessages.kitchensink;

/**
 * The CartItem instance encapsulates relevant metadata about an item the user has 
 * added to their shopping cart. Each CartItem's itemId corresponds to an InventoryItem's
 * itemId.
 */
public class CartItem {
    private final String id;
    private final String title;
    private final int count;

    public CartItem(String id, String title, int count) {
        this.id = id;
        this.title = title;
        this.count = count;
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