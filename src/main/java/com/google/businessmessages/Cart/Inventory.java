package com.google.businessmessages.Cart;

import com.google.common.collect.ImmutableCollection;

public interface Inventory {
    public ImmutableCollection<InventoryItem> getInventory();
    public InventoryItem getItem(String itemId);
}