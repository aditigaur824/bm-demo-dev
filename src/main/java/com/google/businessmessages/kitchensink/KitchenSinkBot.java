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
package com.google.businessmessages.kitchensink;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.google.api.services.businessmessages.v1.model.BusinessMessagesContentInfo;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesDialAction;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesEvent;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesMedia;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesMessage;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesOpenUrlAction;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesRepresentative;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesRichCard;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesStandaloneCard;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestedAction;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestedReply;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestion;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSurvey;
import com.google.appengine.api.datastore.Entity;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.common.collect.UnmodifiableIterator;
import com.google.communications.businessmessages.v1.CardWidth;
import com.google.communications.businessmessages.v1.EventType;
import com.google.communications.businessmessages.v1.MediaHeight;
import com.google.communications.businessmessages.v1.RepresentativeType;

/**
 * Main bot logic. Most messages are passed through the routing function to map the user's response
 * to business logic to generate a message in return.
 */
public class KitchenSinkBot {

  private static final Logger logger = Logger.getLogger(KitchenSinkBot.class.getName());

  private static final String EXCEPTION_WAS_THROWN = "exception";

  // Object to maintain OAuth2 credentials to call the BM API
  private GoogleCredential credential;

  // Reference to the BM api builder
  private Businessmessages.Builder builder;

  // The current representative
  private BusinessMessagesRepresentative representative;

  // The datastore service used to persist user data
  private static final DataManager dataManager = new DataManager();

  //Store inventory object
  private Inventory storeInventory;

  //Map to track user's cart
  private Map<String, BusinessMessagesCardContent> cartContent;

  public KitchenSinkBot(BusinessMessagesRepresentative representative) {
    this.representative = representative;
    this.storeInventory = new MockInventory(BotConstants.INVENTORY_IMAGES);
    this.cartContent = new HashMap<>();
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
    initializeCart(conversationId);

    //begin parsing message
    String normalizedMessage = message.toLowerCase().trim();

    if (normalizedMessage.equals(BotConstants.CMD_LOREM_IPSUM)) {
      sendResponse(BotConstants.RSP_LOREM_IPSUM, conversationId);
    } else if (normalizedMessage.equals(BotConstants.CMD_MEDIUM_TEXT)) {
      sendResponse(BotConstants.RSP_MEDIUM_TEXT, conversationId);
    } else if (normalizedMessage.equals(BotConstants.CMD_LONG_TEXT)) {
      sendResponse(BotConstants.RSP_LONG_TEXT, conversationId);
    } else if (normalizedMessage.matches(BotConstants.CMD_SPEAK)) {
      attemptTranslation(normalizedMessage, conversationId);
    } else if (normalizedMessage.matches(BotConstants.CMD_LINK)) {
      sendLinkAction(conversationId);
    } else if (normalizedMessage.matches(BotConstants.CMD_DIAL)) {
      sendDialAction(conversationId);
    } else if (normalizedMessage.matches(BotConstants.CMD_CARD)) {
      sendRichCard(conversationId);
    } else if (normalizedMessage.matches(BotConstants.CMD_CAROURSEL)) {
      sendCarouselRichCard(conversationId);
    } else if (normalizedMessage.matches(BotConstants.CMD_WHO)) {
      sendResponse(BotConstants.RSP_WHO_TEXT, conversationId);
    } else if (normalizedMessage.matches(BotConstants.CMD_CSAT_TRIGGER)) {
      showCSAT(conversationId);
    } else if (normalizedMessage.matches(BotConstants.HELP_COMMAND)) {
      sendResponse(BotConstants.RSP_HELP_TEXT, conversationId);
    } else if (normalizedMessage.matches(BotConstants.HOURS_COMMAND)) {
      sendResponse(BotConstants.RSP_HOURS_TEXT, conversationId);
    } else if (normalizedMessage.matches(BotConstants.SHOP_COMMAND)) {
      sendInventoryCarousel(conversationId);
    } else if (normalizedMessage.matches(BotConstants.VIEW_CART_COMMAND)) {
      if (cartContent.size() > 1) sendCartCarousel(conversationId);
      else sendSingleCartItem(conversationId);
    } else if (normalizedMessage.startsWith(BotConstants.ADD_ITEM_COMMAND)) {
      addItemToCart(normalizedMessage, conversationId);
    } else if (normalizedMessage.startsWith(BotConstants.DELETE_ITEM_COMMAND)) {
      deleteItemFromCart(normalizedMessage, conversationId);
    } else {  // Echo received message
      sendResponse(BotConstants.RSP_DEFAULT, conversationId);
    }
  }

