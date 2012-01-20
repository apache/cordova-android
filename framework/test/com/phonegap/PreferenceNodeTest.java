import org.junit.*;
import static org.junit.Assert.*;

import com.phonegap.PreferenceNode;

public class PreferenceNodeTest {
    @Test
        public void testConstructor() {
            PreferenceNode foo = new com.phonegap.PreferenceNode("fullscreen", "false", false);
            assertEquals("fullscreen", foo.name);
            assertEquals("false", foo.value);
            assertEquals(false, foo.readonly);
        }

    @Test
        public void testNameAssignment() {
            PreferenceNode foo = new com.phonegap.PreferenceNode("fullscreen", "false", false);
            foo.name = "widescreen";
            assertEquals("widescreen", foo.name);
        }

    @Test
        public void testValueAssignment() {
            PreferenceNode foo = new com.phonegap.PreferenceNode("fullscreen", "false", false);
            foo.value = "maybe";
            assertEquals("maybe", foo.value);
        }

    @Test
        public void testReadonlyAssignment() {
            PreferenceNode foo = new com.phonegap.PreferenceNode("fullscreen", "false", false);
            foo.readonly = true;
            assertEquals(true, foo.readonly);
        }
}
