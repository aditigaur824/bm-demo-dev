package com.google.businessmessages.cart;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.common.collect.ImmutableList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Date;

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
    protected static final String WIDGET_CONTEXT_TYPE = "WidgetContext";
    //Properties of entities in datastore
    protected static final String PROPERTY_CONVERSATION_ID = "conversation_id";
    protected static final String PROPERTY_CART_ID = "cart_id";
    protected static final String PROPERTY_ITEM_ID = "item_id";
    protected static final String PROPERTY_ITEM_TITLE = "item_title";
    protected static final String PROPERTY_COUNT = "count";
    protected static final String PROPERTY_FILTER_NAME = "filter_name";
    protected static final String PROPERTY_FILTER_VALUE = "filter_value";
    protected static final String PROPERTY_ORDER_ID = "order_id";
    protected static final String PROPERTY_STORE_ADDRESS = "store_address";
    protected static final String PROPERTY_PICKUP_TIME = "pickup_time";
    protected static final String PROPERTY_PICKUP_STATUS = "pickup_status";
    protected static final String PROPERTY_PICKUP_ADDED_CAL = "pickup_cal";
    protected static final String PROPERTY_WIDGET_CONTEXT_STRING = "widget_context_string";
    //Types of pickup statuses in datastore
    protected static final String PICKUP_INCOMPLETE_STATUS = "incomplete";
    protected static final String PICKUP_SCHEDULED_STATUS = "scheduled";
    protected static final String PICKUP_CHECKED_IN_STATUS = "checked-in";
    protected static final String PICKUP_COMPLETED_STATUS = "complete";

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

    //Functions modifing/querying WidgetContext objects
    /**
     * Stores the widget context in the datastore so the bot can track that
     * this context has already been seen and responded to.
     * @param conversationId The unique id mapping between the agent and user.
     * @param context The context being stored.
     */
    public void storeContext(String conversationId, WidgetContext context) {
        Transaction transaction = datastore.beginTransaction();
        try {
            Entity contextEntity = new Entity(WIDGET_CONTEXT_TYPE);
            contextEntity.setProperty(PROPERTY_CONVERSATION_ID, conversationId);
            contextEntity.setProperty(PROPERTY_WIDGET_CONTEXT_STRING, context.getContext());
            datastore.put(transaction, contextEntity);
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
     * Deletes the widget context so that the user can have the same context
     * recognized in a later conversation.
     * @param conversationId The unique id mapping between the agent and user.
     * @param context The context being deleted.
     */
    public void deleteContext(String conversationId, WidgetContext context) {
        Transaction transaction = datastore.beginTransaction();
        Entity contextEntity = getContext(conversationId, context);
        try {
            if (contextEntity == null) {
                logger.log(Level.SEVERE, "Attempted deletion on null item.");
            } else {
                Key key = contextEntity.getKey();
                datastore.delete(transaction, key);
            }
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
     * Gets an existing widget context entity that matches the one given. If none
     * exist, returns null.
     * @param conversationId The unique id mapping between the agent and the user.
     * @param context The web widget context being queried for.
     * @return Gets the matching widget entity if it exists, returns null otherwise.
     */
    public Entity getContext(String conversationId, WidgetContext context) {
        final Query q = new Query(WIDGET_CONTEXT_TYPE)
        .setFilter(
                new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                    new Query.FilterPredicate(PROPERTY_CONVERSATION_ID, Query.FilterOperator.EQUAL, conversationId),
                    new Query.FilterPredicate(PROPERTY_WIDGET_CONTEXT_STRING, Query.FilterOperator.EQUAL, context.getContext()))));

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> contextResult = pq.asList(FetchOptions.Builder.withLimit(1));
        if (!contextResult.isEmpty()) {
            return contextResult.get(0);
        }
        return null;
    }


    //Functions modifying/querying Filter objects
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
        if (!filter.isEmpty()) {
            return filter.get(0);
        }
        return null;
    }

    /**
     * Queries the datastore for all of the filters the user has set.
     * @param conversationId The unique id mapping between the user and the agent.
     * @return A list of datastore entries if they exist.
     */
    public ImmutableList<Entity> getFiltersFromData(String conversationId) {
        final Query q = new Query(FILTER_TYPE)
                .setFilter(
                        new Query.FilterPredicate(PROPERTY_CONVERSATION_ID,
                                Query.FilterOperator.EQUAL,
                                conversationId)
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> filters = pq.asList(FetchOptions.Builder.withLimit(MAX_QUERY_LIMIT));
        return ImmutableList.copyOf(filters);
    }

    //Functions modifying/querying Cart objects.
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
     * Gets the user's conversationId by querying with the cartId.
     * @param cartId The cartId that will be used to return the user is exists.
     * Returns null otherwise.
     */
    public Entity getUserFromCartId(String cartId) {
        final Query q = new Query(CART_TYPE)
                .setFilter(
                        new Query.FilterPredicate(PROPERTY_CART_ID,
                                Query.FilterOperator.EQUAL,
                                cartId)
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> user = pq.asList(FetchOptions.Builder.withLimit(1));
        if (!user.isEmpty()) {
            return user.get(0);
        }
        return null;
    }



    //Functions modifying/querying for CartItem objects.
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
     * Empties the cart when the user has already checked out with all items in their cart.
     * @param cartId The identifier of the user's cart.
     */
    public void emptyCart(String cartId) {
        final Query q = new Query(CART_ITEM_TYPE)
                .setFilter(
                        new Query.FilterPredicate(PROPERTY_CART_ID,
                                Query.FilterOperator.EQUAL,
                                cartId)
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> currentCart = pq.asList(FetchOptions.Builder.withLimit(MAX_QUERY_LIMIT));
        
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction transaction = datastore.beginTransaction(options);
        try {
            currentCart.stream().forEach(ent -> datastore.delete(transaction, ent.getKey()));
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
    public ImmutableList<Entity> getCartFromData(String cartId) {

        final Query q = new Query(CART_ITEM_TYPE)
                .setFilter(
                        new Query.FilterPredicate(PROPERTY_CART_ID,
                                Query.FilterOperator.EQUAL,
                                cartId)
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> currentCart = pq.asList(FetchOptions.Builder.withLimit(MAX_QUERY_LIMIT));
        return ImmutableList.copyOf(currentCart);
    }

    //Functions modifying/querying for Order objects.
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
     * Queries the datastore for all of the orders associated with the user.
     * @param conversationId The unique id mapping between the user and the agent.
     * @return A list of datastore entries if they exist.
     */
    public ImmutableList<Entity> getOrdersFromData(String conversationId) {
        final Query q = new Query(ORDER_TYPE)
                .setFilter(
                        new Query.FilterPredicate(PROPERTY_CONVERSATION_ID,
                                Query.FilterOperator.EQUAL,
                                conversationId)
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> orders = pq.asList(FetchOptions.Builder.withLimit(MAX_QUERY_LIMIT));
        return ImmutableList.copyOf(orders);
    }

    //Functions modifying/querying for Pickup objects.
     /**
     * Converts the pickup status enum to a string equivalent capable of being stored 
     * in the database.
     * @param status The status being converted to a string.
     * @return The string mapping of the status enum.
     */
    private String pickupStatusToString(Pickup.Status status) {
        System.out.println(status);
        String queryStatus;
        if (status.equals(Pickup.Status.INCOMPLETE)) {
            queryStatus = PICKUP_INCOMPLETE_STATUS;
        } else if (status.equals(Pickup.Status.SCHEDULED)) {
            queryStatus = PICKUP_SCHEDULED_STATUS;
        } else if (status.equals(Pickup.Status.CHECKED_IN)) {
            queryStatus = PICKUP_CHECKED_IN_STATUS;
        } else if (status.equals(Pickup.Status.COMPLETE)) {
            queryStatus = PICKUP_COMPLETED_STATUS;
        } else {
            queryStatus = PICKUP_SCHEDULED_STATUS;
        }
        return queryStatus;
    }

    /**
     * Adds an incomplete pickup associated with the specified user and order.
     * @param conversationId The unique id mapping between the user and the agent.
     * @param orderId The unique id associated with the user's order.
     */
    public void addPickup(String conversationId, String orderId) {
        Transaction transaction = datastore.beginTransaction();
        try {
            Entity pickupEntity = new Entity(PICKUP_TYPE);
            pickupEntity.setProperty(PROPERTY_CONVERSATION_ID, conversationId);
            pickupEntity.setProperty(PROPERTY_ORDER_ID, orderId);
            pickupEntity.setProperty(PROPERTY_PICKUP_STATUS, PICKUP_INCOMPLETE_STATUS);
            datastore.put(pickupEntity);
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
     * Cancels a pickup by removing the associated entity from the database.
     * @param conversationId The unique id mapping between the user and the agent.
     * @param orderId The order for which the associated pickup is being removed.
     */
    public void cancelPickup(String conversationId, String orderId) {
        Transaction transaction = datastore.beginTransaction();
        Entity pickup = getExistingPickup(conversationId, orderId);
        try {
            // check if we are deleting null item
            if (pickup == null) {
                logger.log(Level.SEVERE, "Attempted pickup deletion on null item.");
            } else {
                Key key = pickup.getKey();
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
     * Gets all pickups from data associated with the specified user.
     * @param conversationId The unique id mapping between the user and the agent.
     * @return The list of pickup datastore entries if there are any.
     */
    public ImmutableList<Entity> getPickupsFromData(String conversationId) {
        final Query q = new Query(PICKUP_TYPE)
                .setFilter(
                        new Query.FilterPredicate(PROPERTY_CONVERSATION_ID,
                                Query.FilterOperator.EQUAL,
                                conversationId)
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> pickups = pq.asList(FetchOptions.Builder.withLimit(MAX_QUERY_LIMIT));
        return ImmutableList.copyOf(pickups);
    }
 
    /**
     * Gets all pickups with the specified status associated with the specified user.
     * @param conversationId The unique id mapping between the agent and the user.
     * @param status The status of the pickup that is being queried for.
     * @return The list of pickup datastore entries if there are any.
     */
    public ImmutableList<Entity> getPickupsWithStatus(String conversationId, Pickup.Status status) {
        String queryStatus = pickupStatusToString(status);
        final Query q = new Query(PICKUP_TYPE)
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate(PROPERTY_CONVERSATION_ID, Query.FilterOperator.EQUAL, conversationId),
                            new Query.FilterPredicate(PROPERTY_PICKUP_STATUS, Query.FilterOperator.EQUAL, queryStatus)))
                );

        PreparedQuery pq = datastore.prepare(q);
        List<Entity> pickups = pq.asList(FetchOptions.Builder.withLimit(MAX_QUERY_LIMIT));
        return ImmutableList.copyOf(pickups);
    }

    /**
     * Updates a property of the specified pickup. The pickup object is built through multiple interactions
     * with the user, so the metadata pertaining to the pickup will be added one at a time. This function
     * will add the field specified to the pickup object.
     * @param conversationId The unique id mapping between the agent and user.
     * @param orderId The order that this pickup pertains to.
     * @param propertyName The property of the pickup that is being updated.
     * @param propertyValue The new value the property will take on. This is a generic object because different
     * properties will have different data types.
     */
    public void updatePickupProperties(String conversationId, String orderId, String propertyName, Object propertyValue) {
        Transaction transaction = datastore.beginTransaction();
        try {
            Entity currentPickup = getExistingPickup(conversationId, orderId);
            if (currentPickup != null) {
                if (propertyName.equals(BotConstants.PICKUP_STATUS)) {
                    currentPickup.setProperty(PROPERTY_PICKUP_STATUS, pickupStatusToString((Pickup.Status) propertyValue));
                } else if (propertyName.equals(BotConstants.PICKUP_STORE_ADDRESS)) {
                    currentPickup.setProperty(PROPERTY_STORE_ADDRESS, (String) propertyValue);
                } else if (propertyName.equals(BotConstants.PICKUP_DATE)) {
                    currentPickup.setProperty(PROPERTY_PICKUP_TIME, (Date) propertyValue);
                } else if (propertyName.equals(BotConstants.PICKUP_ADDED_CALENDAR)) {
                    currentPickup.setProperty(PROPERTY_PICKUP_ADDED_CAL, (String) propertyValue);
                } else {
                    logger.log(Level.SEVERE, "Attempted to set invalid pickup property: " + propertyName + ".");
                }
            }
            datastore.put(transaction, currentPickup);
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
        if (!pickup.isEmpty()) {
            return pickup.get(0);
        }
        return null;
    }
}