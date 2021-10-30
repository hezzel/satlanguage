import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.Match;
import logic.parameter.Function;
import java.util.ArrayList;

public class FunctionTest {
  @Test
  public void testMatchBasics() {
    Match m = new Match(1, null, 7);
    assertTrue(m.length() == 3);
    assertTrue(m.toString().equals("(1,_,7)"));
    ArrayList<Integer> arr = new ArrayList<Integer>();
    arr.add(1);
    arr.add(3);
    arr.add(7);
    assertTrue(m.isMatch(arr));
    arr.set(2, 8);
    assertFalse(m.isMatch(arr));
  }
}

