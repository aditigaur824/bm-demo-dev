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

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.businessmessages.v1.Businessmessages;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesCardContent;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesCarouselCard;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesEvent;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesMessage;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesRepresentative;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesRichCard;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesStandaloneCard;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestion;
import com.google.communications.businessmessages.v1.EventType;
import com.google.communications.businessmessages.v1.RepresentativeType;

/**
 * Main bot logic. Most messages are passed through the routing function to map the user's response
 * to business logic to generate a message in return.
 */
public class CartBot {

  private static final Logger logger = Logger.getLogger(CartBot.class.getName());

  // Object to maintain OAuth2 credentials to call the BM API
  private GoogleCredential credential;

  // Reference to the BM api builder
  private Businessmessages.Builder builder;

  // The current representative
  private BusinessMessagesRepresentative representative;

  //Store inventory object
  private final Inventory storeInventory;

  //User's cart
  private Cart userCart;

  public CartBot(BusinessMessagesRepresentative representative) {
    this.representative = representative;
    this.storeInventory = new MockInventory(BotConstants.INVENTORY_IMAGES, BotConstants.INVENTORY_PROPERTIES);
    initBmApi();
  }

  public void setUserCart(String conversationId) {
    this.userCart = CartManager.getOrCreateCart(conversationId);
  }