  /**
   * Adds specified item to the user's cart.
   * @param message The message that contains which item to add to the cart.
   * @param conversationId The unique id that maps from the agent to the user.
   */
  public void addItemToCart(String message, String conversationId) {
    String itemTitle = capitalizeItemTitle(message.substring("add-cart-".length()));
    dataManager.addItemToCart(conversationId, itemTitle, logger);
    initializeCart(conversationId);
    sendResponse(itemTitle + " have been added to your cart.", conversationId);
  }

  /**
   * Deletes specified item from the user's cart.
   * @param message The message that contains which item to delete from the cart.
   * @param conversationId The unique id that maps from the agent to the user.
   */
  public void deleteItemFromCart(String message, String conversationId) {
    String itemTitle = capitalizeItemTitle(message.substring("del-cart-".length()));
    dataManager.deleteItemFromCart(conversationId, itemTitle, logger);
    initializeCart(conversationId);
    sendResponse(itemTitle + " have been deleted from your cart.", conversationId);
  }

  /**
   * Fixes item title capitalization since messages are lowercase when received.
   * @param itemTitle The item title that must be properly capitalized.
   * @return The properly capitalized title.
   */
  public static String capitalizeItemTitle(String itemTitle) {
    char[] title = itemTitle.toCharArray();
    boolean found = true;
    for (int i = 0; i < title.length; i++) {
      if (Character.isWhitespace(title[i])) found = true;
      else if (found) {
        title[i] = Character.toUpperCase(title[i]);
        found = false;
      }
    }
    return String.valueOf(title);
  }

  /**
   * Initializes the user's cart with persisted data.
   * @param conversationId The unique id that maps from the agent to the user.
   */
  public void initializeCart(String conversationId) {
    cartContent = new HashMap<>();
    List<Entity> cartItems = dataManager.getCartFromData(conversationId);
    if (cartItems.isEmpty()) {
      return;
    }
    for (Entity ent : cartItems) {
      int count = ((Long)ent.getProperty("count")).intValue();
      String itemTitle = (String)ent.getProperty("item_title");
      BusinessMessagesCardContent newCard = new BusinessMessagesCardContent()
      .setTitle(itemTitle)
      .setDescription("Quantity: " + count)
      .setSuggestions(getCartSuggestions(itemTitle))
      .setMedia(new BusinessMessagesMedia()
          .setHeight(MediaHeight.MEDIUM.toString())
          .setContentInfo(new BusinessMessagesContentInfo()
              .setFileUrl(BotConstants.INVENTORY_IMAGES.get(itemTitle))
              .setForceRefresh(true)));
      cartContent.put(itemTitle, newCard);
    }
  }

