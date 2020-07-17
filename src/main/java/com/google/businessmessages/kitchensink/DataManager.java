package com.google.businessmessages.kitchensink;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Arrays;
import java.util.ConcurrentModificationException;

public class DataManager {

    private static final int MAX_CART_LIMIT = 50;
    //Properties of the cart entity in datastore
    private static final String PROPERTY_CONVERSATION_ID = "conversation_id";
    private static final String PROPERTY_ITEM_TITLE = "item_title";
    private static final String PROPERTY_COUNT = "count";

    private final DatastoreService datastore;

    public DataManager() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    /**
     * Adds an item to the user's cart persisted in memory. If the item already exists in
     * the user's cart, the count of the item is incremented. 
     * @param conversationId The unique id that maps between the user and the agent.
     * @param itemTitle The title of the item that is being stored in the user's cart.
     * @param logger The logger passed in by the agent to record any potential errors.
     */
    public void addItemToCart(String conversationId, String itemTitle, Logger logger) {
        Transaction transaction = datastore.beginTransaction();
        Entity currentItem = getExistingItem(conversationId, itemTitle);
  
        try {
            // create a new cart item for the datastore if we do not have one already
            if (currentItem == null) {
                currentItem = new Entity("CartItem");
                currentItem.setProperty(PROPERTY_CONVERSATION_ID, conversationId);
                currentItem.setProperty(PROPERTY_ITEM_TITLE, itemTitle);
                currentItem.setProperty(PROPERTY_COUNT, 1);
            } else {
              int count = ((Long)currentItem.getProperty(PROPERTY_COUNT)).intValue();
              currentItem.setProperty(PROPERTY_COUNT, count + 1);
            }

            datastore.put(transaction, currentItem);
            transaction.commit();
        } catch (IllegalStateException e) {
            logger.log(Level.SEVERE, "The transaction is not active.", e);
        } catch (ConcurrentModificationException e) {
            logger.log(Level.SEVERE, "The item is being concurrently modified.", e);
        } catch (DatastoreFailureException e) {
            logger.log(Level.SEVERE, "Datastore was not able to add the item.", e);
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Deletes an item from the user's cart persisted in memory. If there is more than one of 
     * the given item in the user's cart, the count of the item is decremented. 
     * @param conversationId The unique id that maps between the user and the agent.
     * @param itemTitle The title of the item that is being deleted from the user's cart.
     * @param logger The logged passed in by the agent to record any potential errors.
     */
    public void deleteItemFromCart(String conversationId, String itemTitle, Logger logger) {
        Transaction transaction = datastore.beginTransaction();
        Entity currentItem = getExistingItem(conversationId, itemTitle);
        try {
            // check if we are deleting null item
            if (currentItem == null) {
                logger.log(Level.SEVERE, "Attempted deletion on null item.");
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
            logger.log(Level.SEVERE, "The transaction is not active.", e);
        } catch (ConcurrentModificationException e) {
            logger.log(Level.SEVERE, "The item is being concurrently modified.", e);
        } catch (DatastoreFailureException e) {
            logger.log(Level.SEVERE, "Datastore was not able to delete the item.", e);
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Checks the datastore for a specific item in the user's cart.
     * @param conversationId The unique id that maps between the user and the agent.
     * @param itemTitle The title of the item we are looking for.
     * @return The datastore entry if it exists.
     */
    public Entity getExistingItem(String conversationId, String itemTitle) {

        final Query q = new Query("CartItem")
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate(PROPERTY_CONVERSATION_ID, Query.FilterOperator.EQUAL, conversationId),
                            new Query.FilterPredicate(PROPERTY_ITEM_TITLE, Query.FilterOperator.EQUAL, itemTitle)))
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
     * @param conversationId The unique id that maps between the user and the agent.
     * @return A list of datastore entries if they exist.
     */
    public List<Entity> getCartFromData(String conversationId) {

        final Query q = new Query("CartItem")
                .setFilter(
                        new Query.FilterPredicate(PROPERTY_CONVERSATION_ID,
                                Query.FilterOperator.EQUAL,
                                conversationId)
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> currentCart = pq.asList(FetchOptions.Builder.withLimit(MAX_CART_LIMIT));
        return currentCart;
    }

}