  /**
   * Routes the message to produce a response based on the incoming message if it matches an
   * existing supported command. Otherwise, the inbound message is echoed back to the user.
   *
   * @param message The received message from a user.
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  public void routeMessage(String message, String conversationId) {
    //initialize user's cart
    this.setUserCart(conversationId);

    //begin parsing message
    String normalizedMessage = message.toLowerCase().trim();

   if (normalizedMessage.matches(BotConstants.HELP_COMMAND)) {
      sendResponse(BotConstants.HELP_RESPONSE_TEXT, conversationId);
    } else if (normalizedMessage.matches(BotConstants.HOURS_COMMAND)) {
      sendResponse(BotConstants.HOURS_RESPONSE_TEXT, conversationId);
    } else if (normalizedMessage.startsWith(BotConstants.INIT_FILTER_COMMAND)) {
      sendFilterSelections(normalizedMessage, conversationId);
    } else if (normalizedMessage.matches(BotConstants.SHOP_COMMAND)) {
      sendInventoryCarousel(conversationId);
    } else if (normalizedMessage.matches(BotConstants.VIEW_CART_COMMAND)) {
      if (userCart.getItems().size() > 1) sendCartCarousel(conversationId);
      else sendSingleCartItem(conversationId);
    } else if (normalizedMessage.startsWith(BotConstants.ADD_ITEM_COMMAND)) {
      addItemToCart(normalizedMessage, conversationId);
    } else if (normalizedMessage.startsWith(BotConstants.DELETE_ITEM_COMMAND)) {
      deleteItemFromCart(normalizedMessage, conversationId);
    } else if (normalizedMessage.matches(BotConstants.SEE_FILTERS_COMMAND)) {
      sendFilterCarousel(conversationId);
    } else if (normalizedMessage.startsWith(BotConstants.SEE_FILTER_OPTIONS_COMMAND)) {
      sendFilterOptions(normalizedMessage, conversationId);
    } else if (normalizedMessage.startsWith(BotConstants.SET_FILTER_COMMAND)) {
      setFilter(normalizedMessage, conversationId);
    } else if (normalizedMessage.startsWith(BotConstants.REMOVE_FILTER_COMMAND)) {
      removeFilter(normalizedMessage, conversationId);
    } else if (normalizedMessage.startsWith(BotConstants.SCHEDULE_PICKUP_COMMAND)) {
      sendSchedulePickup(message.trim(), conversationId);
    } else if (normalizedMessage.startsWith(BotConstants.CANCEL_PICKUP_COMMAND)) {
      sendCancelPickupResponse(normalizedMessage, conversationId);
    } else if (normalizedMessage.matches(BotConstants.VIEW_PICKUP_COMMAND)) {
      sendPickupCarousel(conversationId);
    } else if (normalizedMessage.startsWith(BotConstants.GCAL_LINK_COMMAND)) {
      setPickupCalendarAdded(normalizedMessage, conversationId);
    } else if (normalizedMessage.startsWith(BotConstants.CHECK_IN_COMMAND)) {
      sendCheckinResponse(normalizedMessage, conversationId);
    } else if (normalizedMessage.startsWith(BotConstants.CHOOSE_PARKING_COMMAND)) {
      sendChooseParkingResponse(normalizedMessage, conversationId);
    } else {
      sendResponse(BotConstants.DEFAULT_RESPONSE_TEXT, conversationId);
    }
  }

  /**
   * Sends a response to the user when the user checks in for a scheduled pickup.
   * Prompts the user to select the parking spot that they are in so the associate knows
   * where to bring their order. 
   * @param normalizedMessage The message containing the order id that the user is picking up.
   * @param conversationId The unique id mapping between the user and the agent.
   */
  private void sendCheckinResponse(String normalizedMessage, String conversationId) {
    String orderId = normalizedMessage.substring(BotConstants.CHECK_IN_COMMAND.length());
    PickupManager.updatePickupProperties(conversationId, orderId, BotConstants.PICKUP_STATUS, Pickup.Status.CHECKED_IN);
    try {
      sendResponse(new BusinessMessagesMessage()
          .setMessageId(UUID.randomUUID().toString())
          .setText(BotConstants.CHECK_IN_RESPONSE_TEXT)
          .setRepresentative(representative)
          .setFallback(BotConstants.CHECK_IN_RESPONSE_TEXT)
          .setSuggestions(UIManager.getParkingSpotSuggestions()), conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while sending response.", e);
    }
  }

  /**
   * Sends a response to the user confirming that they have chosen their parking spot
   * and that their associate is on their way with their order. Approximates a wait time.
   * @param normalizedMessage The message containing the parking spot the user has checked-in with.
   * @param conversationId The unique id mapping between the user and the agent.
   */
  private void sendChooseParkingResponse(String normalizedMessage, String conversationId) {
    try {
      String parkingSpot = normalizedMessage.substring(BotConstants.CHOOSE_PARKING_COMMAND.length());
      sendResponse(new BusinessMessagesMessage()
          .setMessageId(UUID.randomUUID().toString())
          .setText(String.format(BotConstants.PARKING_SPOT_RESPONSE_TEXT, parkingSpot))
          .setRepresentative(representative)
          .setFallback(String.format(BotConstants.PARKING_SPOT_RESPONSE_TEXT, parkingSpot))
          .setSuggestions(UIManager.getCheckinSuggestions()), conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while sending response.", e);
    }
  }

  /**
   * Initiates the schedule pickup workflow. Initially will only have the command and order id 
   * appended to the callback. This will indicate that nothing else about the pickup has been set
   * yet. Then, the workflow will prompt the user to select one characteristic of their pickup at a 
   * time. The workflow will complete when all fields of the pickup have been set by the user.
   * @param normalizedMessage The incoming callback from the user.
   * @param conversationId The unique id mapping between the agent and the user.
   */
  private void sendSchedulePickup(String normalizedMessage, String conversationId) {
    String[] orderIdAndPayload = normalizedMessage.substring(BotConstants.SCHEDULE_PICKUP_COMMAND.length())
                                                  .split("-", 2);
    if (orderIdAndPayload.length == 0) {
      sendResponse(BotConstants.DEFAULT_RESPONSE_TEXT, conversationId);
      return;
    }
    String orderId = orderIdAndPayload[0]; 
    if (orderIdAndPayload.length == 1) {
      //initial part of the workflow
      PickupManager.addPickup(conversationId, orderId);
      sendTextResponse(BotConstants.PICKUP_CHOOSE_STORE_ADDRESS_TEXT, conversationId);
      sendStoreAddressCarousel(conversationId, orderId);
    } else {
      String payload = orderIdAndPayload[1];
      if (payload.startsWith(BotConstants.PICKUP_STORE_ADDRESS)) {
        String storeName = payload.substring(BotConstants.PICKUP_STORE_ADDRESS.length());
        PickupManager.updatePickupProperties(conversationId, orderId, BotConstants.PICKUP_STORE_ADDRESS, storeName);
        sendTextResponse(BotConstants.PICKUP_CHOOSE_TIME_TEXT, conversationId);
        sendPickupTimesCarousel(conversationId, orderId);
      } else if (payload.startsWith(BotConstants.PICKUP_DATE)) {
        int storeTimeZone = BotConstants.STORE_NAME_TO_TIME_ZONE_OFFSET
          .get(PickupManager.getPickup(conversationId, orderId)
          .getStoreAddress());
        String dateString = payload.substring(BotConstants.PICKUP_DATE.length());
        PickupManager.updatePickupProperties(conversationId, orderId, 
          BotConstants.PICKUP_DATE, 
          PickupManager.createPickupDate(storeTimeZone, dateString));
        PickupManager.updatePickupProperties(conversationId, orderId, 
          BotConstants.PICKUP_STATUS,
          Pickup.Status.SCHEDULED);
        Pickup currentPickup = PickupManager.getPickup(conversationId, orderId);
        sendTextResponse(BotConstants.PICKUP_SCHEDULE_COMPLETED_TEXT, conversationId);
        sendPickupConfirmation(conversationId, currentPickup);
      }
    }
  }

  /**
   * Sends the user confirmation that their pickup has been cancelled. 
   * @param normalizedMessage The callback message from the user containing the orderId for
   * which the associated pickup needs to be removed.
   * @param conversationId The unique id mapping between the agent and the user.
   */
  private void sendCancelPickupResponse(String normalizedMessage, String conversationId) {
    String orderId = normalizedMessage.substring(BotConstants.CANCEL_PICKUP_COMMAND.length());
    PickupManager.cancelPickup(conversationId, orderId);
    sendResponse(BotConstants.PICKUP_CANCELED_TEXT, conversationId);
  }

  /**
   * Sets the specified pickup's calendar added field so the user is not prompted to add the event
   * to their gcal after they have already done so.
   * @param normalizedMessage The message that contains the pickup for which the order has been added.
   * @param conversationId The unique id mapping between the agent and the user.
   */
  public void setPickupCalendarAdded(String normalizedMessage, String conversationId) {
    String orderId = normalizedMessage.substring(BotConstants.GCAL_LINK_COMMAND.length());
    PickupManager.updatePickupProperties(conversationId, orderId, 
      BotConstants.PICKUP_ADDED_CALENDAR, BotConstants.PICKUP_ADDED_CALENDAR_TRUE);
  }

  /**
   * Adds specified item to the user's cart.
   * 
   * @param message The message that contains which item to add to the cart.
   * @param conversationId The unique id that maps from the agent to the user.
   */
  public void addItemToCart(String message, String conversationId) {
    String itemId = message.substring(BotConstants.ADD_ITEM_COMMAND.length());
    try {
      InventoryItem itemToAdd = storeInventory.getItem(itemId).get();
      this.userCart = CartManager.addItem(this.userCart.getId(), itemToAdd.getId(), itemToAdd.getTitle());
      sendResponse(itemToAdd.getTitle() + " have been added to your cart.", conversationId);
    } catch (NoSuchElementException e) {
      logger.log(Level.SEVERE, "Attempted to add item not in inventory.", e);
    }
  }

  /**
   * Deletes specified item from the user's cart.
   * @param message The message that contains which item to delete from the cart.
   * @param conversationId The unique id that maps from the agent to the user.
   */
  public void deleteItemFromCart(String message, String conversationId) {
    String itemId = message.substring(BotConstants.DELETE_ITEM_COMMAND.length());
    try {
      InventoryItem itemToDelete = storeInventory.getItem(itemId).get();
      this.userCart = CartManager.deleteItem(this.userCart.getId(), itemToDelete.getId());
      sendResponse(itemToDelete.getTitle() + " have been deleted from your cart.", conversationId);
    } catch (NoSuchElementException e) {
      logger.log(Level.SEVERE, "Attempted to delete item not in inventory.", e);
    }
  }

  /**
   * Sets the filter value the user specifies.
   * @param message The message that contains which filter and value to set.
   * @param conversationId The unique id that maps from the agent to the user.
   */
  public void setFilter(String message, String conversationId) {
    String filterNameAndValue;
    if (message.startsWith(BotConstants.INIT_FILTER_COMMAND)) {
      filterNameAndValue = message.substring(BotConstants.INIT_FILTER_COMMAND.length());
    } else {
      filterNameAndValue = message.substring(BotConstants.SET_FILTER_COMMAND.length());
    }
    String filterName;
    String filterValue;
    if (filterNameAndValue.startsWith(BotConstants.COLOR_FILTER_NAME)) {
      filterName = BotConstants.COLOR_FILTER_NAME;
    } else if (filterNameAndValue.startsWith(BotConstants.BRAND_FILTER_NAME)) {
      filterName = BotConstants.BRAND_FILTER_NAME;
    } else if (filterNameAndValue.startsWith(BotConstants.SIZE_FILTER_NAME)) {
      filterName = BotConstants.SIZE_FILTER_NAME;
    } else {
      logger.log(Level.SEVERE, "Attempted to set invalid filter. The entered filter name and value: " + filterNameAndValue);
      return;
    }
    filterValue = filterNameAndValue.substring(filterName.length() + 1);
    FilterManager.setFilter(conversationId, filterName, filterValue);
    if (message.startsWith(BotConstants.SET_FILTER_COMMAND)) {
      sendResponse(String.format(BotConstants.SET_FILTER_RESPONSE_TEXT, filterName, filterValue), conversationId);
    }
  }

  /**
   * Removes the filter the user specifies.
   * @param message The message that contains which filter to remove.
   * @param conversationId The unique id that maps from the agent to the user.
   */
  public void removeFilter(String message, String conversationId) {
    String filterNameAndValue = message.substring(BotConstants.REMOVE_FILTER_COMMAND.length());
    String filterName;
    if (filterNameAndValue.startsWith(BotConstants.COLOR_FILTER_NAME)) {
      filterName = BotConstants.COLOR_FILTER_NAME;
    } else if (filterNameAndValue.startsWith(BotConstants.BRAND_FILTER_NAME)) {
      filterName = BotConstants.BRAND_FILTER_NAME;
    } else {
      logger.log(Level.SEVERE, "Attempted to remove invalid filter. The entered filter name and value: " + filterNameAndValue);
      return;
    }
    FilterManager.removeFilter(conversationId, filterName);
    sendResponse(String.format(BotConstants.REMOVE_FILTER_RESPONSE_TEXT, filterName), conversationId);
  }

  /**
   * Sends a single cart card when the user's cart contains only one item.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendSingleCartItem(String conversationId) {
    try {
      List<BusinessMessagesSuggestion> suggestions = UIManager.getDefaultMenu(conversationId, this.userCart);

      BusinessMessagesStandaloneCard standaloneCard = UIManager.getCartCard(this.storeInventory, this.userCart);
      String fallbackText = standaloneCard.getCardContent().getTitle() + "\n\n"
          + standaloneCard.getCardContent().getDescription() + "\n\n"
          + standaloneCard.getCardContent().getMedia().getContentInfo().getFileUrl();

      // Send the rich card message and suggestions to the user
      sendResponse(new BusinessMessagesMessage()
          .setMessageId(UUID.randomUUID().toString())
          .setRichCard(new BusinessMessagesRichCard()
              .setStandaloneCard(standaloneCard))
          .setRepresentative(representative)
          .setFallback(fallbackText)
          .setSuggestions(suggestions), conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while sending single cart card.", e);
    }
  }

  /**
   * Sends a rich card with the user's pickup details as well as the option to cancel their 
   * pickup, or add it to their gcal. 
   * @param conversationId The unique id mapping between the user and the agent.
   * @param pickup The pickup the user just finished scheduling.
   */
  private void sendPickupConfirmation(String conversationId, Pickup pickup) {
    try {
      List<BusinessMessagesSuggestion> suggestions = UIManager.getDefaultMenu(conversationId, this.userCart);

      BusinessMessagesStandaloneCard standaloneCard = UIManager.getPickupCard(pickup);
      String fallbackText = standaloneCard.getCardContent().getTitle() + "\n\n"
          + standaloneCard.getCardContent().getDescription() + "\n\n"
          + standaloneCard.getCardContent().getMedia().getContentInfo().getFileUrl();

      // Send the rich card message and suggestions to the user
      sendResponse(new BusinessMessagesMessage()
          .setMessageId(UUID.randomUUID().toString())
          .setRichCard(new BusinessMessagesRichCard()
              .setStandaloneCard(standaloneCard))
          .setRepresentative(representative)
          .setFallback(fallbackText)
          .setSuggestions(suggestions), conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while sending single cart card.", e);
    }
  }

  /**
   * Sends a rich card carousel with all of the user's scheduled pickups so that 
   * the user can see the store name and time they have scheduled various pickups 
   * for.
   * @param conversationId The unique id mapping between the user and the agent.
   */
  private void sendPickupCarousel(String conversationId) {
    try{
      List<BusinessMessagesSuggestion> suggestions = UIManager.getDefaultMenu(conversationId, this.userCart);
      List<Pickup> pickups = PickupManager.getAllPickups(conversationId);

      if (pickups.size() == 1) {
        BusinessMessagesStandaloneCard standaloneCard = UIManager.getPickupCard(pickups.get(0));
        String fallbackText = standaloneCard.getCardContent().getTitle() + "\n\n"
            + standaloneCard.getCardContent().getDescription() + "\n\n"
            + standaloneCard.getCardContent().getMedia().getContentInfo().getFileUrl();

        // Send the rich card message and suggestions to the user
        sendResponse(new BusinessMessagesMessage()
            .setMessageId(UUID.randomUUID().toString())
            .setRichCard(new BusinessMessagesRichCard()
                .setStandaloneCard(standaloneCard))
            .setRepresentative(representative)
            .setFallback(fallbackText)
            .setSuggestions(suggestions), conversationId);
      } else {
        BusinessMessagesCarouselCard carouselCard = UIManager.getPickupCarousel(PickupManager.getAllPickups(conversationId));

        StringBuilder fallbackTextBuilder = new StringBuilder();
        for (BusinessMessagesCardContent cardContent : carouselCard.getCardContents()) {
          fallbackTextBuilder.append(cardContent.getTitle() + "\n\n");
          fallbackTextBuilder.append(cardContent.getDescription() + "\n\n");
          fallbackTextBuilder.append(("---------------------------------------------\n\n"));
        }
  
        // Send the text that indicates what the subsequent carousel consists of
        sendTextResponse(BotConstants.VIEW_PICKUPS_RESPONSE_TEXT, conversationId);
        // Send the carousel card message and suggestions to the user
        sendResponse(new BusinessMessagesMessage()
          .setMessageId(UUID.randomUUID().toString())
          .setRichCard(new BusinessMessagesRichCard()
              .setCarouselCard(carouselCard))
          .setRepresentative(representative)
          .setFallback(fallbackTextBuilder.toString())
          .setSuggestions(suggestions), conversationId);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while sending pickup carousel.", e);
    }
  }

  /**
   * Sends the options for the filter specified in the user's message.
   * @param message The postback message received from the user.
   * @param conversationId The unique id mapping between the agent and the user.
   */
  private void sendFilterOptions(String message, String conversationId) {
    try {
      String filterName = message.substring(BotConstants.SEE_FILTER_OPTIONS_COMMAND.length());
      String responseText = "Here are your options to filter by " + filterName + ".";

      sendResponse(new BusinessMessagesMessage()
          .setMessageId(UUID.randomUUID().toString())
          .setText(responseText)
          .setRepresentative(representative)
          .setFallback(responseText)
          .setSuggestions(UIManager.getFilterSuggestions(filterName)), conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while sending filter options.", e);
    }
  }

  /**
   * Sends the filter rich card carousel to the user.
   * @param conversationId The unique id mapping between the user and the agent.
   */
  private void sendFilterCarousel(String conversationId) {
    try{
      List<BusinessMessagesSuggestion> suggestions = UIManager.getDefaultMenu(conversationId, this.userCart);

      BusinessMessagesCarouselCard carouselCard = UIManager.getFilterCarousel(conversationId);

      StringBuilder fallbackTextBuilder = new StringBuilder();
      for (BusinessMessagesCardContent cardContent : carouselCard.getCardContents()) {
        fallbackTextBuilder.append(cardContent.getTitle() + "\n\n");
        fallbackTextBuilder.append(cardContent.getDescription() + "\n\n");
        fallbackTextBuilder.append(("---------------------------------------------\n\n"));
      }

      // Send the text that indicates what the subsequent carousel consists of
      sendTextResponse(BotConstants.CURRENT_FILTERS_RESPONSE_TEXT, conversationId);
      // Send the carousel card message and suggestions to the user
      sendResponse(new BusinessMessagesMessage()
        .setMessageId(UUID.randomUUID().toString())
        .setRichCard(new BusinessMessagesRichCard()
            .setCarouselCard(carouselCard))
        .setRepresentative(representative)
        .setFallback(fallbackTextBuilder.toString())
        .setSuggestions(suggestions), conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while sending filter carousel.", e);
    }
  }

  /**
   * Regulates the preference selection workflow when the user begins shopping with
   * the bot. This function enters this workflow from the shop command (sendInventoryCarousel function) and
   * leaves this workflow when all filters have been set. This function is responsible for sending prompts
   * and getting responses that pertain to initial filter selection.
   * @param message The message either containing the filter to set, or indicating that this
   * function has been called from the sendInventoryCarousel function.
   * @param conversationId The unique id mapping between user and agent.
   */
  private void sendFilterSelections(String message, String conversationId) {
    if (!message.equalsIgnoreCase(BotConstants.FROM_INVENTORY_CALLBACK)) {
      setFilter(message, conversationId);
    }
    try {
      String filterName;
      String filterResponseText;
      if (FilterManager.getFilter(conversationId, BotConstants.SIZE_FILTER_NAME) == null) {
        filterName = BotConstants.SIZE_FILTER_NAME;
        filterResponseText = BotConstants.SIZE_FILTER_RESPONSE_TEXT;
      } else if (FilterManager.getFilter(conversationId, BotConstants.BRAND_FILTER_NAME) == null) {
        filterName = BotConstants.BRAND_FILTER_NAME;
        filterResponseText = BotConstants.BRAND_FILTER_RESPONSE_TEXT;
      } else if (FilterManager.getFilter(conversationId, BotConstants.COLOR_FILTER_NAME) == null) {
        filterName = BotConstants.COLOR_FILTER_NAME;
        filterResponseText = BotConstants.COLOR_FILTER_RESPONSE_TEXT;
      } else {
        sendTextResponse(BotConstants.FILTER_SELECTION_COMPLETE_RESPONSE_TEXT, conversationId);
        sendInventoryCarousel(conversationId);
        return;
      }
      sendResponse(new BusinessMessagesMessage()
        .setMessageId(UUID.randomUUID().toString())
        .setText(filterResponseText)
        .setRepresentative(representative)
        .setFallback(filterResponseText)
        .setSuggestions(UIManager.getInitFilterSuggestions(filterName)), conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while initializing filters.", e);
    }
  }

  /**
   * Sends the inventory rich card carousel to the user if the user has already 
   * chosen their item preferences. If they have not, prompts user to select item
   * preferences (filters).
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendInventoryCarousel(String conversationId) {
    if (FilterManager.getAllFilters(conversationId).size() < BotConstants.NUM_SUPPORTED_FILTERS) {
      sendFilterSelections(BotConstants.FROM_INVENTORY_CALLBACK, conversationId);
      return;
    }

    List<Filter> filtersMinusAll = FilterManager.getAllFilters(conversationId).stream()
        .filter(x -> !x.getValue().equals("all")).collect(Collectors.toList());
    List<InventoryItem> validItems = storeInventory.getInventory().asList()
      .stream()
      .filter(item -> {
        return filtersMinusAll.stream().allMatch(f -> {
          Optional<ItemProperty> matchingProp = item.getProperties().stream().filter(prop -> prop.getName().equals(f.getName())).findFirst();
          return matchingProp.map(prop -> prop.getOptions().contains(f.getValue())).orElse(false);
        });
      })
      .collect(Collectors.toList());

    try {
      List<BusinessMessagesSuggestion> suggestions = UIManager.getDefaultMenu(conversationId, this.userCart);

      if (validItems.size() == 0) {
        sendResponse(BotConstants.NO_INVENTORY_RESULTS_RESPONSE_TEXT, conversationId);
      } else if (validItems.size() == 1) {
        BusinessMessagesStandaloneCard standaloneCard = UIManager.getShopCard(validItems);
        String fallbackText = standaloneCard.getCardContent().getTitle() + "\n\n"
            + standaloneCard.getCardContent().getDescription() + "\n\n"
            + standaloneCard.getCardContent().getMedia().getContentInfo().getFileUrl();

        // Send the rich card message and suggestions to the user
        sendResponse(new BusinessMessagesMessage()
            .setMessageId(UUID.randomUUID().toString())
            .setRichCard(new BusinessMessagesRichCard()
                .setStandaloneCard(standaloneCard))
            .setRepresentative(representative)
            .setFallback(fallbackText)
            .setSuggestions(suggestions), conversationId);
      } else {
        BusinessMessagesCarouselCard carouselCard = UIManager.getShopCarousel(validItems);

        StringBuilder fallbackTextBuilder = new StringBuilder();
        for (BusinessMessagesCardContent cardContent : carouselCard.getCardContents()) {
          fallbackTextBuilder.append(cardContent.getTitle() + "\n\n");
          fallbackTextBuilder.append(cardContent.getDescription() + "\n\n");
          fallbackTextBuilder.append(cardContent.getMedia().getContentInfo().getFileUrl() + "\n");
          fallbackTextBuilder.append(("---------------------------------------------\n\n"));
        }
  
        // Send the carousel card message and suggestions to the user
        sendResponse(new BusinessMessagesMessage()
            .setMessageId(UUID.randomUUID().toString())
            .setRichCard(new BusinessMessagesRichCard()
                .setCarouselCard(carouselCard))
            .setRepresentative(representative)
            .setFallback(fallbackTextBuilder.toString())
            .setSuggestions(suggestions), conversationId);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while sending inventory carousel.", e);
    }
  }

  /**
   * Sends the cart rich card carousel to the user.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendCartCarousel(String conversationId) {
    try {
      List<BusinessMessagesSuggestion> suggestions = UIManager.getDefaultMenu(conversationId, this.userCart);

      BusinessMessagesCarouselCard carouselCard = UIManager.getCartCarousel(this.storeInventory, this.userCart);

      StringBuilder fallbackTextBuilder = new StringBuilder();
      for (BusinessMessagesCardContent cardContent : carouselCard.getCardContents()) {
        fallbackTextBuilder.append(cardContent.getTitle() + "\n\n");
        fallbackTextBuilder.append(cardContent.getDescription() + "\n\n");
        fallbackTextBuilder.append(cardContent.getMedia().getContentInfo().getFileUrl() + "\n");
        fallbackTextBuilder.append(("---------------------------------------------\n\n"));
      }

      // Send the carousel card message and suggestions to the user
      sendResponse(new BusinessMessagesMessage()
          .setMessageId(UUID.randomUUID().toString())
          .setRichCard(new BusinessMessagesRichCard()
              .setCarouselCard(carouselCard))
          .setRepresentative(representative)
          .setFallback(fallbackTextBuilder.toString())
          .setSuggestions(suggestions), conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while sending cart carousel.", e);
    }
  }

  /**
   * Sends store location rich cards to the user. The user can choose one of these locations
   * for a scheduled pickup.
   *
   * @param conversationId The unique id that maps between the user and agent.
   * @param orderId The order for which the store pickup is being built.
   */
  private void sendStoreAddressCarousel(String conversationId, String orderId) {
    try {
      List<BusinessMessagesSuggestion> suggestions = UIManager.getCancelPickupSuggestion(orderId);

      BusinessMessagesCarouselCard carouselCard = UIManager.getStoreAddressCarousel(orderId);

      StringBuilder fallbackTextBuilder = new StringBuilder();
      for (BusinessMessagesCardContent cardContent : carouselCard.getCardContents()) {
        fallbackTextBuilder.append(cardContent.getTitle() + "\n\n");
        fallbackTextBuilder.append(cardContent.getDescription() + "\n\n");
        fallbackTextBuilder.append(cardContent.getMedia().getContentInfo().getFileUrl() + "\n");
        fallbackTextBuilder.append(("---------------------------------------------\n\n"));
      }

      // Send the carousel card message and suggestions to the user
      sendResponse(new BusinessMessagesMessage()
          .setMessageId(UUID.randomUUID().toString())
          .setRichCard(new BusinessMessagesRichCard()
              .setCarouselCard(carouselCard))
          .setRepresentative(representative)
          .setFallback(fallbackTextBuilder.toString())
          .setSuggestions(suggestions), conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while sending store address carousel.", e);
    }
  }

  /**
   * Sends pickup time rich cards to the user. The user can choose one of these dates/times 
   * for a scheduled pickup.
   * @param conversationId The unique id that maps between the user and agent.
   * @param orderId The order for which the store pickup is being built.
   */
  private void sendPickupTimesCarousel(String conversationId, String orderId) {
    try {
      List<BusinessMessagesSuggestion> suggestions = UIManager.getCancelPickupSuggestion(orderId);

      BusinessMessagesCarouselCard carouselCard = UIManager.getPickupTimesCarousel(orderId);

      StringBuilder fallbackTextBuilder = new StringBuilder();
      for (BusinessMessagesCardContent cardContent : carouselCard.getCardContents()) {
        fallbackTextBuilder.append(cardContent.getTitle() + "\n\n");
        fallbackTextBuilder.append(cardContent.getDescription() + "\n\n");
        fallbackTextBuilder.append(cardContent.getMedia().getContentInfo().getFileUrl() + "\n");
        fallbackTextBuilder.append(("---------------------------------------------\n\n"));
      }

      // Send the carousel card message and suggestions to the user
      sendResponse(new BusinessMessagesMessage()
          .setMessageId(UUID.randomUUID().toString())
          .setRichCard(new BusinessMessagesRichCard()
              .setCarouselCard(carouselCard))
          .setRepresentative(representative)
          .setFallback(fallbackTextBuilder.toString())
          .setSuggestions(suggestions), conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while sending pickup time carousel.", e);
    }
  }

  /**
   * Posts a message to the Business Messages API that only contains text.
   *
   * @param message The message text to send the user.
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendTextResponse(String message, String conversationId) {
    try {
      // Send plaintext message with default menu to user
      sendResponse(new BusinessMessagesMessage()
          .setMessageId(UUID.randomUUID().toString())
          .setText(message)
          .setRepresentative(representative)
          .setFallback(message), conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while sending response.", e);
    }
  }

  /**
   * Posts a message to the Business Messages API.
   *
   * @param message The message text to send the user.
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  public void sendResponse(String message, String conversationId) {
    try {
      // Send plaintext message with default menu to user
      sendResponse(new BusinessMessagesMessage()
          .setMessageId(UUID.randomUUID().toString())
          .setText(message)
          .setRepresentative(representative)
          .setFallback(message)
          .setSuggestions(UIManager.getDefaultMenu(conversationId, this.userCart)), conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while sending response.", e);
    }
  }

  /**
   * Posts a message to the Business Messages API, first sending a typing indicator event and
   * sending a stop typing event after the message has been sent.
   *
   * @param message The message object to send the user.
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendResponse(BusinessMessagesMessage message, String conversationId) {
    try {
      // Send typing indicator
      BusinessMessagesEvent event =
          new BusinessMessagesEvent()
              .setEventType(EventType.TYPING_STARTED.toString());

      Businessmessages.Conversations.Events.Create request
          = builder.build().conversations().events()
          .create("conversations/" + conversationId, event);

      request.setEventId(UUID.randomUUID().toString());
      request.execute();

      logger.info("message id: " + message.getMessageId());
      logger.info("message body: " + message.toPrettyString());

      // Send the message
      Businessmessages.Conversations.Messages.Create messageRequest
          = builder.build().conversations().messages()
          .create("conversations/" + conversationId, message);

      messageRequest.execute();

      // Stop typing indicator
      event =
          new BusinessMessagesEvent()
              .setEventType(EventType.TYPING_STOPPED.toString());

      request
          = builder.build().conversations().events()
          .create("conversations/" + conversationId, event);

      request.setEventId(UUID.randomUUID().toString());
      request.execute();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while sending response.", e);
    }
  }

  private String getAgentDisplayName(RepresentativeType representativeType) {
    return representativeType == RepresentativeType.BOT
        ? BotConstants.BOT_AGENT_NAME : BotConstants.LIVE_AGENT_NAME;
  }

  /**
   * Initializes credentials used by the RBM API.
   *
   * @param credentialsFileLocation The location for the GCP service account file.
   */
  private void initCredentials(String credentialsFileLocation) {
    logger.info("Initializing credentials for BM.");

    try {
      ClassLoader classLoader = getClass().getClassLoader();
      File file = new File(classLoader.getResource(credentialsFileLocation).getFile());

      this.credential = GoogleCredential
          .fromStream(new FileInputStream(file));

      this.credential = credential.createScoped(Arrays.asList(
          "https://www.googleapis.com/auth/businessmessages"));

      this.credential.refreshToken();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while initializing credentials.", e);
    }
  }

  /**
   * Initializes the BM API object.
   */
  private void initBmApi() {
    if (this.credential == null) {
      initCredentials(BotConstants.CREDENTIALS_FILE_NAME);
    }

    try {
      HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

      // create instance of the BM API
      builder = new Businessmessages
          .Builder(httpTransport, jsonFactory, null)
          .setApplicationName(credential.getServiceAccountProjectId());

      // set the API credentials and endpoint
      builder.setHttpRequestInitializer(credential);
      builder.setRootUrl(BotConstants.BM_API_URL);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception thrown while initializing BM API.", e);
    }
  }
}
