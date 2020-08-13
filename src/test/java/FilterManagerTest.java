import static com.google.common.truth.Truth.assertThat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.businessmessages.cart.Filter;
import com.google.businessmessages.cart.FilterManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FilterManagerTest {
    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    @Before
    public void setUp() {
        helper.setUp();
    }
    
    @Test
    public void testGetFilter() {
        String testGetFilterConversationId = "testGetFilterConversationId";
        String testGetFilterName = "testGetFilterName";
        String testGetFilterValue = "testGetFilterValue";
        Entity testGetFilterEntity = new Entity("Filter");
        testGetFilterEntity.setProperty("conversation_id", testGetFilterConversationId);
        testGetFilterEntity.setProperty("filter_name", testGetFilterName);
        testGetFilterEntity.setProperty("filter_value", testGetFilterValue);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testGetFilterEntity);

        Filter testGetFilter = FilterManager.getFilter(testGetFilterConversationId, testGetFilterName);

        assertThat(testGetFilter.getName()).isEqualTo(testGetFilterName);;
        assertThat(testGetFilter.getValue()).isEqualTo(testGetFilterValue);
    }

    @Test
    public void testGetAllFilters() {
        String testGetActiveFiltersConversationId = "testGetActiveFiltersConversationId";
        String testGetActiveFilterName1 = "testGetActiveFilterName1";
        String testGetActiveFilterValue1 = "testGetActiveFilterValue1";
        Entity testGetActiveFilter1 = new Entity("Filter");
        testGetActiveFilter1.setProperty("conversation_id", testGetActiveFiltersConversationId);
        testGetActiveFilter1.setProperty("filter_name", testGetActiveFilterName1);
        testGetActiveFilter1.setProperty("filter_value", testGetActiveFilterValue1);
        String testGetActiveFilterName2 = "testGetActiveFilterName2";
        String testGetActiveFilterValue2 = "testGetActiveFilterValue2";
        Entity testGetActiveFilter2 = new Entity("Filter");
        testGetActiveFilter2.setProperty("conversation_id", testGetActiveFiltersConversationId);
        testGetActiveFilter2.setProperty("filter_name", testGetActiveFilterName2);
        testGetActiveFilter2.setProperty("filter_value", testGetActiveFilterValue2);
        List<String> testFilterNames = new ArrayList<>();
        testFilterNames.add(testGetActiveFilterName1);
        testFilterNames.add(testGetActiveFilterName2);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testGetActiveFilter1);
        datastore.put(testGetActiveFilter2);

        List<Filter> testFilters = FilterManager.getAllFilters(testGetActiveFiltersConversationId);

        assertThat(testFilters.size()).isEqualTo(2);
        for (Filter testFilter : testFilters) {
            assertThat(testFilter.getName()).isIn(testFilterNames);
            if (testFilter.getName().equals(testGetActiveFilterName1)) {
                assertThat(testFilter.getValue()).isEqualTo(testGetActiveFilterValue1);
            } else {
                assertThat(testFilter.getValue()).isEqualTo(testGetActiveFilterValue2);
            }
        }
    }

    @Test
    public void testSetFilter() {
        String testSetFilterConversationId = "testAddFilterConversationId";
        String testSetFilterName = "testAddFilterName";
        String testSetFilterValue = "testAddFilterValue";

        FilterManager.setFilter(testSetFilterConversationId, testSetFilterName, testSetFilterValue);

        final Query q = new Query("Filter")
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate("conversation_id", Query.FilterOperator.EQUAL, testSetFilterConversationId),
                            new Query.FilterPredicate("filter_name", Query.FilterOperator.EQUAL, testSetFilterName)))
                );
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> testFilter = pq.asList(FetchOptions.Builder.withLimit(1));
        assertThat(testFilter.get(0).getProperty("conversation_id")).isEqualTo(testSetFilterConversationId);
        assertThat(testFilter.get(0).getProperty("filter_name")).isEqualTo(testSetFilterName);
        assertThat(testFilter.get(0).getProperty("filter_value")).isEqualTo(testSetFilterValue);
    }

    @Test
    public void testRemoveFilter() {
        String testRemoveFilterConversationId = "testRemoveFilterConversationId";
        String testRemoveFilterName = "testRemoveFilterName";
        String testRemoveFilterValue = "testRemoveFilterValue";
        Entity testRemoveFilterEntity = new Entity("Filter");
        testRemoveFilterEntity.setProperty("conversation_id", testRemoveFilterConversationId);
        testRemoveFilterEntity.setProperty("filter_name", testRemoveFilterName);
        testRemoveFilterEntity.setProperty("filter_value", testRemoveFilterValue);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testRemoveFilterEntity);

        FilterManager.removeFilter(testRemoveFilterConversationId, testRemoveFilterName);

        final Query q = new Query("Filter")
                .setFilter(
                        new Query.CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new Query.FilterPredicate("conversation_id", Query.FilterOperator.EQUAL, testRemoveFilterConversationId),
                            new Query.FilterPredicate("filter_name", Query.FilterOperator.EQUAL, testRemoveFilterName)))
                );
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> testFilter = pq.asList(FetchOptions.Builder.withLimit(1));
        assertThat(testFilter).isEmpty();
    }

    @After
    public void cleanUp() {
        helper.tearDown();
    }
}