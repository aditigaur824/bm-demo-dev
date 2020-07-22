package com.google.businessmessages.kitchensink;

import java.util.Optional;
import com.google.common.collect.ImmutableCollection;

public interface Inventory {
    ImmutableCollection<InventoryItem> getInventory();
    Optional<InventoryItem> getItem(String itemId);
}