import java.util.Map;
import java.util.Optional;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import static com.google.common.truth.Truth.assertThat;
import com.google.businessmessages.cart.Inventory;
import com.google.businessmessages.cart.InventoryItem;
import com.google.businessmessages.cart.MockInventory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;

public class MockInventoryTest {
    
    @Test
    public void testGetInventory() {
        Map<String, String> testItemMap = new HashMap<>();
        testItemMap.put("testItem1", "testUrl1");
        testItemMap.put("testItem2", "testUrl2");
        testItemMap.put("testItem3", "testUrl3");
        testItemMap.put("testItem4", "testUrl4");
        Map<String, Map<String, List<String>>> testItemProperties = new HashMap<>();
        testItemProperties.put("testItem1", ImmutableMap.of("testProp", Arrays.asList("1", "2")));
        testItemProperties.put("testItem2", ImmutableMap.of("testProp", Arrays.asList("1", "2")));
        testItemProperties.put("testItem3", ImmutableMap.of("testProp", Arrays.asList("1", "2")));
        testItemProperties.put("testItem4", ImmutableMap.of("testProp", Arrays.asList("1", "2")));
        Inventory testInventory = new MockInventory(testItemMap, testItemProperties);

        Object[] testItemCollection = testInventory.getInventory().toArray();
        
        assertThat(testItemCollection).hasLength(4);
        for (int i = 0; i < testItemCollection.length; i++) {
            InventoryItem currentItem = (InventoryItem) testItemCollection[i];
            assertThat(testItemMap).containsEntry(currentItem.getTitle(), currentItem.getMediaUrl());
        }
    }

    @Test
    public void testGetItem() {
        Map<String, String> testItemMap = new HashMap<>();
        testItemMap.put("testItem1", "testUrl1");
        Map<String, Map<String, List<String>>> testItemProperties = new HashMap<>();
        testItemProperties.put("testItem1", ImmutableMap.of("testProp", Arrays.asList("1", "2")));
        Inventory testInventory = new MockInventory(testItemMap, testItemProperties);
        UnmodifiableIterator<InventoryItem> iterator = testInventory.getInventory().iterator();
        InventoryItem testItem = iterator.next();

        Optional<InventoryItem> resultItem = testInventory.getItem(testItem.getId());

        assertThat(resultItem.isPresent()).isTrue();
        assertThat(resultItem.get().getId()).isEqualTo(testItem.getId());
        assertThat(resultItem.get().getTitle()).isEqualTo(testItem.getTitle());
        assertThat(resultItem.get().getMediaUrl()).isEqualTo(testItem.getMediaUrl());
    }
}