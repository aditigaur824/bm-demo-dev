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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableList;
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
        int NUM_SUPPORTED_FILTERS = 3;
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
        String SCHEDULE_PICKUP_TEXT = "Schedule Pickup";
        String VIEW_PICKUPS_TEXT = "View Pickups";
        String ADD_TO_CAL_TEXT = "Add to Calendar";
        String CHOOSE_STORE_ADDRESS_TEXT = "Choose this Store";
        String CANCEL_TEXT = "Cancel";

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
        String SCHEDULE_PICKUP_COMMAND = "schedule-pickup-";
        String CANCEL_PICKUP_COMMAND = "cancel-pickup-";
        String VIEW_PICKUP_COMMAND = "view-pickup";
        String GCAL_LINK_COMMAND = "open-cal-url-";
        String HELP_COMMAND = "^help.*|^commands\\s.*|see the help menu";

        //List of pickup properties for callbacks
        String PICKUP_STATUS = "pickup-status";
        String PICKUP_STORE_ADDRESS = "store-address";
        String PICKUP_DATE = "pickup-date";
        String PICKUP_ADDED_CALENDAR = "pickup-cal";
        
        //Property value when pickup has been added to the calendar
        String PICKUP_ADDED_CALENDAR_TRUE = "added";

        //Postback data string formatters
        String INIT_FILTER_POSTBACK = INIT_FILTER_COMMAND + "%s-%s";
        String REMOVE_FILTER_POSTBACK = REMOVE_FILTER_COMMAND + "%s";
        String SET_FILTER_POSTBACK = SET_FILTER_COMMAND + "%s-%s";
        String SEE_FILTER_OPTIONS_POSTBACK = SEE_FILTER_OPTIONS_COMMAND + "%s";
        String ADD_ITEM_POSTBACK = ADD_ITEM_COMMAND + "%s";
        String DELETE_ITEM_POSTBACK = DELETE_ITEM_COMMAND + "%s";
        String SCHEDULE_PICKUP_POSTBACK = SCHEDULE_PICKUP_COMMAND + "%s";
        String CHOOSE_STORE_ADDRESS_POSTBACK = SCHEDULE_PICKUP_COMMAND + "%s-"
                + PICKUP_STORE_ADDRESS + "%s";
        String CHOOSE_PICKUP_TIME_POSTBACK = SCHEDULE_PICKUP_COMMAND + "%s-"
                + PICKUP_DATE + "%s-%s";
        String CANCEL_PICKUP_POSTBACK = CANCEL_PICKUP_COMMAND + "%s";
        String GCAL_LINK_POSTBACK = GCAL_LINK_COMMAND + "%s";

        //Pickup Card Constants
        String PICKUP_IMAGE = "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/pickup_art.png";
        String PICKUP_TITLE = "Pickup for Order: %s";
        String GCAL_LINK_TEMPLATE = "https://www.google.com/calendar/render?action=TEMPLATE&text=Your+G-Shoes+Pick+Up&dates="
        + "%s/%s" 
        + "&details=For+location+details,+link+here:+%s";

        // List of pre-programmed responses
        String DEFAULT_RESPONSE_TEXT =
                "Sorry, I didn't quite get that. Perhaps you were looking for one of these options?";

        String HOURS_RESPONSE_TEXT = "We are open Monday - Friday from 9 A.M. to 5 P.M.";

        String PICKUP_CHOOSE_STORE_ADDRESS_TEXT = "Way to go! 🙌 We will have this pickup scheduled in no time!\n\n"
                + "First, let's choose a pickup location.";

        String PICKUP_CHOOSE_TIME_TEXT = "I've got that store down!\n\n" 
                + "Next, let's choose a time for your pickup!";

        String PICKUP_SCHEDULE_COMPLETED_TEXT = "Nice! I've got your pickup scheduled! 😊 \n\n"
                + "I hope the details below look okay! \n\n"
                + "On the day of your pick up, check back with me, and I'll guide you through "
                + "checking in with your store!";

        String PICKUP_CANCELED_TEXT = "No worries! I've canceled this pickup.\n\n"
                + "If you change your mind, you can always click schedule pickup again!";

        String HELP_RESPONSE_TEXT = "Welcome to the help menu! This program will echo "
                + "any text that you enter that is not part of a supported command. The supported "
                + "commands are: \n\n" + "Help - Shows the list of supported commands and functions\n\n"
                + "Inquire About Hours - Will respond with the times that our store is open.\n\n"
                + "Shop Our Collection/Continue Shopping - Will respond with a collection of mock inventory items.\n\n"
                + "View/Edit Filters - Will respond with a collection of filters and their set values.\n\n"
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
        
        
        //Data pertaining to store locations
        ImmutableMap<String, String> STORE_NAME_TO_ADDRESS = ImmutableMap.copyOf(new HashMap<String, String>() {{
                put("G-Shoes Mountain View", 
                        "1600 Amphitheatre Pkwy, Mountain View, CA 94043");
                put("G-Shoes Kirkland", 
                        "747 6th St South, Kirkland, WA 98033");
                put("G-Shoes New York", 
                        "85 10th Ave, New York, NY 10011");
        }});

        ImmutableMap<String, String> STORE_NAME_TO_LOCATION = ImmutableMap.copyOf(new HashMap<String, String>() {{
                put("G-Shoes Mountain View", 
                        "https://maps.googleapis.com/maps/api/staticmap?center=37.422128,-122.084045&zoom=12&size=250x250&markers=color:red%7C37.422128,-122.084045&key=AIzaSyDtbmtNywHovIOr_XU7AEDAe6OAruCsWO4");
                put("G-Shoes Kirkland", 
                        "https://maps.googleapis.com/maps/api/staticmap?center=47.669940,-122.197099&zoom=12&size=250x250&markers=color:red%7C47.669940,-122.197099&key=AIzaSyDtbmtNywHovIOr_XU7AEDAe6OAruCsWO4");
                put("G-Shoes New York", 
                        "https://maps.googleapis.com/maps/api/staticmap?center=40.743545,-74.007939&zoom=12&size=250x250&markers=color:red%7C40.743545,-74.007939&key=AIzaSyDtbmtNywHovIOr_XU7AEDAe6OAruCsWO4");
        }});

        ImmutableMap<String, String> STORE_NAME_TO_MAPS_LINK = ImmutableMap.copyOf(new HashMap<String, String>() {{
                put("G-Shoes Mountain View", 
                        "https://www.google.com/maps/place/Googleplex/@37.4219999,-122.0862462,17z/data=!3m1!4b1!4m5!3m4!1s0x808fba02425dad8f:0x6c296c66619367e0!8m2!3d37.4219999!4d-122.0840575");
                put("G-Shoes Kirkland", 
                        "https://www.google.com/maps/place/Google+Building+C/@47.669846,-122.1996099,17z/data=!3m1!4b1!4m5!3m4!1s0x549012dae8b1164f:0x94ca4b8d5cc5fb58!8m2!3d47.669846!4d-122.1974212");
                put("G-Shoes New York", 
                        "https://www.google.com/maps/place/Google+NYC:+8510+Building/@40.7420814,-74.0072099,17z/data=!4m8!1m2!2m1!1sgoogl+new+york!3m4!1s0x89c259c0b6279809:0xf0f85f5d47fed64c!8m2!3d40.7434001!4d-74.0079724");
        }});

        ImmutableMap<String, Integer> STORE_NAME_TO_TIME_ZONE_OFFSET = ImmutableMap.copyOf(new HashMap<String, Integer>() {{
                put("G-Shoes Mountain View", 
                        7);
                put("G-Shoes Kirkland", 
                        7);
                put("G-Shoes New York", 
                        4);
        }});

        //Data pertaining to pickup time slots
        int TIME_SLOT_DURATION = 2;

        String CALENDAR_IMAGE = "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/calendar_art.png";

        ImmutableMap<String, String> PICKUP_DATES = ImmutableMap.copyOf(new LinkedHashMap<String, String>() {{
                put("Saturday, Sept. 5",
                        "9/5");
                put("Monday, Sept. 7",
                        "9/7");
                put("Tuesday, Sept. 8",
                        "9/8");
        }});

        ImmutableMap<String, String> PICKUP_TIMES = ImmutableMap.copyOf(new LinkedHashMap<String, String>() {{
                put("8 A.M. - 10 A.M.",
                        "8-10");
                put("12 P.M. - 2 P.M.",
                        "12-14");
                put("3 P.M. - 5 P.M.",
                        "15-17");
        }});
        String SET_FILTER_RESPONSE_TEXT = "Thanks! Your %s filter has been set to %s.";
        
        String REMOVE_FILTER_RESPONSE_TEXT = "Your %s filter has been removed.";

        String CURRENT_FILTERS_RESPONSE_TEXT = "Here are your current filters: ";

        String NO_INVENTORY_RESULTS_RESPONSE_TEXT = "Sorry, we don't have any items that matched your filters.";

        // Data pertaining to filters and inventory items.
        String COLOR_CARD_IMAGE = "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/color_card_image.jpeg";
        ImmutableList<String> COLOR_LIST = ImmutableList.of("All", "Blue", "Neon", "Pink", "White", "Purple", "Black");

        String BRAND_CARD_IMAGE = "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/brand_collage.jpg";
        ImmutableList<String> BRAND_LIST = ImmutableList.of("All", "Adidas", "Nike", "New Balance", "Asics");

        String SIZE_CARD_IMAGE = "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/size_card_image.jpg";
        ImmutableList<String> SIZE_LIST = ImmutableList.of("5", "6", "7", "8", "9", "10", "11", "12");

        ImmutableMap<String, String> INVENTORY_IMAGES = ImmutableMap.copyOf(new HashMap<String, String>() {{
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
        }});

        ImmutableMap<String, Map<String, List<String>>> INVENTORY_PROPERTIES = ImmutableMap.copyOf(new HashMap<String, Map<String, List<String>>>() {{
                put("Asics Blue Running Shoes", 
                        ImmutableMap.of(
                                "size", 
                                Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                                "color", 
                                Arrays.asList("blue"),
                                "brand", 
                                Arrays.asList("asics")));
                put("Adidas Neon Running Shoes", 
                        ImmutableMap.of(
                                "size", 
                                Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                                "color", 
                                Arrays.asList("neon"),
                                "brand", 
                                Arrays.asList("adidas")));
                put("Asics Pink Running Shoes", 
                        ImmutableMap.of(
                                "size", 
                                Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                                "color", 
                                Arrays.asList("pink"),
                                "brand", 
                                Arrays.asList("asics")));
                put("Asics Purple Running Shoes", 
                        ImmutableMap.of(
                                "size", 
                                Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                                "color", 
                                Arrays.asList("purple", "pink"),
                                "brand", 
                                Arrays.asList("asics")));
                put("Asics White Running Shoes", 
                        ImmutableMap.of(
                                "size", 
                                Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                                "color", 
                                Arrays.asList("blue", "white", "purple"),
                                "brand", 
                                Arrays.asList("asics")));
                put("Nike Neon and Grey Running Shoes", 
                        ImmutableMap.of(
                                "size", 
                                Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                                "color", 
                                Arrays.asList("neon", "grey"),
                                "brand", 
                                Arrays.asList("nike")));
                put("New Balance Black Running Shoes", 
                        ImmutableMap.of(
                                "size", 
                                Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                                "color", 
                                Arrays.asList("black"),
                                "brand", 
                                Arrays.asList("new balance")));
                put("New Balance White Running Shoes", 
                        ImmutableMap.of(
                                "size", 
                                Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                                "color", 
                                Arrays.asList("white"),
                                "brand", 
                                Arrays.asList("new balance")));
                put("Nike Neon Blue Running Shoes", 
                        ImmutableMap.of(
                                "size", 
                                Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                                "color", 
                                Arrays.asList("neon", "blue"),
                                "brand", 
                                Arrays.asList("nike")));
                put("Nike Navy Running Shoes", 
                        ImmutableMap.of(
                                "size", 
                                Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                                "color", 
                                Arrays.asList("blue"),
                                "brand", 
                                Arrays.asList("nike")));
                put("Asics Neon Orange Running Shoes", 
                        ImmutableMap.of(
                                "size", 
                                Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                                "color", 
                                Arrays.asList("neon"),
                                "brand", 
                                Arrays.asList("asics")));
                put("New Balance Neon Pink Running Shoes", 
                        ImmutableMap.of(
                                "size", 
                                Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                                "color", 
                                Arrays.asList("neon"),
                                "brand", 
                                Arrays.asList("new balance")));
                put("Asics Pink and Purple Running Shoes", 
                        ImmutableMap.of(
                                "size", 
                                Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12"),
                                "color", 
                                Arrays.asList("pink", "purple"),
                                "brand", 
                                Arrays.asList("asics")));
        }});
}
