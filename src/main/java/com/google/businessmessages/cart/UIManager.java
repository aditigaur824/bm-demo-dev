package com.google.businessmessages.cart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.services.businessmessages.v1.model.BusinessMessagesCardContent;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesCarouselCard;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesContentInfo;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesMedia;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesOpenUrlAction;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesStandaloneCard;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestedAction;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestedReply;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestion;
import com.google.communications.businessmessages.v1.CardWidth;
import com.google.communications.businessmessages.v1.MediaHeight;

/**
 * Defines methods to create UI elements such as suggestion chips, rich cards, and 
 * rich card carousels to send back to CartBot. 
 */
public class UIManager {
  private static final int MAX_CAROUSEL_LIMIT = 10;
  private static final String COLOR_FILTER_CARD_TITLE = "Color";
  private static final String BRAND_FILTER_CARD_TITLE = "Brand";
  private static final String SIZE_FILTER_CARD_TITLE = "Size";
  private static final Logger logger = Logger.getLogger(Cart.class.getName());

 /**
  * Creates a list of default list of suggestions to accompany a response
  * @param conversationId The unique id mapping between the user and the agent.
  * @param userCart The cart instance associated with the current user.
  * @return List of default suggestions.
  */
  public static List<BusinessMessagesSuggestion> getDefaultMenu(String conversationId, Cart userCart) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    if (!OrderManager.getUnscheduledOrders(conversationId).isEmpty()) {
      suggestions.add(new BusinessMessagesSuggestion()
        .setReply(new BusinessMessagesSuggestedReply()
            .setText(BotConstants.SCHEDULE_PICKUP_TEXT).setPostbackData(String.format(BotConstants.SCHEDULE_PICKUP_POSTBACK,
              OrderManager.getUnscheduledOrders(conversationId).get(0).getId()))
        ));
    }

    if (!userCart.getItems().isEmpty()) {
      suggestions.add(new BusinessMessagesSuggestion()
        .setReply(new BusinessMessagesSuggestedReply()
            .setText(BotConstants.VIEW_CART_TEXT).setPostbackData(BotConstants.VIEW_CART_COMMAND)
        ));

      suggestions.add(new BusinessMessagesSuggestion()
        .setReply(new BusinessMessagesSuggestedReply()
            .setText(BotConstants.CONTINUE_SHOPPING_TEXT).setPostbackData(BotConstants.SHOP_COMMAND)
        ));
    } else {
      suggestions.add(new BusinessMessagesSuggestion()
        .setReply(new BusinessMessagesSuggestedReply()
            .setText(BotConstants.SHOP_TEXT).setPostbackData(BotConstants.SHOP_COMMAND)
        ));
    }
    
    if (!FilterManager.getAllFilters(conversationId).isEmpty()) {
      suggestions.add(new BusinessMessagesSuggestion()
        .setReply(new BusinessMessagesSuggestedReply()
            .setText(BotConstants.FILTERS_TEXT).setPostbackData(BotConstants.SEE_FILTERS_COMMAND)
        ));
    }

    if (!PickupManager.getAllPickups(conversationId).isEmpty()) {
      suggestions.add(new BusinessMessagesSuggestion()
        .setReply(new BusinessMessagesSuggestedReply()
            .setText(BotConstants.VIEW_PICKUPS_TEXT).setPostbackData(BotConstants.VIEW_PICKUP_COMMAND)
        ));
    }

    suggestions.add(new BusinessMessagesSuggestion()
        .setReply(new BusinessMessagesSuggestedReply()
            .setText(BotConstants.HOURS_TEXT).setPostbackData(BotConstants.HOURS_COMMAND)
        ));

    suggestions.add(getHelpMenuItem());

