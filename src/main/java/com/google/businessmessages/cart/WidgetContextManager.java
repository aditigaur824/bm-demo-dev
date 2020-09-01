package com.google.businessmessages.cart;

import com.google.appengine.api.datastore.Entity;

public class WidgetContextManager {
    
    public static void storeContext(String conversationId, WidgetContext context) {
        DataManager.getInstance().storeContext(conversationId, context);
    }

    public static void deleteContext(String conversationId, WidgetContext context) {
        DataManager.getInstance().deleteContext(conversationId, context);
    }

    public static boolean hasBeenSeen(String conversationId, WidgetContext context) {
        Entity ent = DataManager.getInstance().getContext(conversationId, context);
        if (ent != null) {
            return true;
        }
        return false;
    }
}