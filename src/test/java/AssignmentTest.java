import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.Assignment;
import java.util.Set;

public class AssignmentTest {
  @Test
  public void testAddContainsQuery() {
    Assignment ass = new Assignment();
    ass.put("hello", 4);
    assertTrue(ass.defines("hello"));
    assertTrue(ass.get("hello") == 4);
  }

  @Test
  public void testAddRemove() {
    Assignment ass = new Assignment();
    ass.put("hello", 4);
    assertTrue(ass.defines("hello"));
    ass.remove("hello");
    assertFalse(ass.defines("hello"));
  }

  @Test(expected = java.lang.Error.class)
  public void testDoubleRemove() {
    Assignment ass = new Assignment();
    ass.put("hello", 4);
    ass.remove("hello");
    ass.remove("hello");
  }

  @Test
  public void testRemoveOther() {
    Assignment ass = new Assignment("a", 1, "b", 0);
    ass.remove("a");
    assertTrue(ass.defines("b"));
    assertFalse(ass.defines("a"));
  }

  @Test(expected = java.lang.Error.class)
  public void testBadLookup() {
    Assignment ass = new Assignment("a", 0);
    ass.get("b");
  }

  @Test
  public void testKeys() {
    Assignment ass = new Assignment("a", 1, "b", 0);
    ass.put("c", 45);
    Set<String> keys = ass.queryKeys();
    assertTrue(keys.size() == 3);
    assertTrue(keys.contains("a"));
    assertTrue(keys.contains("b"));
    assertTrue(keys.contains("c"));
  }
}