    return suggestions;
   }

   /**
   * Creates suggestions to return when the user is being asked questions to initialize their
   * filters for the first time.
   * @param filterName The name of the filter that the suggestions will pertain to.
   * @return List of suggestions.
   */
  public static List<BusinessMessagesSuggestion> getInitFilterSuggestions(String filterName) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    List<String> filterOptions;
    if (filterName.equals(BotConstants.COLOR_FILTER_NAME)) {
      filterOptions = BotConstants.COLOR_LIST;
    } else if (filterName.equals(BotConstants.BRAND_FILTER_NAME)) {
      filterOptions = BotConstants.BRAND_LIST;
    } else if (filterName.equals(BotConstants.SIZE_FILTER_NAME)) {
      filterOptions = BotConstants.SIZE_LIST;
    } else {
      filterOptions = new ArrayList<>();
    }

    for (String option : filterOptions) {
      suggestions.add(
          new BusinessMessagesSuggestion()
              .setReply(new BusinessMessagesSuggestedReply()
                  .setText(option).setPostbackData(String.format(BotConstants.INIT_FILTER_POSTBACK, 
                    filterName, option))));
    }
    return suggestions;
  }

   /**
   * Creates suggestions to return when the user clicks on change/edit on a particular filter. 
   * @param filterName The name of the filter that the suggestions will pertain to.
   * @return List of suggestions.
   */
  public static List<BusinessMessagesSuggestion> getFilterSuggestions(String filterName) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    List<String> filterOptions;
    if (filterName.equals(BotConstants.COLOR_FILTER_NAME)) {
      filterOptions = BotConstants.COLOR_LIST;
    } else if (filterName.equals(BotConstants.BRAND_FILTER_NAME)) {
      filterOptions = BotConstants.BRAND_LIST;
    } else if (filterName.equals(BotConstants.SIZE_FILTER_NAME)) {
      filterOptions = BotConstants.SIZE_LIST;
    } else {
      filterOptions = new ArrayList<>();
    }

    suggestions.add(
          new BusinessMessagesSuggestion()
              .setReply(new BusinessMessagesSuggestedReply()
                  .setText("Remove").setPostbackData(
                    String.format(BotConstants.REMOVE_FILTER_POSTBACK, filterName))));
    for (String option : filterOptions) {
      suggestions.add(
          new BusinessMessagesSuggestion()
              .setReply(new BusinessMessagesSuggestedReply()
                  .setText(option).setPostbackData(
                    String.format(BotConstants.SET_FILTER_POSTBACK, 
                    filterName, option))));
    }
    return suggestions;
  }

  /**
   * Creates suggestions to add to filter cards. 
   * @param filterName The name of the filter that the suggestions will pertain to.
   * @return List of suggestions.
   */
  public static List<BusinessMessagesSuggestion> getFilterCardSuggestions(String filterName) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    suggestions.add(
          new BusinessMessagesSuggestion()
              .setReply(new BusinessMessagesSuggestedReply()
                  .setText("Remove").setPostbackData(
                    String.format(BotConstants.REMOVE_FILTER_POSTBACK, filterName))));
      suggestions.add(
          new BusinessMessagesSuggestion()
              .setReply(new BusinessMessagesSuggestedReply()
                  .setText(BotConstants.EDIT_FILTER_TEXT).setPostbackData(
                      String.format(BotConstants.SEE_FILTER_OPTIONS_POSTBACK, filterName))));
    return suggestions;
  }

   /**
   * Creates suggestions to add to inventory item cards. 
   * @param itemId The id of the item that the suggestions will pertain to.
   * @return List of suggestions.
   */
  public static List<BusinessMessagesSuggestion> getInventorySuggestions(String itemId) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    suggestions.add(
        new BusinessMessagesSuggestion()
            .setReply(new BusinessMessagesSuggestedReply()
                .setText(BotConstants.ADD_ITEM_TEXT).setPostbackData(
                  String.format(BotConstants.ADD_ITEM_POSTBACK, itemId))));

    return suggestions;
  }

    /**
   * Creates suggestions to add to cart item cards.
   * @param itemId The id of the item that the suggestions will pertain to.
   * @return List of suggestions.
   */
  public static List<BusinessMessagesSuggestion> getCartSuggestions(String itemId) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    suggestions.add(
        new BusinessMessagesSuggestion()
            .setReply(new BusinessMessagesSuggestedReply()
                .setText(BotConstants.INCREMENT_COUNT_TEXT).setPostbackData(
                  String.format(BotConstants.ADD_ITEM_POSTBACK, itemId))));

    suggestions.add(
        new BusinessMessagesSuggestion()
            .setReply(new BusinessMessagesSuggestedReply()
                .setText(BotConstants.DECREMENT_COUNT_TEXT).setPostbackData(
                  String.format(BotConstants.DELETE_ITEM_POSTBACK, itemId))));

    return suggestions;
  }

  /**
   * Creates suggestions for cards hosting store addresses. These suggestions will route to a callback
   * that will allow the user to select a store for pickup. 
   * @param orderId The identifier of the order the callback will refer to.
   * @param storeName The store the user will be choosing for pickup if they click on the suggestion.
   */
  public static List<BusinessMessagesSuggestion> getStoreCardSuggestions(String orderId, String storeName) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    suggestions.add(
        new BusinessMessagesSuggestion()
            .setReply(new BusinessMessagesSuggestedReply()
                .setText(BotConstants.CHOOSE_STORE_ADDRESS_TEXT).setPostbackData(
                  String.format(BotConstants.CHOOSE_STORE_ADDRESS_POSTBACK, orderId, storeName)
                )));
    
    return suggestions;
  }

  /**
   * Creates suggestions for cards hosting pickup dates. These suggestions will route to a callback that 
   * will allow the user to select a date/time for pickup. 
   * @param orderId The identifier of the order the callback will refer to.
   * @param date The date these suggestions are embedded in. 
   */
  public static List<BusinessMessagesSuggestion> getPickupTimeSuggestions(String orderId, String date) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    for (Map.Entry<String, String> time : BotConstants.PICKUP_TIMES.entrySet()) {
      suggestions.add(
        new BusinessMessagesSuggestion()
            .setReply(new BusinessMessagesSuggestedReply()
                .setText(time.getKey()).setPostbackData(
                  String.format(BotConstants.CHOOSE_PICKUP_TIME_POSTBACK, orderId, date, time.getValue())
                )));
    }

    return suggestions;
  }

  /**
   * Creates a cancel pickup suggestion chip. Clicking on this chip will cancel the pickup and remove the instance
   * from the user's data.
   */
  public static List<BusinessMessagesSuggestion> getCancelPickupSuggestion(String orderId) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    suggestions.add(
        new BusinessMessagesSuggestion()
            .setReply(new BusinessMessagesSuggestedReply()
                .setText(BotConstants.CANCEL_TEXT).setPostbackData(
                  String.format(BotConstants.CANCEL_PICKUP_POSTBACK, orderId)
                )));
    
    return suggestions;
  }

  /**
   * Creates suggestions to attach to a pickup confirmation card. Includes relevant 
   * information about the pickup such as the pickup location and time. 
   * Also includes an external link action to add the pickup to the user's google calendar.
   * @param pickup The pickup the confirmation card is for.
   * @return The cancel and add to calendar suggestions for the specified pickup.
   */
  public static List<BusinessMessagesSuggestion> getPickupCardSuggestions(Pickup pickup) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    if (!pickup.getAddedToCal()) {
      SimpleDateFormat formatter = new SimpleDateFormat("YYYYMMdd'T'HHmmssZ");
      String startTime = formatter.format(pickup.getTime());
      Calendar endTimeCal = new Calendar.Builder().setInstant(pickup.getTime()).build();
      endTimeCal.add(Calendar.HOUR, BotConstants.TIME_SLOT_DURATION);
      String endTime = formatter.format(endTimeCal.getTime());

      String storeMapsLink = BotConstants.STORE_NAME_TO_MAPS_LINK.get(pickup.getStoreAddress());

      String gcalLink = String.format(BotConstants.GCAL_LINK_TEMPLATE, startTime, endTime, storeMapsLink);

      suggestions.add(new BusinessMessagesSuggestion()
            .setAction(new BusinessMessagesSuggestedAction()
                .setOpenUrlAction(
                    new BusinessMessagesOpenUrlAction()
                        .setUrl(gcalLink))
                .setText(BotConstants.ADD_TO_CAL_TEXT).setPostbackData(
                  String.format(BotConstants.GCAL_LINK_POSTBACK, pickup.getOrderId())
                )));
    }

    suggestions.addAll(getCancelPickupSuggestion(pickup.getOrderId()));

    return suggestions;
  }

   /**
   * Creates the help suggestion chip. 
   * @return A help suggested reply.
   */
  public static BusinessMessagesSuggestion getHelpMenuItem() {
    return new BusinessMessagesSuggestion()
        .setReply(new BusinessMessagesSuggestedReply()
            .setText(BotConstants.HELP_TEXT).setPostbackData(BotConstants.HELP_TEXT)
        );
  }

  /**
   * Creates a single shop card. Used when there is only one item in the business inventory
   * that matches a user's filters.
   * @return A standalone shop item card.
   */
  public static BusinessMessagesStandaloneCard getShopCard(List<InventoryItem> validItems) {
    BusinessMessagesCardContent card = null;
    for (InventoryItem currentItem : validItems) {
      card = new BusinessMessagesCardContent()
      .setTitle(currentItem.getTitle())
      .setSuggestions(getInventorySuggestions(currentItem.getId()))
      .setMedia(new BusinessMessagesMedia()
        .setHeight(MediaHeight.MEDIUM.toString())
        .setContentInfo(new BusinessMessagesContentInfo()
          .setFileUrl(currentItem.getMediaUrl())
          .setForceRefresh(true)));
    }
    return new BusinessMessagesStandaloneCard().setCardContent(card);
  }

  /**
   * Creates pickup cards to detail relevant information about a scheduled pickup.
   * Allows the user to cancel the pickup or add it to their google calendar.
   * @param pickup The pickup for which the card is being generated.
   * @return The rich card with pickup information.
   */
  public static BusinessMessagesStandaloneCard getPickupCard(Pickup pickup) {
    SimpleDateFormat formatter = new SimpleDateFormat("EEE MM/dd hh:mm a");
    Calendar pickupTimeCal = new Calendar.Builder().setInstant(pickup.getTime()).build();
    pickupTimeCal.add(Calendar.HOUR, 
      -1 * BotConstants.STORE_NAME_TO_TIME_ZONE_OFFSET.get(pickup.getStoreAddress()));
    String startTime = formatter.format(pickupTimeCal.getTime());
    pickupTimeCal.add(Calendar.HOUR, BotConstants.TIME_SLOT_DURATION);
    formatter = new SimpleDateFormat("hh:mm a");
    String endTime = formatter.format(pickupTimeCal.getTime());

    BusinessMessagesCardContent card = new BusinessMessagesCardContent()
      .setTitle(String.format(BotConstants.PICKUP_TITLE, pickup.getOrderId()))
      .setDescription("Store: " + pickup.getStoreAddress() + "\n"
        + "Time: " + startTime + " - " + endTime)
      .setSuggestions(getPickupCardSuggestions(pickup))
      .setMedia(new BusinessMessagesMedia()
        .setHeight(MediaHeight.MEDIUM.toString())
        .setContentInfo(new BusinessMessagesContentInfo()
          .setFileUrl(BotConstants.PICKUP_IMAGE)
          .setForceRefresh(true)));
    return new BusinessMessagesStandaloneCard().setCardContent(card);
  }

  /**
   * Creates a single cart card. Used when there is a single item in a user's cart.
   * @return A standalone cart item card.
   */
  public static BusinessMessagesStandaloneCard getCartCard(Inventory storeInventory, Cart userCart) {
    BusinessMessagesCardContent card = null;
    for (CartItem currentItem : userCart.getItems()) {
      try {
        InventoryItem itemInStore = storeInventory.getItem(currentItem.getId()).get();
        card = new BusinessMessagesCardContent()
        .setTitle(currentItem.getTitle())
        .setDescription("Quantity: " + currentItem.getCount())
        .setSuggestions(getCartSuggestions(currentItem.getId()))
        .setMedia(new BusinessMessagesMedia()
          .setHeight(MediaHeight.MEDIUM.toString())
          .setContentInfo(new BusinessMessagesContentInfo()
            .setFileUrl(itemInStore.getMediaUrl())
            .setForceRefresh(true)));
      } catch (NoSuchElementException e) {
        logger.log(Level.SEVERE, "Item in cart that is no longer in inventory.", e);
      }
    }
    return new BusinessMessagesStandaloneCard().setCardContent(card);
  }

  /**
   * Creates a rich card carousel out of the user's filters.
   * @return A carousel rich card.
   */
  public static BusinessMessagesCarouselCard getFilterCarousel(String conversationId) {
    List<BusinessMessagesCardContent> cardContents = new ArrayList<>();

    Filter colorFilter = FilterManager.getFilter(conversationId, BotConstants.COLOR_FILTER_NAME);
    Filter brandFilter = FilterManager.getFilter(conversationId, BotConstants.BRAND_FILTER_NAME);
    Filter sizeFilter = FilterManager.getFilter(conversationId, BotConstants.SIZE_FILTER_NAME);
    String colorOption;
    String brandOption;
    String sizeOption;
    if (colorFilter == null) colorOption = "None";
    else colorOption = colorFilter.getValue();
    if (brandFilter == null) brandOption = "None";
    else brandOption = brandFilter.getValue();
    if (sizeFilter == null) sizeOption = "None";
    else sizeOption = sizeFilter.getValue();

    cardContents.add(new BusinessMessagesCardContent()
      .setTitle(COLOR_FILTER_CARD_TITLE)
      .setDescription(colorOption)
      .setSuggestions(getFilterCardSuggestions(BotConstants.COLOR_FILTER_NAME))
      .setMedia(new BusinessMessagesMedia()
          .setHeight(MediaHeight.MEDIUM.toString())
          .setContentInfo(new BusinessMessagesContentInfo()
            .setFileUrl(BotConstants.COLOR_CARD_IMAGE)
            .setForceRefresh(true))));

    cardContents.add(new BusinessMessagesCardContent()
      .setTitle(BRAND_FILTER_CARD_TITLE)
      .setDescription(brandOption)
      .setSuggestions(getFilterCardSuggestions(BotConstants.BRAND_FILTER_NAME))
      .setMedia(new BusinessMessagesMedia()
          .setHeight(MediaHeight.MEDIUM.toString())
          .setContentInfo(new BusinessMessagesContentInfo()
            .setFileUrl(BotConstants.BRAND_CARD_IMAGE)
            .setForceRefresh(true))));

    cardContents.add(new BusinessMessagesCardContent()
      .setTitle(SIZE_FILTER_CARD_TITLE)
      .setDescription(sizeOption)
      .setSuggestions(getFilterCardSuggestions(BotConstants.SIZE_FILTER_NAME))
      .setMedia(new BusinessMessagesMedia()
          .setHeight(MediaHeight.MEDIUM.toString())
          .setContentInfo(new BusinessMessagesContentInfo()
            .setFileUrl(BotConstants.SIZE_CARD_IMAGE)
            .setForceRefresh(true))));

    return new BusinessMessagesCarouselCard()
        .setCardContents(cardContents)
        .setCardWidth(CardWidth.MEDIUM.toString());
  }

  /**
   * Creates a rich card carousel full of the user's scheduled pickups
   * so that the user can see details of their pickups and cancel the ones
   * they no longer want scheduled.
   * @param pickups The list of the user's pickups.
   * @return A carousel rich card.
   */
  public static BusinessMessagesCarouselCard getPickupCarousel(List<Pickup> pickups) {
    List<BusinessMessagesCardContent> cardContents = new ArrayList<>();

    for (int i = 0; i < pickups.size() && i < MAX_CAROUSEL_LIMIT; i++) {
      cardContents.add(getPickupCard(pickups.get(i)).getCardContent());
    }

    return new BusinessMessagesCarouselCard()
        .setCardContents(cardContents)
        .setCardWidth(CardWidth.MEDIUM.toString());
  }

  /**
   * Creates a rich card carousel out of items in business inventory.
   * @return A carousel rich card.
   */
  public static BusinessMessagesCarouselCard getShopCarousel(List<InventoryItem> validItems) {
    List<BusinessMessagesCardContent> cardContents = new ArrayList<>();

    for (int i = 0; i < validItems.size() && i < MAX_CAROUSEL_LIMIT; i++) {
      InventoryItem currentItem = validItems.get(i);
      cardContents.add(new BusinessMessagesCardContent()
        .setTitle(currentItem.getTitle())
        .setSuggestions(getInventorySuggestions(currentItem.getId()))
        .setMedia(new BusinessMessagesMedia()
          .setHeight(MediaHeight.MEDIUM.toString())
          .setContentInfo(new BusinessMessagesContentInfo()
            .setFileUrl(currentItem.getMediaUrl())
            .setForceRefresh(true))));
    }

    return new BusinessMessagesCarouselCard()
        .setCardContents(cardContents)
        .setCardWidth(CardWidth.MEDIUM.toString());
  }

  /**
   * Creates a rich card carousel out of items in the user's cart.
   * @return A carousel rich card.
   */
  public static BusinessMessagesCarouselCard getCartCarousel(Inventory storeInventory, Cart userCart) {
    List<BusinessMessagesCardContent> cardContents = new ArrayList<>();

    for (CartItem currentItem : userCart.getItems()) {
      try {
        InventoryItem itemInStore = storeInventory.getItem(currentItem.getId()).get();
        cardContents.add(new BusinessMessagesCardContent()
        .setTitle(currentItem.getTitle())
        .setDescription("Quantity: " + currentItem.getCount())
        .setSuggestions(getCartSuggestions(currentItem.getId()))
        .setMedia(new BusinessMessagesMedia()
          .setHeight(MediaHeight.MEDIUM.toString())
          .setContentInfo(new BusinessMessagesContentInfo()
            .setFileUrl(itemInStore.getMediaUrl())
            .setForceRefresh(true))));
      } catch (NoSuchElementException e) {
        logger.log(Level.SEVERE, "Item in cart not in inventory.", e);
      }
    }

    return new BusinessMessagesCarouselCard()
        .setCardContents(cardContents)
        .setCardWidth(CardWidth.MEDIUM.toString());
  }

  /**
   * Constructs and returns a rich card carousel out of the different store locations such that the user
   * can select one to be the location of their scheduled pickup.
   * @param orderId The order identifier for which the pickup is being scheduled.
   * @return The carousel of different store locations.
   */
  public static BusinessMessagesCarouselCard getStoreAddressCarousel(String orderId) {
    List<BusinessMessagesCardContent> cardContents = new ArrayList<>();

    for (Map.Entry<String, String> ent : BotConstants.STORE_NAME_TO_LOCATION.entrySet()) {
        cardContents.add(new BusinessMessagesCardContent()
        .setTitle(ent.getKey())
        .setDescription(BotConstants.STORE_NAME_TO_ADDRESS.get(ent.getKey()))
        .setSuggestions(getStoreCardSuggestions(orderId, ent.getKey()))
        .setMedia(new BusinessMessagesMedia()
          .setHeight(MediaHeight.MEDIUM.toString())
          .setContentInfo(new BusinessMessagesContentInfo()
            .setFileUrl(ent.getValue())
            .setForceRefresh(true))));
    }

    return new BusinessMessagesCarouselCard()
        .setCardContents(cardContents)
        .setCardWidth(CardWidth.MEDIUM.toString());
  }

  /**
   * Constructs and returns a rich card carousel out of different dates such that the user 
   * can select one to be the day of their scheduled pickup. 
   * @param orderId The order identifier for which the pickup is being scheduled.
   * @return The carousel of different dates with their respective times.
   */
  public static BusinessMessagesCarouselCard getPickupTimesCarousel(String orderId) {
    List<BusinessMessagesCardContent> cardContents = new ArrayList<>();

    for (Map.Entry<String, String> ent : BotConstants.PICKUP_DATES.entrySet()) {
        cardContents.add(new BusinessMessagesCardContent()
        .setTitle(ent.getKey())
        .setSuggestions(getPickupTimeSuggestions(orderId, ent.getValue()))
        .setMedia(new BusinessMessagesMedia()
          .setHeight(MediaHeight.MEDIUM.toString())
          .setContentInfo(new BusinessMessagesContentInfo()
            .setFileUrl(BotConstants.CALENDAR_IMAGE)
            .setForceRefresh(true))));
    }

    return new BusinessMessagesCarouselCard()
        .setCardContents(cardContents)
        .setCardWidth(CardWidth.MEDIUM.toString());
  }
    
}