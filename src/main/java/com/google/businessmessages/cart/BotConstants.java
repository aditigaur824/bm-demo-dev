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

import java.util.ArrayList;
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

    // List of suggestion strings
    String FILTERS_TEXT = "See/Edit My Filters";
    String VIEW_CART_TEXT = "View Cart";
    String CONTINUE_SHOPPING_TEXT = "Continue Shopping";
    String SHOP_TEXT = "Shop Our Collection";
    String HOURS_TEXT = "Inquire About Hours";
    String HELP_TEXT = "Help";
    String ADD_ITEM_TEXT = "\uD83D\uDED2 Add to Cart";
    String INCREMENT_COUNT_TEXT = "\u2795";
    String DECREMENT_COUNT_TEXT = "\u2796";

    // List of recognized commands to produce certain responses
    String SEE_FILTERS_COMMAND = "see-filters";
    String SET_FILTER_COMMAND = "set-filter-";
    String REMOVE_FILTER_COMMAND = "remove-filter-";
    String DELETE_ITEM_COMMAND = "del-cart-";
    String ADD_ITEM_COMMAND = "add-cart-";
    String VIEW_CART_COMMAND = "cart";
    String HOURS_COMMAND = "hours";
    String SHOP_COMMAND = "shop";
    String HELP_COMMAND = "^help.*|^commands\\s.*|see the help menu";

    // List of pre-programmed responses
    String RSP_DEFAULT =
            "Sorry, I didn't quite get that. Perhaps you were looking for one of these options?";

    String RSP_HOURS_TEXT = "We are open Monday - Friday from 9 A.M. to 5 P.M.";

    String RSP_HELP_TEXT = "Welcome to the help menu! This program will echo "
            + "any text that you enter that is not part of a supported command. The supported "
            + "commands are: \n\n" + "Help - Shows the list of supported commands and functions\n\n"
            + "Inquire About Hours - Will respond with the times that our store is open.\n\n"
            + "Shop Our Collection/Continue Shopping - Will respond with a collection of mock inventory items.\n\n"
            + "View Cart - Will respond with all of the items in your cart.\n\n";

    Map<String, String> INVENTORY_IMAGES = new HashMap<String, String>() {{
            put("Blue Running Shoes",
                    "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/blue_running_shoes.jpeg");
            put("Neon Running Shoes",
                    "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/neon_running_shoes.jpg");
            put("Pink Running Shoes",
                    "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/pink_running_shoes.jpeg");
            put("Teal Running Shoes",
                    "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/teal_running_shoes.jpg");
            put("White Running Shoes",
                    "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/white_running_shoes.jpg");
    }};

    List<String> colorList = Arrays.asList("Blue", "Neon", "Pink");
    List<String> brandList = Arrays.asList("Adidas", "Nike", "New Balance");

    Map<String, Map<String, List<String>>> INVENTORY_PROPERTIES = new HashMap<String, Map<String, List<String>>>() {{
        put("Blue Running Shoes", ImmutableMap.of("size", Arrays.asList("7", "8", "9"),
            "color", Arrays.asList("blue"),
            "brand", Arrays.asList("Adidas")));
        put("Neon Running Shoes", ImmutableMap.of("size", Arrays.asList("8", "9"),
            "color", Arrays.asList("neon"),
            "brand", Arrays.asList("Nike")));
        put("Pink Running Shoes", ImmutableMap.of("size", Arrays.asList("6", "7", "9"),
            "color", Arrays.asList("pink"),
            "brand", Arrays.asList("New Balance")));
        put("Teal Running Shoes", ImmutableMap.of("size", Arrays.asList("6", "7"),
            "color", Arrays.asList("teal"),
            "brand", Arrays.asList("New Balance")));
        put("White Running Shoes", ImmutableMap.of("size", Arrays.asList("5", "8", "9"),
            "color", Arrays.asList("white"),
            "brand", Arrays.asList("Adidas")));
    }};
}
