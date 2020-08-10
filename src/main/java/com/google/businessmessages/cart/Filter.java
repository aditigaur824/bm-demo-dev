package com.google.businessmessages.cart;

/**
 * The filter object representing the name of the filter (i.e. Color) and the 
 * value it is being set to (i.e. blue).
 */
public class Filter {
    private final String name;
    private String value;
    
    public Filter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the name of the filter.
     * @return The name of the filter.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of the filter.
     * @return The value of the filter.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value of the filter to the given value.
     * @param value The value the filter will be set to.
     */
    public void setValue(String value) {
        this.value = value;
    }
}