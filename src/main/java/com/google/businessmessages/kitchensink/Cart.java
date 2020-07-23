package com.google.businessmessages.kitchensink;

import java.util.UUID;
import java.util.List;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.appengine.api.datastore.Entity;

/**
 * The cart class is responsible for keeping track of all items, CartItems, the user
 * adds to their shopping cart.
 */
public class Cart {
    private String cartId;
    private int size;
    private ImmutableCollection<CartItem> cartItems;

    public Cart(String conversationId) {
        DataManager dataManager = DataManager.getInstance();
        Entity cartEntity = dataManager.getCart(conversationId);
        if (cartEntity == null) {
            this.cartId = UUID.randomUUID().toString();
            dataManager.saveCart(conversationId, this.cartId);
        } else {
            this.cartId = (String) cartEntity.getProperty(DataManager.PROPERTY_CART_ID);
        }
        this.populate();
    }
    
    /**
     * Gets the unique id that belongs to this cart.
     * @return cartId The unique id belonging to the cart instance.
     */
    public String getId() {
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
     * Adds the specified item to the cart.
     * @param itemId The unique identifier of the item being added.
     * @param itemTitle The title of the item being added.
     */
    public void addItem(String itemId, String itemTitle) {
        DataManager dataManager = DataManager.getInstance();
        dataManager.addItemToCart(this.cartId, itemId, itemTitle);
        this.populate();
    }

    /**
     * Deletes the specified item from the cart.
     * @param itemId The unique identifier of the item being deleted.
     */
    public void deleteItem(String itemId) {
        DataManager dataManager = DataManager.getInstance();
        dataManager.deleteItemFromCart(this.cartId, itemId);
        this.populate();
    }

    /**
     * Populates the cart instance with all the items associated with it. Can be invoked upon
     * the initialization of a user's cart, or when items are added or deleted from the cart.
     */
    public void populate() {
        DataManager dataManager = DataManager.getInstance();
        List<Entity> itemList = dataManager.getCartFromData(this.cartId);
        this.size = itemList.size();
        if (itemList != null && !itemList.isEmpty()) {
            ImmutableCollection.Builder<CartItem> builder = new ImmutableList.Builder<>();
            for (Entity ent : itemList) {
                builder.add(new CartItem(ent));
            }
            this.cartItems = builder.build();
        }
    }
}