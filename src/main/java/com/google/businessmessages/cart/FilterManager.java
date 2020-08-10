package com.google.businessmessages.cart;

import java.util.ArrayList;
import java.util.List;
import com.google.appengine.api.datastore.Entity;

public class FilterManager {

    public static List<Filter> getActiveFilters(String conversationId) {
        List<Filter> activeFilters = new ArrayList<>();
        DataManager dataManager = DataManager.getInstance();
        List<Entity> filters = dataManager.getFiltersFromData(conversationId);
        for (Entity ent : filters) {
            String filterName = (String) ent.getProperty(DataManager.PROPERTY_FILTER_NAME);
            String filterValue = (String) ent.getProperty(DataManager.PROPERTY_FILTER_VALUE);
            activeFilters.add(new Filter(filterName, filterValue));
        }
        return activeFilters;
    }

    public static Filter getFilter(String conversationId, String filterName) {
        DataManager dataManager = DataManager.getInstance(); 
        Entity filterEntity = dataManager.getExistingFilter(conversationId, filterName);
        if (filterEntity != null) {
            return new Filter((String) filterEntity.getProperty(DataManager.PROPERTY_FILTER_NAME), 
                (String) filterEntity.getProperty(DataManager.PROPERTY_FILTER_VALUE));
        }
        return null;
    }

    public static void setFilter(String conversationId, String filterName, String filterValue) {
        DataManager dataManager = DataManager.getInstance();
        dataManager.addFilter(conversationId, filterName, filterValue);
    }
    
    public static void removeFilter(String conversationId, String filterName) {
        DataManager dataManager = DataManager.getInstance(); 
        dataManager.removeFilter(conversationId, filterName);
    }
}