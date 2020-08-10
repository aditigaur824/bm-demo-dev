package com.google.businessmessages.cart;

public class Filter {
    private final String name;
    private String value;
    
    public Filter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}