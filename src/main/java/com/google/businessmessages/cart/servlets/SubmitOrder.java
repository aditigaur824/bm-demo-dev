package com.google.businessmessages.cart.servlets;

import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesRepresentative;
import com.google.appengine.api.datastore.Entity;
import com.google.businessmessages.cart.BotConstants;
import com.google.businessmessages.cart.CartBot;
import com.google.businessmessages.cart.DataManager;
import com.google.businessmessages.cart.OrderManager;
import com.google.communications.businessmessages.v1.RepresentativeType;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@WebServlet(name = "SubmitOrder", value = "/submitorder")
public class SubmitOrder extends HttpServlet {

    public SubmitOrder() {
        super();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // set the response type to JSON
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        // create the user's order 
        String cartId = request.getParameter("cartId");
        String orderId = request.getParameter("orderId");
        Entity cartEntity = DataManager.getInstance().getUserFromCartId(cartId);
        OrderManager.addOrder((String) cartEntity.getProperty("conversation_id"), 
            orderId);
        // form json object with order items to display on order confirmation page
        List<Entity> orderItems = DataManager.getInstance().getCartFromData(cartId);
        JsonObject orderObject = new JsonObject();
        JsonArray items = new JsonArray();
        for (Entity ent : orderItems) {
            JsonObject itemObj = new JsonObject();
            itemObj.addProperty("itemTitle", (String) ent.getProperty("item_title"));
            itemObj.addProperty("itemCount", Long.toString((Long) ent.getProperty("count")));
            items.add(itemObj);
        }
        orderObject.add("items", items);
        // delete all items from cart now that order is complete
        DataManager.getInstance().emptyCart(cartId);
        // send message to user indicating they can schedule pickup
        CartBot bot = new CartBot(new BusinessMessagesRepresentative()
                .setRepresentativeType(RepresentativeType.BOT.toString())
                .setDisplayName(BotConstants.BOT_AGENT_NAME));
        bot.setUserCart((String) cartEntity.getProperty("conversation_id"));
        bot.sendResponse(BotConstants.PLACED_ORDER_RESPONSE_TEXT, (String) cartEntity.getProperty("conversation_id"));
        // write the json object to the response and commit
        response.getWriter().print(orderObject);
        response.getWriter().flush();   
    }

    @Override   
    public void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        //resp.setHeader("Access-Control-Allow-Headers", SUPPORTED_HEADERS);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        response.flushBuffer();
    } 

}