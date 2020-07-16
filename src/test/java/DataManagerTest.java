import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Logger;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.businessmessages.kitchensink.DataManager;

public class DataManagerTest {

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    DataManager datamanager;
    Logger logger;

    @Before
    public void initDataManager() {
        helper.setUp();
        datamanager = new DataManager();
        logger = Logger.getLogger("DataManagerTest");
    }
    
    @Test
    public void testGetItem() {
        String testGetItemConversationId = "testGetItemConversationId";
        String testGetItemTitle = "testGetItemTitle";
        Entity testGetItem = new Entity("CartItem");
        testGetItem.setProperty("conversation_id", testGetItemConversationId);
        testGetItem.setProperty("item_title", testGetItemTitle);
        testGetItem.setProperty("count", 1);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testGetItem);

        Entity testResult = datamanager.getExistingItem(testGetItemConversationId, testGetItemTitle);

        assertTrue("Getting existing item has failed.", testResult != null);
    }
    
    @Test
    public void testAddItemToCart() {
        String testAddItemConversationId = "testAddItemConversationId";
        String testAddItemTitle = "testAddItemTitle";

        datamanager.addItemToCart(testAddItemConversationId, testAddItemTitle, logger);

        final Query q = new Query("CartItem")
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                        new Query.FilterPredicate("conversation_id", Query.FilterOperator.EQUAL, testAddItemConversationId),
                        new Query.FilterPredicate("item_title", Query.FilterOperator.EQUAL, testAddItemTitle)))
                );
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> testCart = pq.asList(FetchOptions.Builder.withLimit(1));
        assertTrue("Adding test item has failed.", testCart.isEmpty() == false);
    }

    @Test
    public void testDeleteItemFromCart() {
        String testDeleteItemConversationId = "testDeleteItemConversationId";
        String testDeleteItemTitle = "testDeleteItemTitle";
        Entity testDeleteItem = new Entity("CartItem");
        testDeleteItem.setProperty("conversation_id", testDeleteItemConversationId);
        testDeleteItem.setProperty("item_title", testDeleteItemTitle);
        testDeleteItem.setProperty("count", 1);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testDeleteItem);

        datamanager.deleteItemFromCart(testDeleteItemConversationId, testDeleteItemTitle, logger);

        final Query q = new Query("CartItem")
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                        new Query.FilterPredicate("conversation_id", Query.FilterOperator.EQUAL, testDeleteItemConversationId),
                        new Query.FilterPredicate("item_title", Query.FilterOperator.EQUAL, testDeleteItemTitle)))
                );
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> testCart = pq.asList(FetchOptions.Builder.withLimit(50));
        assertTrue("Deleting test item has failed.", testCart.isEmpty() == true);
    }

    @Test
    public void testGetCart() {
        String testGetCartConversationId = "testGetCartConversationId";
        String testGetCartItemTitle1 = "testGetCartItemTitle1";
        String testGetCartItemTitle2 = "testGetCartItemTitle2";
        Entity testGetCartItem1 = new Entity("CartItem");
        testGetCartItem1.setProperty("conversation_id", testGetCartConversationId);
        testGetCartItem1.setProperty("item_title", testGetCartItemTitle1);
        testGetCartItem1.setProperty("count", 1);
        Entity testGetCartItem2 = new Entity("CartItem");
        testGetCartItem2.setProperty("conversation_id", testGetCartConversationId);
        testGetCartItem2.setProperty("item_title", testGetCartItemTitle2);
        testGetCartItem2.setProperty("count", 1);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testGetCartItem1);
        datastore.put(testGetCartItem2);

        List<Entity> testCart = datamanager.getCartFromData(testGetCartConversationId);
        
        assertTrue("Getting cart from data has failed.", testCart.size() == 2);
    }

    @After
    public void cleanUp() {
        helper.tearDown();
    }

}