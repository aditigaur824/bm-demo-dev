package com.google.businessmessages.cart;

import java.util.List;

/**
 * Represents the properties applicable to an inventory item and all the options
 * available for that property. (i.e. An item has property size which has options
 * 5, 6, and 7.)
 */
public class ItemProperty {
    private String name;
    private List<String> options;

    public ItemProperty(String name, List<String> options) {
        this.name = name;
        this.options = options;
    }

    /**
     * Gets the name of the property.
     * @return name Name of the property.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the options available for the property.
     * @return options The options available for the property.
     */
    public List<String> getOptions() {
        return this.options;
    }
}