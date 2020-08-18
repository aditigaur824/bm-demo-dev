package com.google.businessmessages.cart;

import java.util.Date;
import java.util.List;
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

    public static void updatePickupProperties(String conversationId, String orderId, String propertyName, Object propertyValue) {
        DataManager dataManager = DataManager.getInstance();
        dataManager.updatePickupProperties(conversationId, orderId, propertyName, propertyValue);
    }

    public static ImmutableList<Pickup> getAllPickups(String conversationId) {
        ImmutableList.Builder<Pickup> builder = new ImmutableList.Builder<>();
        DataManager dataManager = DataManager.getInstance();
        List<Entity> pickupList = dataManager.getPickupsFromData(conversationId);
        for (Entity ent : pickupList) {
            String orderId = (String) ent.getProperty(DataManager.PROPERTY_ORDER_ID);
            String pickupStatus = (String) ent.getProperty(DataManager.PROPERTY_PICKUP_STATUS);
            if (pickupStatus.equals(DataManager.PICKUP_INCOMPLETE_STATUS)) {
                builder.add(new Pickup(orderId));
            } else {
                String storeAddress = (String) ent.getProperty(DataManager.PROPERTY_STORE_ADDRESS);
                Date date = (Date) ent.getProperty(DataManager.PROPERTY_PICKUP_TIME);
                if (pickupStatus.equals(DataManager.PICKUP_SCHEDULED_STATUS)) {
                    builder.add(new Pickup(orderId, storeAddress, date, Pickup.Status.SCHEDULED));
                } else if (pickupStatus.equals(DataManager.PICKUP_CHECKED_IN_STATUS)) {
                    builder.add(new Pickup(orderId, storeAddress, date, Pickup.Status.CHECKED_IN));
                } else {
                    builder.add(new Pickup(orderId, storeAddress, date, Pickup.Status.COMPLETE));
                }
            }
        }
        return builder.build();
    }

    public static ImmutableList<Pickup> getPickupsWithStatus(String conversationId, Pickup.Status status) {
        List<Entity> pickupsWithStatus = DataManager.getInstance().getPickupsWithStatus(conversationId, status);
        switch(status) {
            case INCOMPLETE:
                return ImmutableList.copyOf(pickupsWithStatus
                .stream()
                .map(ent -> 
                    new Pickup(
                        (String) ent.getProperty(DataManager.PROPERTY_ORDER_ID))
                ).collect(Collectors.toList()));
            default:
                return ImmutableList.copyOf(pickupsWithStatus
                .stream()
                .map(ent -> 
                    new Pickup(
                        (String) ent.getProperty(DataManager.PROPERTY_ORDER_ID),
                        (String) ent.getProperty(DataManager.PROPERTY_STORE_ADDRESS),
                        (Date) ent.getProperty(DataManager.PROPERTY_PICKUP_TIME),
                        status)
                ).collect(Collectors.toList()));
       }
    }
    
}