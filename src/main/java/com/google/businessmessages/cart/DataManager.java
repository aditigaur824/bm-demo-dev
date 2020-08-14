package com.google.businessmessages.cart;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Arrays;
import java.util.ConcurrentModificationException;

/**
 * Wrapper layer to manage all Datastore queries and storage requests.
 */
public class DataManager {

    private static final int MAX_QUERY_LIMIT = 50;
    //Types of entities in datastore
    protected static final String CART_TYPE = "Cart";
    protected static final String CART_ITEM_TYPE = "CartItem";
    protected static final String FILTER_TYPE = "Filter";
    protected static final String ORDER_TYPE = "Order";
    protected static final String PICKUP_TYPE = "Pickup";
    //Properties of the cart and cart item entities in datastore
    protected static final String PROPERTY_CONVERSATION_ID = "conversation_id";
    protected static final String PROPERTY_CART_ID = "cart_id";
    protected static final String PROPERTY_ITEM_ID = "item_id";
    protected static final String PROPERTY_ITEM_TITLE = "item_title";
    protected static final String PROPERTY_COUNT = "count";
    protected static final String PROPERTY_FILTER_NAME = "filter_name";
    protected static final String PROPERTY_FILTER_VALUE = "filter_value";
    protected static final String PROPERTY_ORDER_ID = "order-id";
    protected static final String PROPERTY_STORE_ADDRESS = "store-address";
    protected static final String PROPERTY_PICKUP_TIME = "pickup-time";
    protected static final String PROPERTY_PICKUP_STATUS = "pickup-status";

    private static final Logger logger = Logger.getLogger(CartBot.class.getName());
    private final DatastoreService datastore;
    private static DataManager dataManager = new DataManager();

    private DataManager() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    /**
     * Returns the existing instance of the DataManager.
     * @return dataManager The single instance of DataManager.
     */
    public static DataManager getInstance() {
        return dataManager;
    }

