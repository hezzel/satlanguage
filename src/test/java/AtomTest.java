import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;

public class AtomTest {
  private Variable x() {
    return new Variable("x");
  }

  private Variable y() {
    return new Variable("y");
  }

  @Test
  public void testBasics() {
    Atom xtrue = new Atom(x(), true);
    Atom xfalse = new Atom(x(), false);
    assertTrue(xtrue.queryIndex() == x().queryIndex());
    assertTrue(xfalse.queryIndex() == x().queryIndex());
    assertFalse(xtrue.queryNegative());
    assertTrue(xfalse.queryNegative());
    assertFalse(xtrue.getSatDescription().equals(xfalse.getSatDescription()));
    assertTrue(xtrue.toString().equals("x"));
    assertTrue(xfalse.toString().equals("¬x"));
  }

  @Test
  public void testComparison() {
    Atom xtrue = new Atom(x(), true);
    Atom xfalse = new Atom(x(), false);
    Atom ytrue = new Atom(y(), true);
    Atom yfalse = new Atom(y(), false);
    assertTrue(xtrue.compareTo(xfalse) == -1);
    assertTrue(yfalse.compareTo(ytrue) == 1);
    assertTrue(xtrue.compareTo(yfalse) == x().compareTo(y()));
    assertTrue(ytrue.compareTo(xfalse) == y().compareTo(x()));
  }

  @Test
  public void testDoubleNegation() {
    Atom xt = new Atom(x(), true);
    Atom xf = new Atom(x(), false);

    assertTrue(xt.negate().equals(xf));
    assertTrue(xf.negate().equals(xt));
    assertTrue(xt.negate().negate().equals(xt));
  }
}

