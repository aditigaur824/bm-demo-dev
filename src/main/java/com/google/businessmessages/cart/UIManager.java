package com.google.businessmessages.cart;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.services.businessmessages.v1.model.BusinessMessagesCardContent;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesCarouselCard;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesContentInfo;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesMedia;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesStandaloneCard;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestedReply;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestion;
import com.google.common.collect.UnmodifiableIterator;
import com.google.communications.businessmessages.v1.CardWidth;
import com.google.communications.businessmessages.v1.MediaHeight;

/**
 * Defines methods to create UI elements such as suggestion chips, rich cards, and 
 * rich card carousels to send back to CartBot. 
 */
public class UIManager {
  private static final String COLOR_FILTER_CARD_TITLE = "Color";
  private static final String BRAND_FILTER_CARD_TITLE = "Brand";
  private static final Logger logger = Logger.getLogger(Cart.class.getName());

    /**
    * Creates the default menu items for responses.
    * @return List of suggestions to form a menu.
    */
  public static List<BusinessMessagesSuggestion> getDefaultMenu(Cart userCart) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

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

    suggestions.add(new BusinessMessagesSuggestion()
        .setReply(new BusinessMessagesSuggestedReply()
            .setText(BotConstants.HOURS_TEXT).setPostbackData(BotConstants.HOURS_COMMAND)
        ));

    suggestions.add(getHelpMenuItem());

    return suggestions;
   }

   /**
   * Creates suggestions to add to filter cards. 
   * @param itemId The id of the item that the suggestions will pertain to.
   * @return List of suggestions.
   */
  public static List<BusinessMessagesSuggestion> getFilterSuggestions(String filterName) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    List<String> filterOptions;
    if (filterName.equals(BotConstants.COLOR_FILTER_NAME)) {
      filterOptions = BotConstants.colorList;
    } else if (filterName.equals(BotConstants.BRAND_FILTER_NAME)) {
      filterOptions = BotConstants.brandList;
    } else {
      filterOptions = new ArrayList<>();
    }

    suggestions.add(
          new BusinessMessagesSuggestion()
              .setReply(new BusinessMessagesSuggestedReply()
                  .setText("Remove").setPostbackData(BotConstants.REMOVE_FILTER_COMMAND + filterName)));
    for (String option : filterOptions) {
      suggestions.add(
          new BusinessMessagesSuggestion()
              .setReply(new BusinessMessagesSuggestedReply()
                  .setText(option).setPostbackData(BotConstants.SET_FILTER_COMMAND + filterName + "-" + option)));
    }
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
                .setText(BotConstants.ADD_ITEM_TEXT).setPostbackData(BotConstants.ADD_ITEM_COMMAND + itemId)));

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
                .setText(BotConstants.INCREMENT_COUNT_TEXT).setPostbackData(BotConstants.ADD_ITEM_COMMAND + itemId)));

    suggestions.add(
        new BusinessMessagesSuggestion()
            .setReply(new BusinessMessagesSuggestedReply()
                .setText(BotConstants.DECREMENT_COUNT_TEXT).setPostbackData(BotConstants.DELETE_ITEM_COMMAND + itemId)));

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
   * Creates a single cart card.
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
    String colorOption;
    String brandOption;
    if (colorFilter == null) colorOption = "None";
    else colorOption = colorFilter.getValue();
    if (brandFilter == null) brandOption = "None";
    else brandOption = brandFilter.getValue();

    cardContents.add(new BusinessMessagesCardContent()
      .setTitle(COLOR_FILTER_CARD_TITLE)
      .setDescription(colorOption)
      .setSuggestions(getFilterSuggestions(BotConstants.COLOR_FILTER_NAME)));

    cardContents.add(new BusinessMessagesCardContent()
      .setTitle(BRAND_FILTER_CARD_TITLE)
      .setDescription(brandOption)
      .setSuggestions(getFilterSuggestions(BotConstants.BRAND_FILTER_NAME)));

    return new BusinessMessagesCarouselCard()
        .setCardContents(cardContents)
        .setCardWidth(CardWidth.MEDIUM.toString());
  }

  /**
   * Creates a rich card carousel out of items in business inventory.
   * @return A carousel rich card.
   */
  public static BusinessMessagesCarouselCard getShopCarousel(Inventory storeInventory) {
    List<BusinessMessagesCardContent> cardContents = new ArrayList<>();

    UnmodifiableIterator<InventoryItem> iterator = storeInventory.getInventory().iterator();
    while(iterator.hasNext()) {
      InventoryItem currentItem = iterator.next();
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
    
}