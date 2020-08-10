import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.businessmessages.cart.DataManager;
import static com.google.common.truth.Truth.assertThat;

public class DataManagerTest {

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    DataManager datamanager;

    @Before
    public void initDataManager() {
        helper.setUp();
        datamanager = DataManager.getInstance();
    }
    
    @Test
    public void testSaveCart() {
        String testSaveCartConversationId = "testSaveCartConversationId";
        String testSaveCartId = "testSaveCartId";

        datamanager.saveCart(testSaveCartConversationId, testSaveCartId);

        final Query q = new Query("Cart")
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate("conversation_id", Query.FilterOperator.EQUAL, testSaveCartConversationId),
                            new Query.FilterPredicate("cart_id", Query.FilterOperator.EQUAL, testSaveCartId)))
                );
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> testCart = pq.asList(FetchOptions.Builder.withLimit(1));
        assertThat(testCart).isNotEmpty();
        assertThat((String) testCart.get(0).getProperty("conversation_id")).isEqualTo(testSaveCartConversationId);
        assertThat((String) testCart.get(0).getProperty("cart_id")).isEqualTo(testSaveCartId);
    }

    @Test
    public void testGetCart() {
        String testGetCartConversationId = "testGetCartConversationId";
        String testGetCartId = "testGetCartId";
        Entity testCart = new Entity("Cart");
        testCart.setProperty("conversation_id", testGetCartConversationId);
        testCart.setProperty("cart_id", testGetCartId);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testCart);

        Entity resultCart = datamanager.getCart(testGetCartConversationId);

        assertThat(resultCart).isNotNull();
        assertThat((String) resultCart.getProperty("conversation_id")).isEqualTo(testGetCartConversationId);
        assertThat((String) resultCart.getProperty("cart_id")).isEqualTo(testGetCartId);
    }

    @Test
    public void testGetExistingItem() {
        String testGetItemCartId = "testGetItemCartId";
        String testGetItemId = "testGetItemId";
        String testGetItemTitle = "testGetItemTitle";
        Entity testGetItem = new Entity("CartItem");
        testGetItem.setProperty("cart_id", testGetItemCartId);
        testGetItem.setProperty("item_id", testGetItemId);
        testGetItem.setProperty("item_title", testGetItemTitle);
        testGetItem.setProperty("count", 1);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testGetItem);

        Entity testResult = datamanager.getExistingItem(testGetItemCartId, testGetItemId);

        assertThat(testResult).isNotNull();
        assertThat((String) testResult.getProperty("cart_id")).isEqualTo(testGetItemCartId);
        assertThat((String) testResult.getProperty("item_id")).isEqualTo(testGetItemId);
        assertThat((String) testResult.getProperty("item_title")).isEqualTo(testGetItemTitle);
        assertThat(((Long) testResult.getProperty("count")).intValue()).isEqualTo(1);
    }

    @Test
    public void testGetExistingFilter() {
        String testGetFilterConversationId = "testGetFilterConversationId";
        String testGetFilterName = "testGetFilterName";
        String testGetFilterValue = "testGetFilterValue";
        Entity testGetFilter = new Entity("Filter");
        testGetFilter.setProperty("conversation_id", testGetFilterConversationId);
        testGetFilter.setProperty("filter_name", testGetFilterName);
        testGetFilter.setProperty("filter_value", testGetFilterValue);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testGetFilter);

        Entity testResult = datamanager.getExistingFilter(testGetFilterConversationId, testGetFilterName);

        assertThat(testResult).isNotNull();
        assertThat((String) testResult.getProperty("conversation_id")).isEqualTo(testGetFilterConversationId);
        assertThat((String) testResult.getProperty("filter_name")).isEqualTo(testGetFilterName);
        assertThat((String) testResult.getProperty("filter_value")).isEqualTo(testGetFilterValue);
    }    
    
    @Test
    public void testAddItemToCart() {
        String testAddItemCartId = "testAddItemCartId";
        String testAddItemId = "testAddItemId";
        String testAddItemTitle = "testAddItemTitle";

        datamanager.addItemToCart(testAddItemCartId, testAddItemId, testAddItemTitle);

        final Query q = new Query("CartItem")
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate("cart_id", Query.FilterOperator.EQUAL, testAddItemCartId),
                            new Query.FilterPredicate("item_id", Query.FilterOperator.EQUAL, testAddItemId)))
                );
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> testCart = pq.asList(FetchOptions.Builder.withLimit(1));
        assertThat(testCart).isNotEmpty();
        assertThat((String) testCart.get(0).getProperty("cart_id")).isEqualTo(testAddItemCartId);
        assertThat((String) testCart.get(0).getProperty("item_id")).isEqualTo(testAddItemId);
        assertThat((String) testCart.get(0).getProperty("item_title")).isEqualTo(testAddItemTitle);
        assertThat(((Long) testCart.get(0).getProperty("count")).intValue()).isEqualTo(1);
    }

    @Test
    public void testAddFilter() {
        String testAddFilterConversationId = "testAddFilterConversationId";
        String testAddFilterName = "testAddFilterName";
        String testAddFilterValue = "testAddFilterValue";

        datamanager.addFilter(testAddFilterConversationId, testAddFilterName, testAddFilterValue);

        final Query q = new Query("Filter")
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate("conversation_id", Query.FilterOperator.EQUAL, testAddFilterConversationId),
                            new Query.FilterPredicate("filter_name", Query.FilterOperator.EQUAL, testAddFilterName)))
                );
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> testCart = pq.asList(FetchOptions.Builder.withLimit(1));
        assertThat(testCart).isNotEmpty();
        assertThat((String) testCart.get(0).getProperty("conversation_id")).isEqualTo(testAddFilterConversationId);
        assertThat((String) testCart.get(0).getProperty("filter_name")).isEqualTo(testAddFilterName);
        assertThat((String) testCart.get(0).getProperty("filter_value")).isEqualTo(testAddFilterValue);
    }

    @Test
    public void testDeleteItemFromCart() {
        String testDeleteItemCartId = "testDeleteItemCartId";
        String testDeleteItemId = "testDeleteItemId";
        Entity testDeleteItem = new Entity("CartItem");
        testDeleteItem.setProperty("cart_id", testDeleteItemCartId);
        testDeleteItem.setProperty("item_id", testDeleteItemId);
        testDeleteItem.setProperty("count", 1);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testDeleteItem);

        datamanager.deleteItemFromCart(testDeleteItemCartId, testDeleteItemId);

        final Query q = new Query("CartItem")
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate("cart_id", Query.FilterOperator.EQUAL, testDeleteItemCartId),
                            new Query.FilterPredicate("item_id", Query.FilterOperator.EQUAL, testDeleteItemId)))
                );
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> testCart = pq.asList(FetchOptions.Builder.withLimit(50));
        assertThat(testCart).isEmpty();
    }

    @Test
    public void testRemoveFilter() {
        String testDeleteFilterConversationId = "testDeleteFilterConversationId";
        String testDeleteFilterName = "testDeleteFilterName";
        Entity testDeleteFilter = new Entity("Filter");
        testDeleteFilter.setProperty("conversation_id", testDeleteFilterConversationId);
        testDeleteFilter.setProperty("filter_name", testDeleteFilterName);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testDeleteFilter);

        datamanager.removeFilter(testDeleteFilterConversationId, testDeleteFilterName);

        final Query q = new Query("Filter")
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate("conversation_id", Query.FilterOperator.EQUAL, testDeleteFilterConversationId),
                            new Query.FilterPredicate("filter_name", Query.FilterOperator.EQUAL, testDeleteFilterName)))
                );
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> testCart = pq.asList(FetchOptions.Builder.withLimit(50));
        assertThat(testCart).isEmpty();
    }

    @Test
    public void testGetCartFromData() {
        String testGetCartId = "testGetCartId";
        List<String> testIds = new ArrayList<>();
        String testGetCartItemId1 = "testGetCartItemId1";
        String testGetCartItemId2 = "testGetCartItemId2";
        testIds.add(testGetCartItemId1);
        testIds.add(testGetCartItemId2);
        Entity testGetCartItem1 = new Entity("CartItem");
        testGetCartItem1.setProperty("cart_id", testGetCartId);
        testGetCartItem1.setProperty("item_id", testGetCartItemId1);
        testGetCartItem1.setProperty("count", 1);
        Entity testGetCartItem2 = new Entity("CartItem");
        testGetCartItem2.setProperty("cart_id", testGetCartId);
        testGetCartItem2.setProperty("item_id", testGetCartItemId2);
        testGetCartItem2.setProperty("count", 1);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testGetCartItem1);
        datastore.put(testGetCartItem2);

        List<Entity> testCart = datamanager.getCartFromData(testGetCartId);
        
        assertThat(testCart.size()).isEqualTo(2);
        for (Entity ent : testCart) {
            assertThat((String) ent.getProperty("cart_id")).isEqualTo(testGetCartId);
            assertThat((String) ent.getProperty("item_id")).isIn(testIds);
        }
    }

    @Test
    public void testGetFiltersFromData() {
        String testGetFilterConversationId = "testGetFilterConversationId";
        List<String> testNames = new ArrayList<>();
        String testGetFilterName1 = "testGetFilterName1";
        String testGetFilterName2 = "testGetFilterName2";
        testNames.add(testGetFilterName1);
        testNames.add(testGetFilterName2);
        Entity testGetFilter1 = new Entity("Filter");
        testGetFilter1.setProperty("conversation_id", testGetFilterConversationId);
        testGetFilter1.setProperty("filter_name", testGetFilterName1);
        Entity testGetFilter2 = new Entity("Filter");
        testGetFilter2.setProperty("conversation_id", testGetFilterConversationId);
        testGetFilter2.setProperty("filter_name", testGetFilterName2);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testGetFilter1);
        datastore.put(testGetFilter2);

        List<Entity> testFilters = datamanager.getFiltersFromData(testGetFilterConversationId);
        
        assertThat(testFilters.size()).isEqualTo(2);
        for (Entity ent : testFilters) {
            assertThat((String) ent.getProperty("conversation_id")).isEqualTo(testGetFilterConversationId);
            assertThat((String) ent.getProperty("filter_name")).isIn(testNames);
        }
    }

    @After
    public void cleanUp() {
        helper.tearDown();
    }

}