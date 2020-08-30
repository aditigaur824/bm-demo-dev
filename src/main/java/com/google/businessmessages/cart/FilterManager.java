package com.google.businessmessages.cart;

import java.util.stream.Collectors;
import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.ImmutableList;

/**
 * Manages all operations that add, remove, modify, or view the filters a user sets.
 */
public class FilterManager {

    /**
     * Gets all filters associated with the user.
     * @param conversationId The unique id mapping between the user and the agent.
     * @return The list of active filters. Empty if there are none.
     */
    public static ImmutableList<Filter> getAllFilters(String conversationId) {
        return ImmutableList.copyOf(DataManager.getInstance().getFiltersFromData(conversationId)
            .stream()
            .map(ent -> 
                new Filter(
                    (String) ent.getProperty(DataManager.PROPERTY_FILTER_NAME),
                    (String) ent.getProperty(DataManager.PROPERTY_FILTER_VALUE))
            ).collect(Collectors.toList()));
    }

    /**
     * Gets the specific filter associated with the user if there is one. 
     * @param conversationId The unique id mapping between the user and the agent.
     * @param filterName The name of the filter being queried for.
     * @return The Filter object if there is one. Returns null if there is no filter
     * of filterName associated with the user.
     */
    public static Filter getFilter(String conversationId, String filterName) {
        DataManager dataManager = DataManager.getInstance(); 
        Entity filterEntity = dataManager.getExistingFilter(conversationId, filterName);
        if (filterEntity != null) {
            return new Filter((String) filterEntity.getProperty(DataManager.PROPERTY_FILTER_NAME), 
                (String) filterEntity.getProperty(DataManager.PROPERTY_FILTER_VALUE));
        }
        return null;
    }

    /**
     * Sets the given filter with the given filter value for the user.
     * @param conversationId The unique id mapping between the user and the agent.  
     * @param filterName The name of the filter being set.
     * @param filterValue The value the filter is being set with.
     */
    public static void setFilter(String conversationId, String filterName, String filterValue) {
        DataManager dataManager = DataManager.getInstance();
        dataManager.addFilter(conversationId, filterName, filterValue);
    }
    
    /**
     * Removes the given filter.
     * @param conversationId The unique id mapping between the user and the agent.
     * @param filterName The name of the filter being removed.
     */
    public static void removeFilter(String conversationId, String filterName) {
        DataManager dataManager = DataManager.getInstance(); 
        dataManager.addFilter(conversationId, filterName, "all");
    }
}