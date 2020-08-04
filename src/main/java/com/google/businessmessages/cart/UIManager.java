package com.google.businessmessages.Cart;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesCardContent;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesCarouselCard;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesContentInfo;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesMedia;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesRepresentative;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesStandaloneCard;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestedReply;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestion;
import com.google.common.collect.UnmodifiableIterator;
import com.google.communications.businessmessages.v1.CardWidth;
import com.google.communications.businessmessages.v1.MediaHeight;
import com.google.communications.businessmessages.v1.RepresentativeType;

/**
 * Defines methods to create UI elements such as suggestion chips, rich cards, and 
 * rich card carousels to send back to CartBot. 
 */
public class UIManager {
  private static final Logger logger = Logger.getLogger(Cart.class.getName());

    /**
    * Creates the default menu items for responses.
    *
    * @return List of suggestions to form a menu.
    */
  public static List<BusinessMessagesSuggestion> getDefaultMenu(BusinessMessagesRepresentative representative, Cart userCart) {
     List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();
     suggestions.add(getHelpMenuItem());
 
     suggestions.add(new BusinessMessagesSuggestion()
         .setReply(new BusinessMessagesSuggestedReply()
             .setText("Inquire About Hours").setPostbackData("hours")
         ));
     
     if (!userCart.getItems().isEmpty()) {
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
   * Suggestions to add to inventory cards.
   * @param itemId The id of the item that the suggestions will pertain to.
   * @return List of suggestions.
   */
  public static List<BusinessMessagesSuggestion> getInventorySuggestions(String itemId) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    suggestions.add(
        new BusinessMessagesSuggestion()
            .setReply(new BusinessMessagesSuggestedReply()
                .setText("\uD83D\uDED2 Add to Cart").setPostbackData("add-cart-" + itemId)));

    return suggestions;
  }

    /**
   * Suggestions to add to cart cards.
   * @param itemId The id of the item that the suggestions will pertain to.
   * @return List of suggestions.
   */
  public static List<BusinessMessagesSuggestion> getCartSuggestions(String itemId) {
    List<BusinessMessagesSuggestion> suggestions = new ArrayList<>();

    suggestions.add(
        new BusinessMessagesSuggestion()
            .setReply(new BusinessMessagesSuggestedReply()
                .setText("\u2795").setPostbackData("add-cart-" + itemId)));

    suggestions.add(
        new BusinessMessagesSuggestion()
            .setReply(new BusinessMessagesSuggestedReply()
                .setText("\u2796").setPostbackData("del-cart-" + itemId)));

    return suggestions;
  }

   /**
   * Get the help menu suggested reply.
   *
   * @return A help suggested reply.
   */
  public static BusinessMessagesSuggestion getHelpMenuItem() {
    return new BusinessMessagesSuggestion()
        .setReply(new BusinessMessagesSuggestedReply()
            .setText("Help").setPostbackData("help")
        );
  }

  /**
   * Creates a single cart card.
   *
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
   * Creates a rich card carousel out of items in business inventory.
   *
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
   *
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