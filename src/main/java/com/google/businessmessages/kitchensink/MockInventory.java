package com.google.businessmessages.kitchensink;

import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

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
    public ImmutableList<InventoryItem> getInventory() {
        return (ImmutableList<InventoryItem>) inventoryItems;
    }

    /**
     * Gets the inventory item instance specified by the item id.
     * @param itemId The item's unique identifier.
     * @return item The Optional containing the InventoryItem instance if exists in the inventory, empty if it does not.
     */
    public Optional<InventoryItem> getItem(String itemId) {
        return inventoryItems.stream().filter(x -> x.getId().equals(itemId)).findFirst();
    }
}