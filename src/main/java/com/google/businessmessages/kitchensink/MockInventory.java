package com.google.businessmessages.kitchensink;

import java.util.Map;
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
    public ImmutableCollection<InventoryItem> getInventory() {
        return inventoryItems;
    }
}