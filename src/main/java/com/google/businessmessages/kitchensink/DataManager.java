package com.google.businessmessages.kitchensink;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import java.util.logging.Level;
import java.util.List;
import java.util.Arrays;
import java.util.ConcurrentModificationException;

public class DataManager {

    private static final int MAX_CART_LIMIT = 50;
    //Types of entities in datastore
    private static final String CART_TYPE = "Cart";
    private static final String CART_ITEM_TYPE = "CartItem";
    //Properties of the cart and cart item entities in datastore
    private static final String PROPERTY_CONVERSATION_ID = "conversation_id";
    private static final String PROPERTY_CART_ID = "cart_id";
    private static final String PROPERTY_ITEM_ID = "item_id";
    private static final String PROPERTY_ITEM_TITLE = "item_title";
    private static final String PROPERTY_COUNT = "count";

    private final DatastoreService datastore;

    public DataManager() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    public void saveCart(String conversationId, String cartId) {
        try {
            Entity cart = new Entity(CART_TYPE);
            cart.setProperty(PROPERTY_CONVERSATION_ID, conversationId);
            cart.setProperty(PROPERTY_CART_ID, cartId);
            datastore.put(cart);
        } catch (Exception e) {
            KitchenSinkBot.logger.log(Level.SEVERE, "Exception thrown while trying to add item to cart.", e);
        }
    }

    public Entity getCart(String conversationId) {

        final Query q = new Query(CART_TYPE)
                .setFilter(
                        new Query.FilterPredicate(PROPERTY_CONVERSATION_ID,
                                Query.FilterOperator.EQUAL,
                                conversationId)
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> cart = pq.asList(FetchOptions.Builder.withLimit(1));
        if (!cart.isEmpty()) {
            return cart.get(0);
        }
        
        return null;
    }

    /**
     * Adds an item to the user's cart persisted in memory. If the item already exists in
     * the user's cart, the count of the item is incremented. 
     * @param cartId The unique id that maps the user's cart to its associated items.
     * @param itemId The item's unique identifier.
     * @param itemTitle The title of the item that is being stored in the user's cart.
     */
    public void addItemToCart(String cartId, String itemId, String itemTitle) {
        Transaction transaction = datastore.beginTransaction();
        Entity currentItem = getExistingItem(cartId, itemId);
        
        try {
            // create a new cart item for the datastore if we do not have one already
            if (currentItem == null) {
                currentItem = new Entity(CART_ITEM_TYPE);
                currentItem.setProperty(PROPERTY_CART_ID, cartId);
                currentItem.setProperty(PROPERTY_ITEM_ID, itemId);
                currentItem.setProperty(PROPERTY_ITEM_TITLE, itemTitle);
                currentItem.setProperty(PROPERTY_COUNT, 1);
            } else {
              int count = ((Long)currentItem.getProperty(PROPERTY_COUNT)).intValue();
              currentItem.setProperty(PROPERTY_COUNT, count + 1);
            }

            datastore.put(transaction, currentItem);
            transaction.commit();
        } catch (IllegalStateException e) {
            KitchenSinkBot.logger.log(Level.SEVERE, "The transaction is not active.", e);
        } catch (ConcurrentModificationException e) {
            KitchenSinkBot.logger.log(Level.SEVERE, "The item is being concurrently modified.", e);
        } catch (DatastoreFailureException e) {
            KitchenSinkBot.logger.log(Level.SEVERE, "Datastore was not able to add the item.", e);
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Deletes an item from the user's cart persisted in memory. If there is more than one of 
     * the given item in the user's cart, the count of the item is decremented. 
     * @param cartId The unique id that maps between the user and the agent.
     * @param itemId The id of the item that is being deleted from the user's cart.
     */
    public void deleteItemFromCart(String cartId, String itemId) {
        Transaction transaction = datastore.beginTransaction();
        Entity currentItem = getExistingItem(cartId, itemId);
        try {
            // check if we are deleting null item
            if (currentItem == null) {
                KitchenSinkBot.logger.log(Level.SEVERE, "Attempted deletion on null item.");
            } else {
              int count = ((Long) currentItem.getProperty(PROPERTY_COUNT)).intValue();
              if (count == 1) {
                Key key = currentItem.getKey();
                datastore.delete(transaction, key);
              } else {
                currentItem.setProperty(PROPERTY_COUNT, count - 1);
                datastore.put(transaction, currentItem);
              }
            }
            transaction.commit();
        } catch (IllegalStateException e) {
            KitchenSinkBot.logger.log(Level.SEVERE, "The transaction is not active.", e);
        } catch (ConcurrentModificationException e) {
            KitchenSinkBot.logger.log(Level.SEVERE, "The item is being concurrently modified.", e);
        } catch (DatastoreFailureException e) {
            KitchenSinkBot.logger.log(Level.SEVERE, "Datastore was not able to delete the item.", e);
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Checks the datastore for a specific item in the user's cart.
     * @param cartId The unique id that maps the user's cart to its associated items.
     * @param itemId The id of the item we are looking for.
     * @return The datastore entry if it exists.
     */
    public Entity getExistingItem(String cartId, String itemId) {

        final Query q = new Query(CART_ITEM_TYPE)
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate(PROPERTY_CART_ID, Query.FilterOperator.EQUAL, cartId),
                            new Query.FilterPredicate(PROPERTY_ITEM_TITLE, Query.FilterOperator.EQUAL, itemId)))
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> currentCart = pq.asList(FetchOptions.Builder.withLimit(1));

        // return the current configuration settings
        if (!currentCart.isEmpty()) {
            return currentCart.get(0);
        }

        return null;
    }

    /**
     * Queries the datastore for all items in the user's cart.
     * @param cartId The unique id that maps between the user's cart and its 
     * associated items.
     * @return A list of datastore entries if they exist.
     */
    public List<Entity> getCartFromData(String cartId) {

        final Query q = new Query("CartItem")
                .setFilter(
                        new Query.FilterPredicate(PROPERTY_CART_ID,
                                Query.FilterOperator.EQUAL,
                                cartId)
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> currentCart = pq.asList(FetchOptions.Builder.withLimit(MAX_CART_LIMIT));
        return currentCart;
    }

}