package com.google.businessmessages.cart;

/**
 * Represents a BM third party widget context within an object. Contains the context
 * string to track whether the context has already been addressed by the bot to avoid
 * repetitive prompting to the user.
 */
public class WidgetContext {
    private String context;

    public WidgetContext(String context)  {
        this.context = context;
    }

    /**
     * Returns the context string.
     * @return The context string.
     */
    public String getContext() {
        return this.context;
    }
    
}