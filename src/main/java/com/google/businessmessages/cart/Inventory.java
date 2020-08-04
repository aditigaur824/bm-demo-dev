package com.google.businessmessages.cart;

import java.util.Optional;
import com.google.common.collect.ImmutableCollection;

/**
 * The Inventory is responsible for tracking a business's collection of items and 
 * supports two main functions: returning the entire collection of items and returning a 
 * particular item instance from the inventory.
 * 
 * This interface is currently implemented by MockInventory and will need to be implemented 
 * by a similar class for custom use.
 */
public interface Inventory {

    /**
     * Gets the collection of items in the inventory.
     * @return inventoryItems The collection of items in the inventory.
     */
    ImmutableCollection<InventoryItem> getInventory();

    /**
     * Gets the inventory item instance specified by the item id.
     * @param itemId The item's unique identifier.
     * @return item The Optional containing the InventoryItem instance if exists in the inventory, empty if it does not.
     */
    Optional<InventoryItem> getItem(String itemId);
}