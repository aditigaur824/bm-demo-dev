package com.google.businessmessages.kitchensink;

import java.util.Map;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

public class MockInventory implements Inventory {
    private ImmutableCollection<InventoryItem> inventoryItems;
    
    public MockInventory(Map<String, String> nameToMedia) {
        ImmutableCollection.Builder<InventoryItem> builder = new ImmutableList.Builder<>();
        for (Map.Entry<String, String> ent : nameToMedia.entrySet()) {
            builder.add(new InventoryItem(ent.getKey(), ent.getValue()));
        }
        inventoryItems = builder.build();
    }

    /**
     * Gets the collection of items in the inventory.
     * @return inventoryItems The collection of items in the inventory.
     */
    public ImmutableCollection<InventoryItem> getInventory() {
        return inventoryItems;
    }

    /**
     * Gets the inventory item instance specified by the item id.
     * @param itemId The item's unique identifier.
     * @return item The InventoryItem instance.
     */
    public InventoryItem getItem(String itemId) {
        UnmodifiableIterator<InventoryItem> iterator = inventoryItems.iterator();
        while(iterator.hasNext()) {
            InventoryItem currentItem = iterator.next();
            if (currentItem.getInventoryItemId().equals(itemId)) {
                return currentItem;
            }
        }
        return null;
    }
}