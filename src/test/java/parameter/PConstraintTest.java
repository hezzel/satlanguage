import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.*;
import java.util.Set;

public class PConstraintTest {
  private PConstraint createConstr() {
    // 1 + a < b + 3 ∧ (b != c ∨ a != c)
    return new AndConstraint(
      new SmallerConstraint(
        new SumExpression(new ConstantExpression(1), new ParameterExpression("a")),
        new SumExpression(new ParameterExpression("b"), new ConstantExpression(3))
      ),
      new OrConstraint(
        new NeqConstraint(
          new ParameterExpression("b"),
          new ParameterExpression("c")
        ),
        new NeqConstraint(
          new ParameterExpression("a"),
          new ParameterExpression("c")
        )
      )
    );
  }

  @Test
  public void testToString() {
    assertTrue(createConstr().toString().equals("1+a < b+3 ∧ (b ≠ c ∨ a ≠ c)"));
  }

  @Test(expected = java.lang.Error.class)
  public void testEvaluateIncomplete() {
    Assignment ass = new Assignment("a", 12, "c", 3);
    createConstr().evaluate(ass);
  }

  @Test
  public void testEvaluateComplete() {
    Assignment ass = new Assignment("a", 12, "b", 10, "c", 12);
    assertFalse(createConstr().evaluate(ass));
    ass = new Assignment("a", 12, "b", 11, "c", 12);
    assertTrue(createConstr().evaluate(ass));
  }

  @Test
  public void testSubstituteSimple() {
    Assignment ass = new Assignment("a", 12);
    PConstraint c = createConstr().substitute(new Substitution(ass));
    assertTrue(c.toString().equals("13 < b+3 ∧ (b ≠ c ∨ 12 ≠ c)"));
  }

  @Test
  public void testToStringNoRedundantBrackets() {
    PConstraint rel = new SmallerConstraint(new ParameterExpression("i"),
                                            new ConstantExpression(5));
    PConstraint c = new AndConstraint(rel, new AndConstraint(rel, rel));
    assertTrue(c.toString().equals("i < 5 ∧ i < 5 ∧ i < 5"));
    c = new OrConstraint(new OrConstraint(rel, rel), rel);
    assertTrue(c.toString().equals("i < 5 ∨ i < 5 ∨ i < 5"));
  }

  @Test
  public void testSubstitute() {
    Substitution subst = new Substitution("a", new ConstantExpression(12),
                                          "b", new ParameterExpression("a"));
    PConstraint c = createConstr().substitute(subst);
    assertTrue(c.toString().equals("13 < a+3 ∧ (a ≠ c ∨ 12 ≠ c)"));
  }

  @Test
  public void testNotQuiteAConstant() {
    PConstraint c = new SmallerConstraint(new ConstantExpression(0), new ConstantExpression(1));
    assertFalse(c.isTop());
  }

  @Test
  public void testSubstituteToConstant() {
    Substitution subst = new Substitution(new Assignment("a", 12, "b", 11, "c", 12));
    assertTrue(createConstr().substitute(subst).isTop());
  }

  @Test
  public void testSubstituteOrPart() {
    Substitution subst = new Substitution("a", new ConstantExpression(1),
                                          "c", new ConstantExpression(0));
    assertTrue(createConstr().substitute(subst).toString().equals("2 < b+3"));
  }

  @Test
  public void testQueryParameters() {
    Set<String> params = createConstr().queryParameters();
    assertTrue(params.contains("a"));
    assertTrue(params.contains("b"));
    assertTrue(params.contains("c"));
    assertTrue(params.size() == 3);
  }
}