  /**
   * Transfers the chat to a live agent. Creating the representative as a HUMAN will show a
   * tombstone to the user indicating that they have been transferred to a live agent.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  public void transferToLiveAgent(String conversationId) {
    transferToAnAgent(conversationId, new BusinessMessagesRepresentative()
        .setRepresentativeType(RepresentativeType.HUMAN.toString())
        .setDisplayName(BotConstants.LIVE_AGENT_NAME));
  }

  /**
   * Transfers the chat back to a bot.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  public void transferToBot(String conversationId) {
    try {
      BusinessMessagesEvent event =
          new BusinessMessagesEvent()
              .setEventType(EventType.REPRESENTATIVE_LEFT.toString())
              .setRepresentative(
                  new BusinessMessagesRepresentative()
                      .setRepresentativeType(RepresentativeType.HUMAN.toString())
                      .setDisplayName(BotConstants.LIVE_AGENT_NAME));

      Businessmessages.Conversations.Events.Create request
          = builder.build().conversations().events()
          .create("conversations/" + conversationId, event);

      request.setEventId(UUID.randomUUID().toString());

      request.execute();

      transferToAnAgent(conversationId, new BusinessMessagesRepresentative()
          .setRepresentativeType(RepresentativeType.BOT.toString())
          .setDisplayName(BotConstants.BOT_AGENT_NAME));

      sendResponse(BotConstants.RSP_BOT_TRANSFER, conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }

  /**
   * Generic method to transfer in a representative into the conversation.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   * @param representative The representative joining the conversation.
   */
  private void transferToAnAgent(String conversationId,
      BusinessMessagesRepresentative representative) {
    try {
      BusinessMessagesEvent event =
          new BusinessMessagesEvent()
              .setEventType(EventType.REPRESENTATIVE_JOINED.toString())
              .setRepresentative(representative);

      Businessmessages.Conversations.Events.Create request
          = builder.build().conversations().events()
          .create("conversations/" + conversationId, event);

      request.setEventId(UUID.randomUUID().toString());

      request.execute();

      sendResponse(BotConstants.RSP_LIVE_AGENT_TRANSFER, conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }

  /**
   * Sends the user a CSAT survey.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void showCSAT(String conversationId) {
    try {
      Businessmessages.Conversations.Surveys.Create request
          = builder.build().conversations().surveys()
          .create("conversations/" + conversationId,
              new BusinessMessagesSurvey());

      request.setSurveyId(UUID.randomUUID().toString());

      request.execute();
    } catch (Exception e) {
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }

  /**
   * Sends a text message along with an open url action to the user.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendLinkAction(String conversationId) {
    try {
      List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

      // Add the open url action
      suggestions.add(new BusinessMessagesSuggestion()
          .setAction(new BusinessMessagesSuggestedAction()
              .setOpenUrlAction(
                  new BusinessMessagesOpenUrlAction()
                      .setUrl("https://www.google.com"))
              .setText("Open Google").setPostbackData("open_url")));

      suggestions.addAll(getDefaultMenu());

      // Send the text message and suggestions to the user
      // Use a fallback text of the actual URL
      sendResponse(new BusinessMessagesMessage()
          .setMessageId(UUID.randomUUID().toString())
          .setText(BotConstants.RSP_LINK_TEXT)
          .setRepresentative(representative)
          .setFallback(BotConstants.RSP_LINK_TEXT + " https://www.google.com")
          .setSuggestions(suggestions), conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }

  /**
   * Sends a text message along with a dial action to the user.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendDialAction(String conversationId) {
    try {
      List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

      // Add the dial action
      suggestions.add(new BusinessMessagesSuggestion()
          .setAction(new BusinessMessagesSuggestedAction()
              .setDialAction(
                  new BusinessMessagesDialAction()
                      .setPhoneNumber("+12223334444"))
              .setText("Call example").setPostbackData("call_example")));

      suggestions.add(getHelpMenuItem());

      // Send the text message and suggestions to the user
      sendResponse(new BusinessMessagesMessage()
          .setMessageId(UUID.randomUUID().toString())
          .setText(BotConstants.RSP_LINK_TEXT)
          .setRepresentative(representative)
          .setSuggestions(suggestions), conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }

  /**
   * Sends a sample rich card to the user.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendRichCard(String conversationId) {
    try {
      List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();
      suggestions.add(getHelpMenuItem());

      BusinessMessagesStandaloneCard standaloneCard = getSampleCard();
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
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }

  /**
   * Used when the user's cart contains only one item.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendSingleCartItem(String conversationId) {
    try {
      List<BusinessMessagesSuggestion> suggestions = getDefaultMenu();

      BusinessMessagesStandaloneCard standaloneCard = getCartCard();
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
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }

  /**
   * Creates a sample standalone rich card.
   *
   * @return A standalone rich card.
   */
  private BusinessMessagesStandaloneCard getSampleCard() {
    return new BusinessMessagesStandaloneCard()
        .setCardContent(
            new BusinessMessagesCardContent()
                .setTitle("Business Messages!!!")
                .setDescription("The future of business communication")
                .setSuggestions(getCardSuggestions())
                .setMedia(new BusinessMessagesMedia()
                    .setHeight(MediaHeight.MEDIUM.toString())
                    .setContentInfo(
                        new BusinessMessagesContentInfo()
                            .setFileUrl(BotConstants.SAMPLE_IMAGES[0])
                    ))
        );
  }

  /**
   * Creates a single cart card.
   *
   * @return A standalone cart item card.
   */
  private BusinessMessagesStandaloneCard getCartCard() {
    BusinessMessagesCardContent card = null;
    for (Map.Entry<String, BusinessMessagesCardContent> ent : cartContent.entrySet()) {
      card = ent.getValue();
    }

    return new BusinessMessagesStandaloneCard().setCardContent(card);
  }

  /**
   * Sends a sample carousel rich card to the user.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendCarouselRichCard(String conversationId) {
    try {
      List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();
      suggestions.add(getHelpMenuItem());

      BusinessMessagesCarouselCard carouselCard = getSampleCarousel();

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
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }

  /**
   * Sends the inventory rich card carousel to the user.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendInventoryCarousel(String conversationId) {
    try {
      List<BusinessMessagesSuggestion> suggestions = getDefaultMenu();

      BusinessMessagesCarouselCard carouselCard = getShopCarousel();

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
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }

  /**
   * Sends the cart rich card carousel to the user.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendCartCarousel(String conversationId) {
    try {
      List<BusinessMessagesSuggestion> suggestions = getDefaultMenu();

      BusinessMessagesCarouselCard carouselCard = getCartCarousel();

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
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }

  /**
   * Creates a sample carousel rich card.
   *
   * @return A carousel rich card.
   */
  private BusinessMessagesCarouselCard getSampleCarousel() {
    List<BusinessMessagesCardContent> cardContents = new ArrayList<>();

    // Create individual cards for the carousel
    for (int i = 0; i < BotConstants.SAMPLE_IMAGES.length; i++) {
      cardContents.add(new BusinessMessagesCardContent()
          .setTitle("Card #" + (i + 1))
          .setDescription("What do you think?")
          .setSuggestions(getCardSuggestions())
          .setMedia(new BusinessMessagesMedia()
              .setHeight(MediaHeight.MEDIUM.toString())
              .setContentInfo(new BusinessMessagesContentInfo()
                  .setFileUrl(BotConstants.SAMPLE_IMAGES[i]))));
    }

    return new BusinessMessagesCarouselCard()
        .setCardContents(cardContents)
        .setCardWidth(CardWidth.MEDIUM.toString());
  }

  /**
   * Creates a rich card carousel out of items in business inventory.
   *
   * @return A carousel rich card.
   */
  private BusinessMessagesCarouselCard getShopCarousel() {
    List<BusinessMessagesCardContent> cardContents = new ArrayList<>();

    UnmodifiableIterator<InventoryItem> iterator = storeInventory.getInventory().iterator();
    while(iterator.hasNext()) {
      InventoryItem currentItem = iterator.next();
      cardContents.add(new BusinessMessagesCardContent()
        .setTitle(currentItem.getInventoryItemTitle())
        .setSuggestions(getInventorySuggestions(currentItem.getInventoryItemTitle()))
        .setMedia(new BusinessMessagesMedia()
          .setHeight(MediaHeight.MEDIUM.toString())
          .setContentInfo(new BusinessMessagesContentInfo()
            .setFileUrl(currentItem.getInventoryItemURL())
            .setForceRefresh(true))));
    }

    return new BusinessMessagesCarouselCard()
        .setCardContents(cardContents)
        .setCardWidth(CardWidth.MEDIUM.toString());
  }

  /**
   * Creates a rich card carousel out of items in the user's cart.
   *
   * @return A carousel rich card.
   */
  private BusinessMessagesCarouselCard getCartCarousel() {
    List<BusinessMessagesCardContent> cardContents = new ArrayList<>();

    // Create individual cards for the carousel using the inventory
    for (Map.Entry<String, BusinessMessagesCardContent> ent : cartContent.entrySet()) {
      cardContents.add(ent.getValue());
    }

    return new BusinessMessagesCarouselCard()
        .setCardContents(cardContents)
        .setCardWidth(CardWidth.MEDIUM.toString());
  }

  /**
   * Suggestions to add to sample cards.
   *
   * @return List of suggestions.
   */
  private List<BusinessMessagesSuggestion> getCardSuggestions() {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    suggestions.add(
        new BusinessMessagesSuggestion()
            .setReply(new BusinessMessagesSuggestedReply()
                .setText("\uD83D\uDC4D Like").setPostbackData("like-item")));

    suggestions.add(
        new BusinessMessagesSuggestion()
            .setReply(new BusinessMessagesSuggestedReply()
                .setText("\uD83D\uDC4E Dislike").setPostbackData("dislike-item")));

    return suggestions;
  }

  /**
   * Suggestions to add to inventory cards.
   * @param itemTitle The title of the item that the suggestions will pertain to.
   * @return List of suggestions.
   */
  private List<BusinessMessagesSuggestion> getInventorySuggestions(String itemTitle) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    suggestions.add(
        new BusinessMessagesSuggestion()
            .setReply(new BusinessMessagesSuggestedReply()
                .setText("\uD83D\uDED2 Add to Cart").setPostbackData("add-cart-" + itemTitle)));

    return suggestions;
  }

   /**
   * Suggestions to add to cart cards.
   * @param itemTitle The title of the item that the suggestions will pertain to.
   * @return List of suggestions.
   */
  private List<BusinessMessagesSuggestion> getCartSuggestions(String itemTitle) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    suggestions.add(
        new BusinessMessagesSuggestion()
            .setReply(new BusinessMessagesSuggestedReply()
                .setText("\u2795").setPostbackData("add-cart-" + itemTitle)));

    suggestions.add(
        new BusinessMessagesSuggestion()
            .setReply(new BusinessMessagesSuggestedReply()
                .setText("\u2796").setPostbackData("del-cart-" + itemTitle)));

    return suggestions;
  }

