import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.*;

public class ParamTreeTest {
  private ParamTree<String> makeTree() {
    PExpression exp1 = new SumExpression(new ParameterExpression("a"), new ConstantExpression(1));
    PExpression exp2 = new ParameterExpression("c");
    Parameter a = new Parameter("a", 0, 3);
    Parameter b = new Parameter("b", new ConstantExpression(2),
      new SumExpression(new ParameterExpression("a"), new ConstantExpression(2)),
      new TrueConstraint());
    Parameter c = new Parameter("c", 0, 5, new SmallerConstraint(exp1, exp2));
    ParameterList params = new ParameterList(a, b, c);
    // [a,b,c] with 0 ≤ a ≤ 3 and 2 ≤ b ≤ a + 2 and 0 ≤ c ≤ 5 and a + 1 < c

    ParamTree.ConstructorHelper<String> helper = new ParamTree.ConstructorHelper<String>() {
      public String generate(Assignment args) {
        return "[" + args.get("a") + "," + args.get("b") + "," + args.get("c") + "]";
      }
    };

    return new ParamTree<String>(params, helper);
  }

  @Test
  public void testValidLookup() {
    ParamTree<String> tree = makeTree();
    Assignment ass = new Assignment();
    // in the middle
    ass.put("a", 2);
    ass.put("b", 3);
    ass.put("c", 4);
    assertFalse(tree.lookup(ass) == null);
    assertTrue(tree.lookup(ass).equals("[2,3,4]"));
    // on the edges
    ass.put("a", 0);
    ass.put("b", 2);
    ass.put("c", 5);
    assertFalse(tree.lookup(ass) == null);
    assertTrue(tree.lookup(ass).equals("[0,2,5]"));
  }

  @Test
  public void testLookupAssignmentOutOfRange() {
    ParamTree<String> tree = makeTree();
    Assignment ass = new Assignment();
    ass.put("a", 2);
    ass.put("b", 4);
    ass.put("c", 6);
    assertTrue(tree.lookup(ass) == null);
    ass.put("a", -1);
    ass.put("b", 4);
    ass.put("c", 1);
    assertTrue(tree.lookup(ass) == null);
  }

  @Test
  public void testLookupAssignmentViolatingRestriction() {
    ParamTree<String> tree = makeTree();
    Assignment ass = new Assignment();
    ass.put("a", 1);
    ass.put("b", 2);
    ass.put("c", 2);
    assertTrue(tree.lookup(ass) == null);
  }

  @Test
  public void testLookupAssignmentViolatingExpressionBoundary() {
    ParamTree<String> tree = makeTree();
    Assignment ass = new Assignment();
    ass.put("a", 1);
    ass.put("b", 5);
    ass.put("c", 3);
    assertTrue(tree.lookup(ass) == null);
  }

  @Test(expected = java.lang.Error.class)
  public void testLookupIncompleteAssignment() {
    ParamTree<String> tree = makeTree();
    Assignment ass = new Assignment();
    ass.put("a", 2);
    ass.put("c", 5);
    tree.lookup(ass);
  }

  @Test
  public void testValidSet() {
    ParamTree<String> tree = makeTree();
    Assignment ass = new Assignment();
    ass.put("a", 3);
    ass.put("b", 4);
    ass.put("c", 5);
    tree.set(ass, "Hello world!");
    assertTrue(tree.lookup(ass) != null);
    assertTrue(tree.lookup(ass).equals("Hello world!"));
    ass.put("b", 2);
    assertTrue(tree.lookup(ass).equals("[3,2,5]"));
  }

  @Test(expected = java.lang.Error.class)
  public void testSetIllegalAssignment() {
    ParamTree<String> tree = makeTree();
    Assignment ass = new Assignment();
    ass.put("a", 2);
    ass.put("b", 5);
    ass.put("c", 4);
    tree.set(ass, "Hello");
  }

  @Test(expected = java.lang.Error.class)
  public void setIncompleteAssignment() {
    ParamTree<String> tree = makeTree();
    Assignment ass = new Assignment();
    ass.put("a", 2);
    ass.put("b", 3);
    tree.set(ass, "Bing");
  }
}

