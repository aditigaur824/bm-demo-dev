package com.google.businessmessages.cart.servlets;

import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.Entity;
import com.google.businessmessages.cart.DataManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Servlet for starting the conversation with the bot.
 */
@WebServlet(name = "GetCart", value = "/cart")
public class GetCart extends HttpServlet {

    public GetCart() {
        super();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // set the response type to JSON
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        // get the user's cart and form the json object
        String cartId = request.getParameter("cartId");
        List<Entity> cartItems = DataManager.getInstance().getCartFromData(cartId);
        JsonObject cartObject = new JsonObject();
        JsonArray items = new JsonArray();
        for (Entity ent : cartItems) {
            JsonObject itemObj = new JsonObject();
            itemObj.addProperty("itemTitle", (String) ent.getProperty("item_title"));
            itemObj.addProperty("itemCount", Long.toString((Long) ent.getProperty("count")));
            items.add(itemObj);
        }
        cartObject.add("items", items);
        // write the json object to the response and commit
        response.getWriter().print(cartObject);
        response.getWriter().flush();   
    }

    @Override   
    public void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        //resp.setHeader("Access-Control-Allow-Headers", SUPPORTED_HEADERS);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        response.flushBuffer();
    }
}