import java.util.Map;
import java.util.HashMap;
import org.junit.Test;
import static com.google.common.truth.Truth.assertThat;
import com.google.businessmessages.kitchensink.Inventory;
import com.google.businessmessages.kitchensink.MockInventory;
import com.google.common.collect.UnmodifiableIterator;
import com.google.businessmessages.kitchensink.InventoryItem;

public class InventoryTest {
    
    @Test
    public void testGetInventory() {
        Map<String, String> testItemMap = new HashMap<>();
        testItemMap.put("testItem1", "testUrl1");
        testItemMap.put("testItem2", "testUrl2");
        testItemMap.put("testItem3", "testUrl3");
        testItemMap.put("testItem4", "testUrl4");
        Inventory testInventory = new MockInventory(testItemMap);

        Object[] testItemCollection = testInventory.getInventory().toArray();
        
        assertThat(testItemCollection).hasLength(4);
        for (int i = 0; i < testItemCollection.length; i++) {
            InventoryItem currentItem = (InventoryItem) testItemCollection[i];
            assertThat(testItemMap).containsEntry(currentItem.getInventoryItemTitle(), currentItem.getInventoryItemURL());
        }
    }

    @Test
    public void testGetItem() {
        Map<String, String> testItemMap = new HashMap<>();
        testItemMap.put("testItem1", "testUrl1");
        Inventory testInventory = new MockInventory(testItemMap);
        UnmodifiableIterator<InventoryItem> iterator = testInventory.getInventory().iterator();
        InventoryItem testItem = iterator.next();

        InventoryItem resultItem = testInventory.getItem(testItem.getInventoryItemId());

        assertThat(resultItem.getInventoryItemId()).isEqualTo(testItem.getInventoryItemId());
        assertThat(resultItem.getInventoryItemTitle()).isEqualTo(testItem.getInventoryItemTitle());
        assertThat(resultItem.getInventoryItemURL()).isEqualTo(testItem.getInventoryItemURL());
    }
}