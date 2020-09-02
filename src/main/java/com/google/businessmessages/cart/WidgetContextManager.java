package com.google.businessmessages.cart;

import com.google.appengine.api.datastore.Entity;

/**
 * Responsible for managing all database operations pertaining to WidgetContext objects.
 */
public class WidgetContextManager {
    
    /**
     * Stores the specified widget context in the datastore so that the user will not be
     * prompted about the same context again.
     * @param conversationId The unique id mapping between the agent and the user.
     * @param context The context that is being saved.
     */
    public static void storeContext(String conversationId, WidgetContext context) {
        DataManager.getInstance().storeContext(conversationId, context);
    }

    /**
     * Deletes the specified widget context in the datastore so that the user can later be 
     * prompted about the same context (i.e. searches for running shoes once and then 
     * searches for running shoes again a few weeks later).
     * @param conversationId The unique id mapping between the agent and the user.
     * @param context The context that is being deleted.
     */
    public static void deleteContext(String conversationId, WidgetContext context) {
        DataManager.getInstance().deleteContext(conversationId, context);
    }

    /**
     * Returns whether the specified context is already in the database. If it has been 
     * seen already, will return true. If it has not, will return false.
     * @param conversationId The unique id mapping between the agent and the user.
     * @param context The context that is being searched for.
     */
    public static boolean hasBeenSeen(String conversationId, WidgetContext context) {
        Entity ent = DataManager.getInstance().getContext(conversationId, context);
        if (ent != null) {
            return true;
        }
        return false;
    }
}