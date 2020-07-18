package com.google.businessmessages.kitchensink;

import java.util.UUID;
import java.util.List;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.appengine.api.datastore.Entity;

public class Cart {
    private String cartId;
    private int size;
    private ImmutableCollection<CartItem> cartItems = null;

    public Cart(String conversationId) {
        Entity cartEntity = KitchenSinkBot.dataManager.getCart(conversationId);
        if (cartEntity == null) {
            this.cartId = UUID.randomUUID().toString();
            KitchenSinkBot.dataManager.saveCart(conversationId, this.cartId);
        } else {
            this.cartId = (String) cartEntity.getProperty(DataManager.PROPERTY_CART_ID);
        }
        this.populateWithItems();
    }
    
    /**
     * Gets the unique id that belongs to this cart.
     * @return cartId The unique id belonging to the cart instance.
     */
    public String getCartId() {
        return this.cartId;
    }

    /**
     * Gets the number of distinct items in this cart.
     * @return size The number of distinct items in the cart.
     */
    public int size() {
        return this.size;
    }

    /**
     * Gets the collection of items in the cart.
     * @return cartItems The collection of items associated with the cart instance.
     */
    public ImmutableCollection<CartItem> getCart() {
        return cartItems;
    }

    /**
     * Populates the cart instance with all the items associated with it. Can be invoked upon
     * the initialization of a user's cart, or when items are added or deleted from the cart.
     */
    public void populateWithItems() {
        List<Entity> itemList = KitchenSinkBot.dataManager.getCartFromData(this.cartId);
        this.size = itemList.size();
        if (!itemList.isEmpty()) {
            ImmutableCollection.Builder<CartItem> builder = new ImmutableList.Builder<>();
            for (Entity ent : itemList) {
                builder.add(new CartItem(ent));
            }
            this.cartItems = builder.build();
        }
    }
}