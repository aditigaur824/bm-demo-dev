package com.google.businessmessages.cart;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;
import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.ImmutableList;

/**
 * Manages all database operations pertaining to storing and retrieving scheduled pickups.
 */
public class PickupManager {

    public static void addPickup(String conversationId, String orderId) {
        DataManager dataManager = DataManager.getInstance();
        dataManager.addPickup(conversationId, orderId);
    }

    public static void cancelPickup(String conversationId, String orderId) {
        DataManager dataManager = DataManager.getInstance();
        dataManager.cancelPickup(conversationId, orderId);
    }

    public static void updatePickupProperties(String conversationId, String orderId, String propertyName, Object propertyValue) {
        DataManager dataManager = DataManager.getInstance();
        dataManager.updatePickupProperties(conversationId, orderId, propertyName, propertyValue);
    }

    public static Pickup getPickup(String conversationId, String orderId) {
        return entityToPickup(DataManager.getInstance()
            .getExistingPickup(conversationId, orderId));
    }

    public static ImmutableList<Pickup> getAllPickups(String conversationId) {
        return ImmutableList.copyOf(DataManager.getInstance().getPickupsFromData(conversationId)
            .stream()
            .map(ent -> 
                entityToPickup(ent)
            ).collect(Collectors.toList()));
    }

    public static ImmutableList<Pickup> getPickupsWithStatus(String conversationId, Pickup.Status status) {
        return ImmutableList.copyOf(DataManager.getInstance().getPickupsWithStatus(conversationId, status)
            .stream()
            .map(ent -> 
                entityToPickup(ent)
            ).collect(Collectors.toList()));
    }

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
            if (pickupStatus.equals(DataManager.PICKUP_SCHEDULED_STATUS)) {
                return new Pickup(orderId, storeAddress, date, Pickup.Status.SCHEDULED);
            } else if (pickupStatus.equals(DataManager.PICKUP_CHECKED_IN_STATUS)) {
                return new Pickup(orderId, storeAddress, date, Pickup.Status.CHECKED_IN);
            } else {
                return new Pickup(orderId, storeAddress, date, Pickup.Status.COMPLETE);
            }
        }
    }

    public static Date createPickupDate(String timeZone, String dateString) {
        Calendar cal = new Calendar.Builder().setTimeZone(TimeZone.getTimeZone(timeZone))
                            .build();
        int year = 2020;
        String[] dateStringParts = dateString.split("/", 2);
        int month = Integer.parseInt(dateStringParts[0]) - 1;
        dateString = dateStringParts[1];
        dateStringParts = dateString.split("-", 2);
        int date = Integer.parseInt(dateStringParts[0]);
        dateString = dateStringParts[1];
        dateStringParts = dateString.split("-", 2);
        int hourOfDay = Integer.parseInt(dateStringParts[0]);
        int minute = 0;
        cal.set(year, month, date, hourOfDay, minute);
        return cal.getTime();
    }
    
}