import static com.google.common.truth.Truth.assertThat;
import java.util.HashSet;
import java.util.Set;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.businessmessages.Cart.Cart;
import com.google.businessmessages.Cart.CartItem;
import com.google.common.collect.UnmodifiableIterator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CartTest {

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    @Before
    public void setUp() {
        helper.setUp();
    }
    
    @Test
    public void testPopulate() {
        Cart cart = new Cart("testPopulateConversationId");
        String testPopulateItemTitle1 = "testPopulateItemTitle1";
        Entity testPopulateItem1 = new Entity("CartItem");
        testPopulateItem1.setProperty("cart_id", cart.getId());
        testPopulateItem1.setProperty("item_title", testPopulateItemTitle1);
        testPopulateItem1.setProperty("count", 1);
        String testPopulateItemTitle2 = "testPopulateItemTitle2";
        Entity testPopulateItem2 = new Entity("CartItem");
        testPopulateItem2.setProperty("cart_id", cart.getId());
        testPopulateItem2.setProperty("item_title", testPopulateItemTitle2);
        testPopulateItem2.setProperty("count", 1);
        Set<String> testItemTitles = new HashSet<>();
        testItemTitles.add(testPopulateItemTitle1);
        testItemTitles.add(testPopulateItemTitle2);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testPopulateItem1);
        datastore.put(testPopulateItem2);

        cart.populate();

        assertThat(cart.size()).isEqualTo(2);
        UnmodifiableIterator<CartItem> iterator = cart.getCart().iterator();
        while (iterator.hasNext()) {
            CartItem currentItem = iterator.next();
            assertThat(currentItem.getTitle()).isIn(testItemTitles);
        }
    }

    @Test
    public void testAddItem() {
        Cart cart = new Cart("testAddConversationId");
        String testAddItemTitle = "testAddItemTitle";
        String testAddItemId = "testAddItemId";

        cart.addItem(testAddItemId, testAddItemTitle);

        assertThat(cart.size()).isEqualTo(1);
        UnmodifiableIterator<CartItem> iterator = cart.getCart().iterator();
        CartItem currentItem = iterator.next();
        assertThat(currentItem.getTitle()).isEqualTo(testAddItemTitle);
        assertThat(currentItem.getId()).isEqualTo(testAddItemId);
    }

    @Test
    public void testDeleteItem() {
        Cart cart = new Cart("testDeleteConversationId");
        String testDeleteItemId = "testDeleteItemId";
        String testDeleteItemTitle = "testDeleteItemTitle";
        Entity testDeleteItem = new Entity("CartItem");
        testDeleteItem.setProperty("cart_id", cart.getId());
        testDeleteItem.setProperty("item_id", testDeleteItemId);
        testDeleteItem.setProperty("item_title", testDeleteItemTitle);
        testDeleteItem.setProperty("count", 1);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(testDeleteItem);

        cart.deleteItem(testDeleteItemId);

        assertThat(cart.size()).isEqualTo(0);
    }

    @After
    public void cleanUp() {
        helper.tearDown();
    }
}