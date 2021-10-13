import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.*;
import java.util.Set;

public class PExpressionTest {
  private PExpression createExpr() {
    // (1+a) + (b+a*3)
    return new SumExpression(
             new SumExpression(
               new ConstantExpression(1),
               new ParameterExpression("a")
             ),
             new SumExpression(
               new ParameterExpression("b"),
               new ProductExpression(
                 new ParameterExpression("a"),
                 new ConstantExpression(3)
               )
             )
           );
  }

  private PExpression createProductExpr() {
    // (a * (2 * b) + (0 * i + (1 + c) * 1)
    return new SumExpression(
        new ProductExpression(
          new ParameterExpression("a"),
          new ProductExpression(
            new ConstantExpression(2),
            new ParameterExpression("b")
          )
        ),
        new SumExpression(
          new ProductExpression(
            new ConstantExpression(0),
            new ParameterExpression("i")
          ),
          new ProductExpression(
            new SumExpression(
              new ConstantExpression(1),
              new ParameterExpression("c")
            ),
            new ConstantExpression(1)
          )
        )
      );
  }

  @Test
  public void testToStringSum() {
    assertTrue(createExpr().toString().equals("1+a+b+a*3"));
  }

  @Test
  public void testToStringProduct() {
    assertTrue(createProductExpr().toString().equals("a*2*b+0*i+(1+c)*1"));
  }

  @Test(expected = java.lang.Error.class)
  public void testEvaluateIncomplete() {
    Assignment ass = new Assignment("a", 12);
    createExpr().evaluate(ass);
  }

  @Test
  public void testEvaluateComplete() {
    Assignment ass = new Assignment("a", 12);
    ass.put("b", 9);
    assertTrue(createExpr().evaluate(ass) == 58);
  }

  @Test
  public void testSimplify() {
    PExpression e = createExpr().substitute(new Substitution());
    assertTrue(e.toString().equals("a+b+3*a+1"));
  }

  @Test
  public void testSimplifyProduct() {
    PExpression e = createProductExpr().substitute(new Substitution());
    assertTrue(e.toString().equals("2*a*b+c+1"));
  }

  @Test
  public void testSubstituteSimple() {
    Assignment ass = new Assignment("a", 12);
    PExpression e = createExpr().substitute(new Substitution(ass));
    assertTrue(e.toString().equals("b+49"));
  }

  @Test
  public void testSubstituteSubstitution() {
    Substitution subst = new Substitution("a", new SumExpression(new ParameterExpression("a"),
                                                                 new ParameterExpression("b")));
    PExpression e = createExpr().substitute(subst);
    assertTrue(e.toString().equals("a+b+b+3*(a+b)+1"));
  }

  @Test
  public void testSubstituteProduct() {
    // b := 7 * (c+1)
    Substitution subst = new Substitution("b", new ProductExpression(
        new ConstantExpression(7),
        new SumExpression(new ParameterExpression("c"), new ConstantExpression(2))));
    PExpression e = createProductExpr().substitute(subst);
    assertTrue(e.toString().equals("14*a*(c+2)+c+1"));
  }

  @Test
  public void testNotQuiteAConstant() {
    PExpression e = new SumExpression(new ConstantExpression(1), new ConstantExpression(0));
    assertFalse(e.queryConstant());
  }

  @Test
  public void testQueryParameters() {
    Set<String> params = createProductExpr().queryParameters();
    assertTrue(params.contains("a"));
    assertTrue(params.contains("b"));
    assertTrue(params.contains("c"));
    assertTrue(params.contains("i"));
    assertTrue(params.size() == 4);
  }

  @Test
  public void testEquals() {
    assertTrue(createExpr().equals(createExpr()));
  }

  @Test
  public void testNotEquals() {
    PExpression a = new ParameterExpression("a");
    PExpression b = new ParameterExpression("b");
    PExpression c = new ParameterExpression("c");
    PExpression expr1 = new SumExpression(a, new SumExpression(b, c));
    PExpression expr2 = new SumExpression(new SumExpression(a, b), c);
    assertFalse(expr1.equals(expr2));
  }

  @Test
  public void testAdd() {
    assertTrue(createExpr().add(1).toString().equals("1+a+b+a*3+1"));
    assertTrue((new ParameterExpression("p")).add(0).toString().equals("p"));
    assertTrue(createExpr().substitute(new Substitution()).add(2).toString().equals("a+b+3*a+3"));
  }
}

