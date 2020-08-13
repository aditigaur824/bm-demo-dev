/*
 * Copyright (C) 2020 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.businessmessages.cart;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Defines constants used by CartBot.
 */
public interface BotConstants {

        String CREDENTIALS_FILE_NAME = "bm-agent-service-account-credentials.json";

        // the URL for the API endpoint
        String BM_API_URL = "https://businessmessages.googleapis.com/";

        String LIVE_AGENT_NAME = "Sally";
        String BOT_AGENT_NAME = "BM Cart Bot";

        //List of filter names
        String COLOR_FILTER_NAME = "color";
        String BRAND_FILTER_NAME = "brand";
        String SIZE_FILTER_NAME = "size";

        //List of workflow indicators
        String FROM_INVENTORY_CALLBACK = "From inventory callback.";

        // List of suggestion strings
        String FILTERS_TEXT = "See/Edit My Filters";
        String EDIT_FILTER_TEXT = "Edit this Filter";
        String VIEW_CART_TEXT = "View Cart";
        String CONTINUE_SHOPPING_TEXT = "Continue Shopping";
        String SHOP_TEXT = "Shop Our Collection";
        String HOURS_TEXT = "Inquire About Hours";
        String HELP_TEXT = "Help";
        String ADD_ITEM_TEXT = "\uD83D\uDED2 Add to Cart";
        String INCREMENT_COUNT_TEXT = "\u2795";
        String DECREMENT_COUNT_TEXT = "\u2796";

        // List of recognized commands to produce certain responses
        String INIT_FILTER_COMMAND = "init-filter-";
        String SEE_FILTERS_COMMAND = "see-filters";
        String SEE_FILTER_OPTIONS_COMMAND = "see-filter-options-";
        String SET_FILTER_COMMAND = "set-filter-";
        String REMOVE_FILTER_COMMAND = "remove-filter-";
        String DELETE_ITEM_COMMAND = "del-cart-";
        String ADD_ITEM_COMMAND = "add-cart-";
        String VIEW_CART_COMMAND = "cart";
        String HOURS_COMMAND = "hours";
        String SHOP_COMMAND = "shop";
        String HELP_COMMAND = "^help.*|^commands\\s.*|see the help menu";

        // List of pre-programmed responses
        String DEFAULT_RESPONSE_TEXT =
                "Sorry, I didn't quite get that. Perhaps you were looking for one of these options?";

        String HOURS_RESPONSE_TEXT = "We are open Monday - Friday from 9 A.M. to 5 P.M.";

        String HELP_RESPONSE_TEXT = "Welcome to the help menu! This program will echo "
                + "any text that you enter that is not part of a supported command. The supported "
                + "commands are: \n\n" + "Help - Shows the list of supported commands and functions\n\n"
                + "Inquire About Hours - Will respond with the times that our store is open.\n\n"
                + "Shop Our Collection/Continue Shopping - Will respond with a collection of mock inventory items.\n\n"
                + "View Cart - Will respond with all of the items in your cart.\n\n";
        
        String SIZE_FILTER_RESPONSE_TEXT = "Woohoo! 🎉 I'd love to help you shop! Before we get started, I just need to know a few things about you.\n\n"
                + "First off, what shoe size are we looking for today?";
        
        String BRAND_FILTER_RESPONSE_TEXT = "Awesome! I'll make sure we look for shoes in that size today! 😊 \n\n"
                + "Do you mind telling me what brand of shoes you were looking for today? " 
                + "If you don't have a preference for any of the brands listed below, feel free to select 'all'!";
        
        String COLOR_FILTER_RESPONSE_TEXT = "Thanks! One final question: what color of shoes were you looking for? "
                + "Once again, if you want to see shoes in all of our colors, you can select 'all'";

        String FILTER_SELECTION_COMPLETE_RESPONSE_TEXT = "Whew! 😌 Thanks for bearing with all of my questions. "
                + "I've saved all of your preferences as filters that you can view and edit whenever you'd like while you shop!\n\n" 
                + "Now, I'll pull up your customized running shoe recommendations! 👟 ";
        