    /**
     * Saves the user's cart to the datastore if the user has never created one 
     * in the past.
     * @param conversationId The unique id that maps between the user and the agent.
     * @param cartId The unique id that maps between the user and their cart.
     */
    public void saveCart(String conversationId, String cartId) {
        try {
            Entity cart = new Entity(CART_TYPE);
            cart.setProperty(PROPERTY_CONVERSATION_ID, conversationId);
            cart.setProperty(PROPERTY_CART_ID, cartId);
            datastore.put(cart);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "The cart entity is incomplete.", e);
        } catch (ConcurrentModificationException e) {
            logger.log(Level.SEVERE, "The item is being concurrently modified.", e);
        } catch (DatastoreFailureException e) {
            logger.log(Level.SEVERE, "Datastore was not able to add the item.", e);
        } 
    }

    /**
     * Gets the user's cart from the datastore, if there is one. Returns null 
     * otherwise. 
     * @param conversationId The unique id that maps between the user and the agent.
     */
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
     * Adds a user's filter to be persisted in memory. If the filter already exists in the 
     * data, then the value of the filter is updated to the most recent value the user
     * has chosen. 
     * @param conversationId The unique id mapping between a user and the agent.
     * @param filterName The name of the filter (i.e. size, color)
     * @param filterValue The value the filter is set for (i.e. blue, medium)
     */
    public void addFilter(String conversationId, String filterName, String filterValue) {
        Transaction transaction = datastore.beginTransaction();
        Entity currentFilter = getExistingFilter(conversationId, filterName);

        try {
            // create a new cart item for the datastore if we do not have one already
            if (currentFilter == null) {
                currentFilter = new Entity(FILTER_TYPE);
                currentFilter.setProperty(PROPERTY_CONVERSATION_ID, conversationId);
                currentFilter.setProperty(PROPERTY_FILTER_NAME, filterName);
                currentFilter.setProperty(PROPERTY_FILTER_VALUE, filterValue);
            } else {
              currentFilter.setProperty(PROPERTY_FILTER_VALUE, filterValue);
            }

            datastore.put(transaction, currentFilter);
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
     * Adds a user's order to the database. 
     * @param conversationId The unique id mapping between a user and the agent.
     * @param orderId The unique id belonging to the user's order.
     */
    public void addOrder(String conversationId, String orderId) {
        Transaction transaction = datastore.beginTransaction();
        try {
            Entity order = new Entity(ORDER_TYPE);
            order.setProperty(PROPERTY_CONVERSATION_ID, conversationId);
            order.setProperty(PROPERTY_ORDER_ID, orderId);
            datastore.put(transaction, order);
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
     * @param cartId The unique id that maps between the user and the agent.
     * @param itemId The id of the item that is being deleted from the user's cart.
     */
    public void deleteItemFromCart(String cartId, String itemId) {
        Transaction transaction = datastore.beginTransaction();
        Entity currentItem = getExistingItem(cartId, itemId);
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
     * Deletes the filter from the user's data. 
     * @param conversationId The unique id mapping between the user and the agent.
     * @param filterName The name of the filter being deleted (i.e. color, size)
     */
    public void removeFilter(String conversationId, String filterName) {
        Transaction transaction = datastore.beginTransaction();
        Entity filter = getExistingFilter(conversationId, filterName);
        try {
            // check if we are deleting null item
            if (filter == null) {
                logger.log(Level.SEVERE, "Attempted deletion on null item.");
            } else {
                Key key = filter.getKey();
                datastore.delete(transaction, key);
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
     * @param cartId The unique id that maps the user's cart to its associated items.
     * @param itemId The id of the item we are looking for.
     * @return The datastore entry if it exists.
     */
    public Entity getExistingItem(String cartId, String itemId) {

        final Query q = new Query(CART_ITEM_TYPE)
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate(PROPERTY_CART_ID, Query.FilterOperator.EQUAL, cartId),
                            new Query.FilterPredicate(PROPERTY_ITEM_ID, Query.FilterOperator.EQUAL, itemId)))
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
     * Checks the datastore for a specific filter associated with the user.
     * @param conversationId The unique id mapping between the user and the agent.
     * @param filterName The name of the filter being searched for.
     * @return The datastore entity if it exists. 
     */
    public Entity getExistingFilter(String conversationId, String filterName) {

        final Query q = new Query(FILTER_TYPE)
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate(PROPERTY_CONVERSATION_ID, Query.FilterOperator.EQUAL, conversationId),
                            new Query.FilterPredicate(PROPERTY_FILTER_NAME, Query.FilterOperator.EQUAL, filterName)))
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> filter = pq.asList(FetchOptions.Builder.withLimit(1));

        // return the current configuration settings
        if (!filter.isEmpty()) {
            return filter.get(0);
        }

        return null;
    }

    /**
     * Checks the datastore for a pickup associated with the provided order.
     * @param conversationId The unique id mapping between the user and the agent.
     * @param orderId The id of the order specified.
     * @return The datastore entity if it exists. 
     */
    public Entity getExistingPickup(String conversationId, String orderId) {

        final Query q = new Query(PICKUP_TYPE)
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate(PROPERTY_CONVERSATION_ID, Query.FilterOperator.EQUAL, conversationId),
                            new Query.FilterPredicate(PROPERTY_ORDER_ID, Query.FilterOperator.EQUAL, orderId)))
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> pickup = pq.asList(FetchOptions.Builder.withLimit(1));

        // return the current configuration settings
        if (!pickup.isEmpty()) {
            return pickup.get(0);
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

        final Query q = new Query(CART_ITEM_TYPE)
                .setFilter(
                        new Query.FilterPredicate(PROPERTY_CART_ID,
                                Query.FilterOperator.EQUAL,
                                cartId)
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> currentCart = pq.asList(FetchOptions.Builder.withLimit(MAX_QUERY_LIMIT));
        return currentCart;
    }

    /**
     * Queries the datastore for all of the filters the user has set.
     * @param conversationId The unique id mapping between the user and the agent.
     * @return A list of datastore entries if they exist.
     */
    public List<Entity> getFiltersFromData(String conversationId) {

        final Query q = new Query(FILTER_TYPE)
                .setFilter(
                        new Query.FilterPredicate(PROPERTY_CONVERSATION_ID,
                                Query.FilterOperator.EQUAL,
                                conversationId)
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> filters = pq.asList(FetchOptions.Builder.withLimit(MAX_QUERY_LIMIT));
        return filters;
    }
    
    public List<Entity> getOrdersFromData(String conversationId) {
        final Query q = new Query(ORDER_TYPE)
                .setFilter(
                        new Query.FilterPredicate(PROPERTY_CONVERSATION_ID,
                                Query.FilterOperator.EQUAL,
                                conversationId)
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> orders = pq.asList(FetchOptions.Builder.withLimit(MAX_QUERY_LIMIT));
        return orders;
    }

}