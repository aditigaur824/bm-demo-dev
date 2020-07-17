package com.google.businessmessages.kitchensink;

import com.google.common.collect.ImmutableCollection;

public interface Inventory {
    public ImmutableCollection<InventoryItem> getInventory();
    public InventoryItem getItem(String itemId);
}