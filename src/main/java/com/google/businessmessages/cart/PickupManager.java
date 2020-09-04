package com.google.businessmessages.cart;

import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;
import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.ImmutableList;

/**
 * Manages all database operations pertaining to storing and retrieving scheduled pickups.
 */
public class PickupManager {

    /**
     * Adds the pickup that the user is in the process of building to the database.
     * @param conversationId The unique id mapping between the user and the agent.
     * @param orderId The identifier of the order for which the pickup is being scheduled.
     */
    public static void addPickup(String conversationId, String orderId) {
        DataManager dataManager = DataManager.getInstance();
        dataManager.addPickup(conversationId, orderId);
    }

    /**
     * Cancels the pickup the user was in the process of building and removes it from 
     * the database.
     * @param conversationId The unique id mapping between the user and the agent.
     * @param orderId The identifier of the order for which the pickup is being cancelled.
     */
    public static void cancelPickup(String conversationId, String orderId) {
        DataManager dataManager = DataManager.getInstance();
        dataManager.cancelPickup(conversationId, orderId);
    }

    /**
     * Updates the given property of the user's pickup in the database. Since the user
     * builds the pickup incrementally through multiple interactions with the bot, single
     * properties are updated at a time.
     * @param conversationId The unique id mapping between the user and the agent.
     * @param orderId The order identifier for which the associated pickup is being updated.
     * @param propertyName The name of the pickup field being updated.
     * @param propertyValue The new value of the pickup field being updated.
     */
    public static void updatePickupProperties(String conversationId, String orderId, String propertyName, Object propertyValue) {
        DataManager dataManager = DataManager.getInstance();
        dataManager.updatePickupProperties(conversationId, orderId, propertyName, propertyValue);
    }

    /**
     * Gets the given pickup specified by the user's id and the order they have placed.
     * @param conversationId The unique id mapping between the user and the agent.
     * @param orderId The order idenfier for which the associated pickup is being retrieved.
     * @return The Pickup associated with the specified order.
     */
    public static Pickup getPickup(String conversationId, String orderId) {
        return entityToPickup(DataManager.getInstance()
            .getExistingPickup(conversationId, orderId));
    }

    /**
     * Gets all pickups associated with the specified user.
     * @param conversationId The unique id mapping between the user and the agent.
     * @return The list of pickups associated with the user. Empty if there are none.
     */
    public static ImmutableList<Pickup> getAllPickups(String conversationId) {
        return ImmutableList.copyOf(DataManager.getInstance().getPickupsFromData(conversationId)
            .stream()
            .map(ent -> entityToPickup(ent))
            .collect(Collectors.toList()));
    }

    /**
     * Gets all the pickups associated with the user with a given status (scheduled, checked-in, etc.)
     * @param conversationId The unique id mapping between the user and the agent. 
     * @param status The status of pickups being retrieved.
     * @return The list of pickups. Empty if there are none.
     */
    public static ImmutableList<Pickup> getPickupsWithStatus(String conversationId, Pickup.Status status) {
        return ImmutableList.copyOf(DataManager.getInstance().getPickupsWithStatus(conversationId, status)
            .stream()
            .map(ent -> entityToPickup(ent))
            .collect(Collectors.toList()));
    }

    /**
     * Converts an Entity datatype returned by datastore to a Pickup object.
     * @param pickupEntity The entity to be converted to a pickup object.
     * @return The pickup object.
     */
    private static Pickup entityToPickup(Entity pickupEntity) {
        String orderId = (String) pickupEntity.getProperty(DataManager.PROPERTY_ORDER_ID);
        String pickupStatus = (String) pickupEntity.getProperty(DataManager.PROPERTY_PICKUP_STATUS);
        if (pickupStatus.equals(DataManager.PICKUP_INCOMPLETE_STATUS)) {
            if (pickupEntity.hasProperty(DataManager.PROPERTY_STORE_ADDRESS)) {
                String storeAddress = (String) pickupEntity.getProperty(DataManager.PROPERTY_STORE_ADDRESS);
                return new Pickup(orderId, storeAddress);
            }
            return new Pickup(orderId);
        } else {
            String storeAddress = (String) pickupEntity.getProperty(DataManager.PROPERTY_STORE_ADDRESS);
            Date date = (Date) pickupEntity.getProperty(DataManager.PROPERTY_PICKUP_TIME);
            boolean addedToCal = false;
            if (pickupEntity.hasProperty(DataManager.PROPERTY_PICKUP_ADDED_CAL)) {
                addedToCal = true;
            }
            if (pickupStatus.equals(DataManager.PICKUP_SCHEDULED_STATUS)) {
                return new Pickup(orderId, storeAddress, date, Pickup.Status.SCHEDULED, addedToCal);
            } else if (pickupStatus.equals(DataManager.PICKUP_CHECKED_IN_STATUS)) {
                return new Pickup(orderId, storeAddress, date, Pickup.Status.CHECKED_IN, addedToCal);
            } else {
                return new Pickup(orderId, storeAddress, date, Pickup.Status.COMPLETE, addedToCal);
            }
        }
    }

    /**
     * Creates a pickup Date object by parsing the inputted time zone and date strings.
     * @param timeZoneOffset The timezone offset off of UTC that the dateString assumes.
     * @param dateString The string containing the month, date, and hour of the user's
     * scheduled pickup.
     * @return The date object containing the time of the pickup.
     */
    public static Date createPickupDate(int timeZoneOffset, String dateString) {
        Calendar cal = new Calendar.Builder().build();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String[] dateStringParts = dateString.split("/", 2);
        int month = Integer.parseInt(dateStringParts[0]) - 1;
        dateString = dateStringParts[1];
        dateStringParts = dateString.split("-", 2);
        int date = Integer.parseInt(dateStringParts[0]);
        dateString = dateStringParts[1];
        dateStringParts = dateString.split("-", 2);
        int hourOfDay = Integer.parseInt(dateStringParts[0]) + timeZoneOffset;
        int minute = 0;
        cal.set(year, month, date, hourOfDay, minute);
        return cal.getTime();
    }
}