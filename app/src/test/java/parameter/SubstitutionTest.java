import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.*;

public class SubstitutionTest {
  @Test
  public void testAddContainsQuery() {
    Substitution subst = new Substitution();
    PExpression expr = new ParameterExpression("i");
    subst.put("hello", expr);
    assertTrue(subst.get("hello").equals(expr));
  }

  @Test
  public void testAddRemove() {
    Substitution subst = new Substitution("hello", new ConstantExpression(4));
    assertTrue(subst.get("hello") != null);
    subst.remove("hello");
    assertTrue(subst.get("hello") == null);
  }

  @Test
  public void testDoubleRemove() {
    Substitution subst = new Substitution("hello", new ConstantExpression(4));
    subst.remove("hello");
    subst.remove("hello");
    assertTrue(subst.get("hello") == null);
  }

  @Test
  public void testRemoveOther() {
    Substitution subst = new Substitution("a", new ConstantExpression(1),
                                          "b", new ParameterExpression("bing"));
    subst.remove("a");
    assertTrue(subst.get("b").toString().equals("bing"));
    assertTrue(subst.get("a") == null);
  }

  @Test
  public void testNeverExistingLookup() {
    Substitution subst = new Substitution("a", new ConstantExpression(0));
    assertTrue(subst.get("b") == null);
  }

  @Test
  public void testCreateFromAssignment() {
    Assignment ass = new Assignment("a", 1, "b", 0);
    ass.put("c", 0);
    Substitution subst = new Substitution(ass);
    assertTrue(subst.get("a").equals(new ConstantExpression(1)));
    assertTrue(subst.get("b").equals(new ConstantExpression(0)));
    assertTrue(subst.get("c").equals(new ConstantExpression(0)));
  }
}

