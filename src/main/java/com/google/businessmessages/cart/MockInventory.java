package com.google.businessmessages.cart;

import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

/**
 * Mock implementation of Inventory. This class pulls items from a pre-defined map of items 
 * to demonstrate the "Shop" feature in the bot.
 */
public class MockInventory implements Inventory {
    private ImmutableCollection<InventoryItem> inventoryItems;
    
    public MockInventory(Map<String, String> nameToMedia) {
        ImmutableCollection.Builder<InventoryItem> builder = new ImmutableList.Builder<>();
        for (Map.Entry<String, String> ent : nameToMedia.entrySet()) {
            builder.add(new InventoryItem(ent.getKey(), ent.getValue()));
        }
        inventoryItems = builder.build();
    }

    @Override
    public ImmutableList<InventoryItem> getInventory() {
        return (ImmutableList<InventoryItem>) inventoryItems;
    }

    @Override
    public Optional<InventoryItem> getItem(String itemId) {
        return inventoryItems.stream().filter(x -> x.getId().equals(itemId)).findFirst();
    }
}