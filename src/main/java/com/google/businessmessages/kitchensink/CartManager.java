package com.google.businessmessages.kitchensink;

import java.util.List;
import java.util.UUID;
import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.ImmutableList;

/**
 * Manages communication between the Cart object and the data layer. Updates 
 * Cart data within the data layer and returns a new and updated Cart instance.
 */
public class CartManager {

    /**
     * Gets the existing Cart data associated with the given conversationId if it exists. 
     * Otherwise, creates new Cart data. Returns new instance of Cart based on data.
     * @param conversationId The unique id that maps between the agent and the user.
     * @return The new Cart instance constructed with persisted data, if any.
     */
    public static Cart getCart(String conversationId) {
        DataManager dataManager = DataManager.getInstance();
        Entity cartEntity = dataManager.getCart(conversationId);
        String cartId;
        if (cartEntity == null) {
            cartId = UUID.randomUUID().toString();
            dataManager.saveCart(conversationId, cartId);
        } else {
            cartId = (String) cartEntity.getProperty(DataManager.PROPERTY_CART_ID);
        }
        return new Cart(cartId, getCartItems(cartId));
    }
    
    /**
     * Gets the collection of items associated with the given cartId. Can be invoked upon
     * the initialization of a user's cart, or when items are added or deleted from the cart.
     * @param cartId The unique identifier of the cart whose items will be returned.
     * @return The immutable collection of items associated with the given cartId.
     */
    private static ImmutableList<CartItem> getCartItems(String cartId) {
        DataManager dataManager = DataManager.getInstance();
        List<Entity> itemList = dataManager.getCartFromData(cartId);
        if (itemList != null && !itemList.isEmpty()) {
            ImmutableList.Builder<CartItem> builder = new ImmutableList.Builder<>();
            for (Entity ent : itemList) {
                String id = (String) ent.getProperty(DataManager.PROPERTY_ITEM_ID);
                String title = (String) ent.getProperty(DataManager.PROPERTY_ITEM_TITLE);
                int count = ((Long) ent.getProperty(DataManager.PROPERTY_COUNT)).intValue();
                builder.add(new CartItem(id, title, count));
            }
            return builder.build();
        }
        return null;
    }

    /**
     * Adds the specified item to the cart and then returns a new instance of 
     * cart with an updated collection of items in it.
     * @param cartId the unique identifier of the cart this item will be added to.
     * @param itemId The unique identifier of the item being added.
     * @param itemTitle The title of the item being added.
     * @return The new instance of Cart with the updated collection of items.
     */
    public static Cart addItem(String cartId, String itemId, String itemTitle) {
        DataManager dataManager = DataManager.getInstance();
        dataManager.addItemToCart(cartId, itemId, itemTitle);
        return new Cart(cartId, getCartItems(cartId));
    }

    /**
     * Deletes the specified item from the cart and then returns a new instance of
     * cart with an updated collection of items in it. 
     * @param cartId The unique identifier of the cart this item will be deleted from.
     * @param itemId The unique identifier of the item being deleted.
     * @return The new instance of Cart with the updated collection of items.
     */
    public static Cart deleteItem(String cartId, String itemId) {
        DataManager dataManager = DataManager.getInstance();
        dataManager.deleteItemFromCart(cartId, itemId);
        return new Cart(cartId, getCartItems(cartId));
    }
}