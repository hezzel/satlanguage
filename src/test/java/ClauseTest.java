import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;

import java.util.ArrayList;

public class ClauseTest {
  @Test
  public void testHighest() {
    Variable.reset();
    Variable x = new Variable("x");
    Variable y = new Variable("y");
    Variable z = new Variable("z");
    ArrayList<Atom> arr = new ArrayList<Atom>();
    arr.add(new Atom(x, true));
    arr.add(new Atom(z, false));
    arr.add(new Atom(y, false));
    Clause c = new Clause(arr);
    assertTrue(c.getHighestAtomIdentifier() == z.queryIndex());
  }

  @Test
  public void testNotEqual() {
    Clause c = new Clause(new Atom(new Variable("x"), true),
                          new Atom(new Variable("y"), false),
                          new Atom(new Variable("z"), true));
    Clause d = new Clause(new Atom(new Variable("x"), true),
                          new Atom(new Variable("y"), true),
                          new Atom(new Variable("z"), true));
    assertFalse(c.equals(d));
  }

  @Test
  public void testEqualityDifferentOrder() {
    Clause c = new Clause(new Atom(new Variable("x"), true),
                          new Atom(new Variable("y"), false),
                          new Atom(new Variable("z"), true));
    Clause d = new Clause(new Atom(new Variable("y"), false),
                          new Atom(new Variable("z"), true),
                          new Atom(new Variable("x"), true));
    assertTrue(c.equals(d));
  }

  @Test
  public void testAlternativeCreation() {
    Clause c = new Clause(new Atom(new Variable("x"), true),
                          new Atom(new Variable("y"), false),
                          new Atom(new Variable("z"), true));
    Clause d = new Clause(new Atom(new Variable("x"), true),
                          new Clause(
                            new Atom(new Variable("y"), false),
                            new Atom(new Variable("z"), true)
                          ));
    assertTrue(c.equals(d));
  }
}

