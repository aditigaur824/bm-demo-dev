package com.google.businessmessages.cart;

import com.google.common.collect.ImmutableList;

/**
 * The Cart is responsible for keeping track of all items, CartItems, the user
 * adds to their shopping cart.
 */
public class Cart {
    private final String cartId;
    private final ImmutableList<CartItem> cartItems;

    public Cart(String cartId, ImmutableList<CartItem> cartItems) {
        this.cartId = cartId;
        this.cartItems = cartItems;
    }
    
    /**
     * Gets the unique id that belongs to this cart.
     * @return cartId The unique id belonging to the cart instance.
     */
    public String getId() {
        return this.cartId;
    }

    /**
     * Gets the collection of items in the cart.
     * @return cartItems The collection of items associated with the cart instance.
     */
    public ImmutableList<CartItem> getItems() {
        return cartItems;
    }

}