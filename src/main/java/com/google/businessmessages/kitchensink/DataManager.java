package com.google.businessmessages.kitchensink;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Arrays;

public class DataManager {

    private static final String EXCEPTION_WAS_THROWN = "exception";
    private static final String DELETING_NULL_ITEM = "Attempted deletion on null item.";
    private DatastoreService datastore;

    public DataManager() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    /**
     * Adds an item to the user's cart persisted in memory. 
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
                currentItem.setProperty("conversation_id", conversationId);
                currentItem.setProperty("item_title", itemTitle);
                currentItem.setProperty("count", 1);
            } else {
              int count = ((Long)currentItem.getProperty("count")).intValue();
              currentItem.setProperty("count", count+1);
            }
            
            datastore.put(transaction, currentItem);
            transaction.commit();
        } catch (Exception e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Deletes an item from the user's cart persisted in memory.
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
                logger.log(Level.SEVERE, DELETING_NULL_ITEM);
            } else {
              int count = ((Long)currentItem.getProperty("count")).intValue();
              if (count == 1) {
                Key key = currentItem.getKey();
                datastore.delete(transaction, key);
              } else {
                currentItem.setProperty("count", count-1);
                datastore.put(transaction, currentItem);
              }
            }
            transaction.commit();
        } catch (Exception e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
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
                        new Query.FilterPredicate("conversation_id", Query.FilterOperator.EQUAL, conversationId),
                        new Query.FilterPredicate("item_title", Query.FilterOperator.EQUAL, itemTitle)))
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
                        new Query.FilterPredicate("conversation_id",
                                Query.FilterOperator.EQUAL,
                                conversationId)
                );

        Transaction transaction = datastore.beginTransaction();
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> currentCart = pq.asList(FetchOptions.Builder.withLimit(50));
        transaction.commit();

        // return the current configuration settings
        if (!currentCart.isEmpty()) {
            return currentCart;
        }

        return null;
    }

}