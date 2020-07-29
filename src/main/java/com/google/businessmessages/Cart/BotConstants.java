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
package com.google.businessmessages.Cart;

import java.util.HashMap;
import java.util.Map;

public interface BotConstants {

    String CREDENTIALS_FILE_NAME = "bm-agent-service-account-credentials.json";

    // the URL for the API endpoint
    String BM_API_URL = "https://businessmessages.googleapis.com/";

    String LIVE_AGENT_NAME = "Sally";
    String BOT_AGENT_NAME = "BM Cart Bot";

    // List of recognized commands to produce certain responses
    String BACK_TO_BOT_COMMAND = "back_to_bot";
    String DELETE_ITEM_COMMAND = "del-cart";
    String ADD_ITEM_COMMAND = "add-cart";
    String VIEW_CART_COMMAND = "cart";
    String HOURS_COMMAND = "hours";
    String SHOP_COMMAND = "shop";
    String HELP_COMMAND = "^help.*|^commands\\s.*|see the help menu";

    // List of pre-programmed responses
    String RSP_DEFAULT = "Sorry, I didn't quite get that. Perhaps you were looking for one of these options?";

    String RSP_HOURS_TEXT = "We are open Monday - Friday from 9 A.M. to 5 P.M.";

    String RSP_HELP_TEXT = "Welcome to the help menu! This program will echo " +
        "any text that you enter that is not part of a supported command. The supported " +
        "commands are: \n\n" +
        "Help - Shows the list of supported commands and functions\n\n" +
        "Inquire About Hours - Will respond with the times that our store is open.\n\n" +
        "Shop Our Collection/Continue Shopping - Will respond with a collection of mock inventory items.\n\n" +
        "View Cart - Will respond with all of the items in your cart.\n\n";

    String RSP_LIVE_AGENT_TRANSFER = "Hey there, you are now chatting with a live agent " +
        "(not really, but let's pretend).";

    String RSP_BOT_TRANSFER = "Hey there, you are now chatting with a bot.";
    
    Map<String, String> INVENTORY_IMAGES = new HashMap<String, String>() {{
        put("Blue Running Shoes", "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/blue_running_shoes.jpeg");
        put("Neon Running Shoes", "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/neon_running_shoes.jpg");
        put("Pink Running Shoes", "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/pink_running_shoes.jpeg");
        put("Teal Running Shoes", "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/teal_running_shoes.jpg");
        put("White Running Shoes", "https://storage.googleapis.com/rbm-boot-camp-15.appspot.com/bot_assets/white_running_shoes.jpg");
    }};

}