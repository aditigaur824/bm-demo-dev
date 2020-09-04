import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.businessmessages.cart.DataManager;
import com.google.businessmessages.cart.Pickup;
import com.google.common.collect.ImmutableList;
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
    public void testAddFilter_isCreatedIfMissing() {
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
        List<Entity> testFilter = pq.asList(FetchOptions.Builder.withLimit(1));
        assertThat(testFilter).isNotEmpty();
        assertThat((String) testFilter.get(0).getProperty("conversation_id")).isEqualTo(testAddFilterConversationId);
        assertThat((String) testFilter.get(0).getProperty("filter_name")).isEqualTo(testAddFilterName);
        assertThat((String) testFilter.get(0).getProperty("filter_value")).isEqualTo(testAddFilterValue);
    }

    @Test
    public void testAddFilter_isUpdatedIfExists() {
        String testUpdateFilterConversationId = "testUpdateFilterConversationId";
        String testUpdateFilterName = "testUpdateFilterName";
        String testUpdateFilterOldValue = "testUpdateFilterOldValue";
        String testUpdateFilterNewValue = "testUpdateFilterNewValue";
        Entity testUpdateFilter = new Entity("Filter");
        testUpdateFilter.setProperty("conversation_id", testUpdateFilterConversationId);
        testUpdateFilter.setProperty("filter_name", testUpdateFilterName);
        testUpdateFilter.setProperty("filter_value", testUpdateFilterOldValue);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testUpdateFilter);

        datamanager.addFilter(testUpdateFilterConversationId, testUpdateFilterName, testUpdateFilterNewValue);

        final Query q = new Query("Filter")
        .setFilter(
                new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                    new Query.FilterPredicate("conversation_id", Query.FilterOperator.EQUAL, testUpdateFilterConversationId),
                    new Query.FilterPredicate("filter_name", Query.FilterOperator.EQUAL, testUpdateFilterName)))
        );
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> testFilter = pq.asList(FetchOptions.Builder.withLimit(1));
        assertThat(testFilter).isNotEmpty();
        assertThat((String) testFilter.get(0).getProperty("conversation_id")).isEqualTo(testUpdateFilterConversationId);
        assertThat((String) testFilter.get(0).getProperty("filter_name")).isEqualTo(testUpdateFilterName);
        assertThat((String) testFilter.get(0).getProperty("filter_value")).isEqualTo(testUpdateFilterNewValue);
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

        ImmutableList<Entity> testCart = datamanager.getCartFromData(testGetCartId);
        
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

        ImmutableList<Entity> testFilters = datamanager.getFiltersFromData(testGetFilterConversationId);
        
        assertThat(testFilters.size()).isEqualTo(2);
        for (Entity ent : testFilters) {
            assertThat((String) ent.getProperty("conversation_id")).isEqualTo(testGetFilterConversationId);
            assertThat((String) ent.getProperty("filter_name")).isIn(testNames);
        }
    }

    @Test
    public void testAddOrder() {
        String testAddOrderConversationId = "testAddOrderConversationId";
        String testAddOrderId = "testAddOrderId";

        datamanager.addOrder(testAddOrderConversationId, testAddOrderId);

        final Query q = new Query("Order")
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate("conversation_id", Query.FilterOperator.EQUAL, testAddOrderConversationId),
                            new Query.FilterPredicate("order_id", Query.FilterOperator.EQUAL, testAddOrderId)))
                );
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> testOrder = pq.asList(FetchOptions.Builder.withLimit(1));
        assertThat(testOrder).isNotEmpty();
        assertThat((String) testOrder.get(0).getProperty("conversation_id")).isEqualTo(testAddOrderConversationId);
        assertThat((String) testOrder.get(0).getProperty("order_id")).isEqualTo(testAddOrderId);
    }

    @Test
    public void testGetOrdersFromData() {
        String testGetOrdersConversationId = "testGetOrdersConversationId";
        String testGetOrderId1 = "testGetOrderId1";
        String testGetOrderId2 = "testGetOrderId2";
        List<String> orderIds = new ArrayList<>();
        orderIds.add(testGetOrderId1);
        orderIds.add(testGetOrderId2);
        Entity testGetOrder1 = new Entity("Order");
        testGetOrder1.setProperty("conversation_id", testGetOrdersConversationId);
        testGetOrder1.setProperty("order_id", testGetOrderId1);
        Entity testGetOrder2 = new Entity("Order");
        testGetOrder2.setProperty("conversation_id", testGetOrdersConversationId);
        testGetOrder2.setProperty("order_id", testGetOrderId2);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testGetOrder1);
        datastore.put(testGetOrder2);

        List<Entity> testOrders = datamanager.getOrdersFromData(testGetOrdersConversationId);

        assertThat(testOrders.size()).isEqualTo(2);
        for (int i = 0; i < 2; i++) {
            assertThat(testOrders.get(i).getProperty("conversation_id")).isEqualTo(testGetOrdersConversationId);
            assertThat(testOrders.get(i).getProperty("order_id")).isIn(orderIds);
            orderIds.remove(testOrders.get(i).getProperty("order_id"));
        }
    }

    @Test
    public void testAddPickup() {
        String testAddPickupConversationId = "testAddPickupConversationId";
        String testAddPickupOrderId = "testAddPickupOrderId";

        datamanager.addPickup(testAddPickupConversationId, testAddPickupOrderId);

        final Query q = new Query("Pickup")
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate("conversation_id", Query.FilterOperator.EQUAL, testAddPickupConversationId),
                            new Query.FilterPredicate("order_id", Query.FilterOperator.EQUAL, testAddPickupOrderId)))
                );
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> testPickup = pq.asList(FetchOptions.Builder.withLimit(1));
        assertThat(testPickup).isNotEmpty();
        assertThat((String) testPickup.get(0).getProperty("conversation_id")).isEqualTo(testAddPickupConversationId);
        assertThat((String) testPickup.get(0).getProperty("order_id")).isEqualTo(testAddPickupOrderId);
    }

    @Test
    public void testCancelPickup() {
        String testCancelPickupConversationId = "testCancelPickupConversationId";
        String testCancelPickupOrderId = "testCancelPickupOrderId";
        Entity testCancelPickup = new Entity("Pickup");
        testCancelPickup.setProperty("conversation_id", testCancelPickupConversationId);
        testCancelPickup.setProperty("order_id", testCancelPickupOrderId);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testCancelPickup);

        datamanager.cancelPickup(testCancelPickupConversationId, testCancelPickupOrderId);

        final Query q = new Query("Pickup")
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate("conversation_id", Query.FilterOperator.EQUAL, testCancelPickupConversationId),
                            new Query.FilterPredicate("order_id", Query.FilterOperator.EQUAL, testCancelPickupOrderId)))
                );
                PreparedQuery pq = datastore.prepare(q);
        List<Entity> testPickup = pq.asList(FetchOptions.Builder.withLimit(1));
        assertThat(testPickup).isEmpty();
    }

    @Test
    public void testGetPickupsFromData() {
        String testGetPickupsConversationId = "testGetPickupsConversationId";
        String testGetPickupsOrderId1 = "testGetPickupsOrderId1";
        String testGetPickupsOrderId2 = "testGetPickupsOrderId2";
        List<String> orderIds = new ArrayList<>();
        orderIds.add(testGetPickupsOrderId1);
        orderIds.add(testGetPickupsOrderId2);
        Entity testGetPickupsOrder1 = new Entity("Pickup");
        testGetPickupsOrder1.setProperty("conversation_id", testGetPickupsConversationId);
        testGetPickupsOrder1.setProperty("order_id", testGetPickupsOrderId1);
        Entity testGetPickupsOrder2 = new Entity("Pickup");
        testGetPickupsOrder2.setProperty("conversation_id", testGetPickupsConversationId);
        testGetPickupsOrder2.setProperty("order_id", testGetPickupsOrderId2);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testGetPickupsOrder1);
        datastore.put(testGetPickupsOrder2);

        List<Entity> testPickups = datamanager.getPickupsFromData(testGetPickupsConversationId);

        assertThat(testPickups.size()).isEqualTo(2);
        for (int i = 0; i < 2; i++) {
            assertThat(testPickups.get(i).getProperty("conversation_id")).isEqualTo(testGetPickupsConversationId);
            assertThat(testPickups.get(i).getProperty("order_id")).isIn(orderIds);
            orderIds.remove(testPickups.get(i).getProperty("order_id"));
        }
    }

    @Test
    public void testGetPickupsWithStatus() {
        String testGetPickupsWSConversationId = "testGetPickupsWSConversationId";
        String testGetPickupsWSOrderId1 = "testGetPickupsWSOrderId1";
        String testGetPickupsWSOrderId2 = "testGetPickupsWSOrderId2";
        String testGetPickupsStatus1 = "incomplete";
        String testGetPickupsStatus2 = "scheduled";
        List<String> orderIds = new ArrayList<>();
        orderIds.add(testGetPickupsWSOrderId1);
        orderIds.add(testGetPickupsWSOrderId2);
        Entity testGetPickupsWSOrder1 = new Entity("Pickup");
        testGetPickupsWSOrder1.setProperty("conversation_id", testGetPickupsWSConversationId);
        testGetPickupsWSOrder1.setProperty("order_id", testGetPickupsWSOrderId1);
        testGetPickupsWSOrder1.setProperty("pickup_status", testGetPickupsStatus1);
        Entity testGetPickupsWSOrder2 = new Entity("Pickup");
        testGetPickupsWSOrder2.setProperty("conversation_id", testGetPickupsWSConversationId);
        testGetPickupsWSOrder2.setProperty("order_id", testGetPickupsWSOrderId2);
        testGetPickupsWSOrder2.setProperty("pickup_status", testGetPickupsStatus2);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testGetPickupsWSOrder1);
        datastore.put(testGetPickupsWSOrder2);

        List<Entity> testPickups = datamanager.getPickupsWithStatus(testGetPickupsWSConversationId, Pickup.Status.SCHEDULED);

        assertThat(testPickups.size()).isEqualTo(1);
        assertThat(testPickups.get(0).getProperty("conversation_id")).isEqualTo(testGetPickupsWSConversationId);
        assertThat(testPickups.get(0).getProperty("order_id")).isEqualTo(testGetPickupsWSOrderId2);
        assertThat(testPickups.get(0).getProperty("pickup_status")).isEqualTo(testGetPickupsStatus2);
    }

    @Test
    public void testUpdatePickupProperties() {
        String testUpdatePropertiesConversationId = "testUpdatePropertiesConversationId";
        String testUpdatePropertiesOrderId = "testUpdatePropertiesOrderId";
        String testStoreAddress = "testStoreAddress";
        Date testDate = new Date();
        Entity testUpdatePickup = new Entity("Pickup");
        testUpdatePickup.setProperty("conversation_id", testUpdatePropertiesConversationId);
        testUpdatePickup.setProperty("order_id", testUpdatePropertiesOrderId);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testUpdatePickup);

        datamanager.updatePickupProperties(testUpdatePropertiesConversationId, testUpdatePropertiesOrderId, 
            "store-address", testStoreAddress);
        datamanager.updatePickupProperties(testUpdatePropertiesConversationId, testUpdatePropertiesOrderId, 
            "pickup-date", testDate);
        datamanager.updatePickupProperties(testUpdatePropertiesConversationId, testUpdatePropertiesOrderId, 
            "pickup-status", Pickup.Status.SCHEDULED);

        final Query q = new Query("Pickup")
            .setFilter(
                    new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                        new Query.FilterPredicate("conversation_id", Query.FilterOperator.EQUAL, testUpdatePropertiesConversationId),
                        new Query.FilterPredicate("order_id", Query.FilterOperator.EQUAL, testUpdatePropertiesOrderId)))
            );
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> testPickup = pq.asList(FetchOptions.Builder.withLimit(1));
        assertThat(testPickup).isNotEmpty();
        assertThat(testPickup.get(0).getProperty("conversation_id")).isEqualTo(testUpdatePropertiesConversationId);
        assertThat(testPickup.get(0).getProperty("order_id")).isEqualTo(testUpdatePropertiesOrderId);
        assertThat(testPickup.get(0).getProperty("store_address")).isEqualTo(testStoreAddress);
        assertThat((Date) testPickup.get(0).getProperty("pickup_time")).isEqualTo(testDate);
        assertThat(testPickup.get(0).getProperty("pickup_status")).isEqualTo("scheduled");
    }

    @Test
    public void testGetExistingPickup() {
        String testGetPickupConversationId = "testGetPickupConversationId";
        String testGetPickupOrderId = "testGetPickupOrderId";
        Entity testGetPickup = new Entity("Pickup");
        testGetPickup.setProperty("conversation_id", testGetPickupConversationId);
        testGetPickup.setProperty("order_id", testGetPickupOrderId);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testGetPickup);

        Entity testPickup = datamanager.getExistingPickup(testGetPickupConversationId, testGetPickupOrderId);

        assertThat(testPickup).isNotNull();
        assertThat(testPickup.getProperty("conversation_id")).isEqualTo(testGetPickupConversationId);
        assertThat(testPickup.getProperty("order_id")).isEqualTo(testGetPickupOrderId);
    }

    @After
    public void cleanUp() {
        helper.tearDown();
    }

}