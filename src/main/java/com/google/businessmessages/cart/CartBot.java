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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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

  /**
   * Routes the message to produce a response based on the incoming message if it matches an
   * existing supported command. Otherwise, the inbound message is echoed back to the user.
   *
   * @param message The received message from a user.
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  public void routeMessage(String message, String conversationId) {
    //initialize user's cart
    this.userCart = CartManager.getOrCreateCart(conversationId);

    //begin parsing message
    String normalizedMessage = message.toLowerCase().trim();

   if (normalizedMessage.matches(BotConstants.HELP_COMMAND)) {
      sendResponse(BotConstants.RSP_HELP_TEXT, conversationId);
    } else if (normalizedMessage.matches(BotConstants.HOURS_COMMAND)) {
      sendResponse(BotConstants.RSP_HOURS_TEXT, conversationId);
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
    } else if (normalizedMessage.startsWith(BotConstants.SET_FILTER_COMMAND)) {
      setFilter(normalizedMessage, conversationId);
    } else if (normalizedMessage.startsWith(BotConstants.REMOVE_FILTER_COMMAND)) {
      removeFilter(normalizedMessage, conversationId);
    } else {
      sendResponse(BotConstants.RSP_DEFAULT, conversationId);
    }
  }

  /**
   * Adds specified item to the user's cart.
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
    String filterNameAndValue = message.substring(BotConstants.SET_FILTER_COMMAND.length());
    String filterName;
    String filterValue;
    if (filterNameAndValue.startsWith(BotConstants.COLOR_FILTER_NAME)) {
      filterName = BotConstants.COLOR_FILTER_NAME;
    } else if (filterNameAndValue.startsWith(BotConstants.BRAND_FILTER_NAME)) {
      filterName = BotConstants.BRAND_FILTER_NAME;
    } else {
      logger.log(Level.SEVERE, "Attempted to set invalid filter.");
      return;
    }
    filterValue = filterNameAndValue.substring(filterName.length());
    FilterManager.setFilter(conversationId, filterName, filterValue);
    sendResponse("Your " + filterName + "filter has been set to " + filterValue + ".", conversationId);
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
      logger.log(Level.SEVERE, "Attempted to remove invalid filter.");
      return;
    }
    FilterManager.removeFilter(conversationId, filterName);
    sendResponse("Your " + filterName + "filter has been removed.", conversationId);
  }

  /**
   * Sends a single cart card when the user's cart contains only one item.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendSingleCartItem(String conversationId) {
    try {
      List<BusinessMessagesSuggestion> suggestions = UIManager.getDefaultMenu(this.userCart);

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
   * Sends the filter rich card carousel to the user.
   * @param conversationId The unique id mapping between the user and the agent.
   */
  private void sendFilterCarousel(String conversationId) {
    try{
      List<BusinessMessagesSuggestion> suggestions = UIManager.getDefaultMenu(this.userCart);

      BusinessMessagesCarouselCard carouselCard = UIManager.getFilterCarousel(conversationId);

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
      logger.log(Level.SEVERE, "Exception thrown while sending inventory carousel.", e);
    }
  }

  /**
   * Sends the inventory rich card carousel to the user.
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendInventoryCarousel(String conversationId) {
    try {
      List<BusinessMessagesSuggestion> suggestions = UIManager.getDefaultMenu(this.userCart);

      BusinessMessagesCarouselCard carouselCard = UIManager.getShopCarousel(this.storeInventory);

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
      List<BusinessMessagesSuggestion> suggestions = UIManager.getDefaultMenu(this.userCart);

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
   * Posts a message to the Business Messages API.
   *
   * @param message The message text to send the user.
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendResponse(String message, String conversationId) {
    try {
      // Send plaintext message with default menu to user
      sendResponse(new BusinessMessagesMessage()
          .setMessageId(UUID.randomUUID().toString())
          .setText(message)
          .setRepresentative(representative)
          .setFallback(message)
          .setSuggestions(UIManager.getDefaultMenu(this.userCart)), conversationId);
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
   */
  private void initCredentials() {
    logger.info("Initializing credentials for Business Messages.");

    try {
      this.credential = GoogleCredential.getApplicationDefault();
      this.credential = credential.createScoped(Arrays.asList(
          "https://www.googleapis.com/auth/businessmessages"));

      this.credential.refreshToken();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception was thrown while initializing credentials.", e);
    }
  }

  /**
   * Initializes the BM API object.
   */
  private void initBmApi() {
    if (this.credential == null) {
      initCredentials();
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
