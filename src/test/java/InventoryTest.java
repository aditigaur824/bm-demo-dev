import java.util.Map;
import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;
import com.google.businessmessages.kitchensink.Inventory;
import com.google.businessmessages.kitchensink.MockInventory;
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
        assertTrue("The inventory contains the wrong number of items.", testItemCollection.length == 4);
        for (int i = 0; i < testItemCollection.length; i++) {
            InventoryItem currentItem = (InventoryItem) testItemCollection[i];
            assertTrue("An inventory item title is corrupted.", testItemMap.containsKey(currentItem.getInventoryItemTitle()));
            assertTrue("An inventory item url is corrupted.", testItemMap.containsValue(currentItem.getInventoryItemURL()));
        }
    }
}