        // Data pertaining to filters and inventory items.
        String colorCardImage = "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/color_card_image.jpeg";
        List<String> colorList = Arrays.asList("All", "Blue", "Neon", "Pink", "White", "Purple", "Black");

        String brandCardImage = "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/brand_collage.jpg";
        List<String> brandList = Arrays.asList("All", "Adidas", "Nike", "New Balance", "Asics");

        String sizeCardImage = "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/size_card_image.jpg";
        List<String> sizeList = Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12");

        Map<String, String> INVENTORY_IMAGES = new HashMap<String, String>() {{
                put("Asics Blue Running Shoes",
                        "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/blue_running_shoes.jpeg");
                put("Adidas Neon Running Shoes",
                        "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/neon_running_shoes.jpg");
                put("Asics Pink Running Shoes",
                        "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/pink_running_shoes.jpeg");
                put("Asics Purple Running Shoes",
                        "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/teal_running_shoes.jpg");
                put("Asics White Running Shoes",
                        "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/white_running_shoes.jpg");
                put("Nike Neon and Grey Running Shoes",
                        "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/neon_yellow_running_shoes.jpg");
                put("New Balance Black Running Shoes",
                        "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/black_running_shoes.jpg");
                put("New Balance White Running Shoes",
                        "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/white_running_shoes_nb.jpeg");
                put("Nike Neon Blue Running Shoes",
                        "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/blue_neon_running_shoes_nike.jpg");
                put("Nike Navy Running Shoes",
                        "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/blue_running_shoes_nike.jpg");
                put("Asics Neon Orange Running Shoes", 
                        "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/orange_neon_running_shoes_asics.jpeg");
                put("New Balance Neon Pink Running Shoes",
                        "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/pink_neon_running_shoes_nb.jpg");
                put("Asics Pink and Purple Running Shoes",
                        "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/pink_purple_running_shoes.jpeg");
        }};

        Map<String, Map<String, List<String>>> INVENTORY_PROPERTIES = new HashMap<String, Map<String, List<String>>>() {{
                put("Asics Blue Running Shoes", ImmutableMap.of("size", Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                "color", Arrays.asList("blue"),
                "brand", Arrays.asList("asics")));
                put("Adidas Neon Running Shoes", ImmutableMap.of("size", Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                "color", Arrays.asList("neon"),
                "brand", Arrays.asList("adidas")));
                put("Asics Pink Running Shoes", ImmutableMap.of("size", Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                "color", Arrays.asList("pink"),
                "brand", Arrays.asList("asics")));
                put("Asics Purple Running Shoes", ImmutableMap.of("size", Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                "color", Arrays.asList("purple", "pink"),
                "brand", Arrays.asList("asics")));
                put("Asics White Running Shoes", ImmutableMap.of("size", Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                "color", Arrays.asList("blue", "white", "purple"),
                "brand", Arrays.asList("asics")));
                put("Nike Neon and Grey Running Shoes", ImmutableMap.of("size", Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                "color", Arrays.asList("neon", "grey"),
                "brand", Arrays.asList("nike")));
                put("New Balance Black Running Shoes", ImmutableMap.of("size", Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                "color", Arrays.asList("black"),
                "brand", Arrays.asList("new balance")));
                put("New Balance White Running Shoes", ImmutableMap.of("size", Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                "color", Arrays.asList("white"),
                "brand", Arrays.asList("new balance")));
                put("Nike Neon Blue Running Shoes", ImmutableMap.of("size", Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                "color", Arrays.asList("neon", "blue"),
                "brand", Arrays.asList("nike")));
                put("Nike Navy Running Shoes", ImmutableMap.of("size", Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                "color", Arrays.asList("blue"),
                "brand", Arrays.asList("nike")));
                put("Asics Neon Orange Running Shoes", ImmutableMap.of("size", Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                "color", Arrays.asList("neon"),
                "brand", Arrays.asList("asics")));
                put("New Balance Neon Pink Running Shoes", ImmutableMap.of("size", Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                "color", Arrays.asList("neon"),
                "brand", Arrays.asList("new balance")));
                put("Asics Pink and Purple Running Shoes", ImmutableMap.of("size", Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                "color", Arrays.asList("pink", "purple"),
                "brand", Arrays.asList("asics")));
        }};
}
