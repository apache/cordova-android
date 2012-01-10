import org.junit.*;
import static org.junit.Assert.*;

import com.phonegap.PreferenceNode;
import com.phonegap.PreferenceSet;

public class PreferenceSetTest {
    private PreferenceSet preferences;
    private PreferenceNode screen;

    @Before
        public void setUp() {
            preferences = new PreferenceSet();
            screen = new PreferenceNode("fullscreen", "true", false);
        }

    @Test
        public void testAddition() {
            preferences.add(screen);
            assertEquals(1, preferences.size());
        }

    @Test
        public void testClear() {
            preferences.add(screen);
            preferences.clear();
            assertEquals(0, preferences.size());
        }

    @Test
        public void testPreferenceRetrieval() {
            preferences.add(screen);
            assertEquals("true", preferences.pref("fullscreen"));
        }

    @Test
        public void testNoPreferenceRetrieval() {
            // return null if the preference is not defined
            assertEquals(null, preferences.pref("antigravity"));
        }
}