  /**
   * The normalizedMessage should be formatted as "speak french", "speak chinese", etc. the
   * specified langauge is parsed and mapped to a supported language. If no supported language is
   * found, an error response is shown.
   *
   * @param normalizedMessage The inbound request from the user.
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void attemptTranslation(String normalizedMessage, String conversationId) {
    String language = normalizedMessage.replace("speak ", "").trim();

    // Trim any extra text
    if (language.indexOf(" ") > 0) {
      language = language.substring(0, language.indexOf(" "));
    }

    logger.info("Trying to translate to language: " + language);

    // Attempt to match input language to language map
    if (BotConstants.LANGUAGE_MAP.containsKey(language)) {
      String languageCode = BotConstants.LANGUAGE_MAP.get(language);

      Translate translate = TranslateOptions.getDefaultInstance().getService();
      Translation translation =
          translate.translate(
              BotConstants.RSP_TO_TRANSLATION,
              Translate.TranslateOption.sourceLanguage("en"),
              Translate.TranslateOption.targetLanguage(languageCode),
              Translate.TranslateOption.format("text"),
              Translate.TranslateOption.model("base"));

      sendResponse(translation.getTranslatedText(), conversationId);
    } else { // No matching language found, show default response
      String noLanguageMatch = "Sorry, but " + language + " is not a supported language.\n\n" +
          "Here is the list of supported languages: ";

      StringBuilder sb = new StringBuilder();
      for (String languageName : BotConstants.LANGUAGE_MAP.keySet()) {
        if (sb.length() != 0) {
          sb.append(", ");
        }
        // Upper case the first letter of the language
        sb.append(languageName.substring(0, 1).toUpperCase());
        sb.append(languageName.substring(1));
      }

      noLanguageMatch += sb.toString();

      sendResponse(noLanguageMatch, conversationId);
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
          .setSuggestions(getDefaultMenu()), conversationId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }

  /**
   * Posts a message to the Business Messages API, first sending a typing indicator event and
   * sending a stop typing event after the message has been sent.
   *
   * @param message The message object to send the user.
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendResponse(BusinessMessagesMessage message,
      String conversationId) {
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
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }

  private String getAgentDisplayName(RepresentativeType representativeType) {
    return representativeType == RepresentativeType.BOT
        ? BotConstants.BOT_AGENT_NAME : BotConstants.LIVE_AGENT_NAME;
  }

  /**
   * Creates the default menu items for responses.
   *
   * @return List of suggestions to form a menu.
   */
  private List<BusinessMessagesSuggestion> getDefaultMenu() {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();
    suggestions.add(getHelpMenuItem());

    suggestions.add(new BusinessMessagesSuggestion()
        .setReply(new BusinessMessagesSuggestedReply()
            .setText("Inquire About Hours").setPostbackData("hours")
        ));
    
    if (cartContent.size() != 0) {
      suggestions.add(new BusinessMessagesSuggestion()
        .setReply(new BusinessMessagesSuggestedReply()
            .setText("Continue Shopping").setPostbackData("shop")
        ));

      suggestions.add(new BusinessMessagesSuggestion()
        .setReply(new BusinessMessagesSuggestedReply()
            .setText("View Cart").setPostbackData("cart")
        ));
    } else {
      suggestions.add(new BusinessMessagesSuggestion()
        .setReply(new BusinessMessagesSuggestedReply()
            .setText("Shop Our Collection").setPostbackData("shop")
        ));
    }

    if (representative.getRepresentativeType().equals(RepresentativeType.HUMAN.toString())) {
      suggestions.add(new BusinessMessagesSuggestion()
          .setReply(new BusinessMessagesSuggestedReply()
              .setText("Back to bot").setPostbackData("back_to_bot")
          ));
    }

    return suggestions;
  }

  /**
   * Get the help menu suggested reply.
   *
   * @return A help suggested reply.
   */
  private BusinessMessagesSuggestion getHelpMenuItem() {
    return new BusinessMessagesSuggestion()
        .setReply(new BusinessMessagesSuggestedReply()
            .setText("Help").setPostbackData("help")
        );
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
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
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
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }
}
