import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;

public class VariableTest {
  @Test
  public void testComparison() {
    Variable v1 = new Variable("v");
    Variable v2 = new Variable("v");
    Variable w = new Variable("w");

    assertTrue(v1.compareTo(v2) == 0);
    assertTrue(v1.compareTo(w) == -1);
    assertTrue(w.compareTo(v2) == 1);
    assertTrue(v1.equals(v2));
    assertFalse(v1.equals(w));
  }

  @Test
  public void testVariableEquality() {
    Variable x = new Variable("x");
    Variable y1 = new Variable("y");
    Variable y2 = new Variable("y");
    assertFalse(x.equals(y1));
    assertTrue(y2.equals(y1));
  }

  @Test
  public void testFreshReallyFresh() {
    Variable x = new Variable(Variable.generateFresh());
    Variable y = new Variable(Variable.generateFresh());
    assertFalse(x.equals(y));
    Variable z = new Variable("_var" + (y.queryIndex() + 2));
    assertFalse(z.generateFresh().equals(z.queryIndex()));
  }

  @Test
  public void testEverythingGoneAfterReset() {
    Variable.reset();
    Variable x = new Variable("x");
    Variable y = new Variable("y");
    Variable.reset();
    Variable z = new Variable("y");
    assertTrue(x.equals(z));
    assertFalse(y.equals(z));
  }

  @Test
  public void testIndex() {
    Variable.reset();
    Variable x = new Variable("x");
    assertTrue(x.queryIndex() > 0);
  }
}

