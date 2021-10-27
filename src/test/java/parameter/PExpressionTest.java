import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.*;
import java.util.Set;
import java.util.TreeMap;

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

  @Test
  public void testMultiply() {
    assertTrue(createExpr().multiply(0).equals(new ConstantExpression(0)));
    assertTrue(createExpr().multiply(1).equals(createExpr()));
    assertTrue((new ParameterExpression("p")).multiply(1).equals(new ParameterExpression("p")));
    assertTrue(createExpr().multiply(-2).toString().equals("-2-2*a-2*b-2*a*3"));
  }

  @Test
  public void testDoubleNegationMultiply() {
    PExpression e = (new ParameterExpression("i")).multiply(-1);
    assertTrue(e.toString().equals("-i"));
    e = e.multiply(-1);
    assertTrue(e.equals(new ParameterExpression("i")));
  }

  private PExpression createDivision() {
    return new DivExpression(
             new ProductExpression(new ParameterExpression("a"),
                                   new ParameterExpression("b")),
             new ProductExpression(new ParameterExpression("c"),
                                   new ConstantExpression(2)));
  }

  @Test
  public void testDivisionBasics() {
    PExpression e = createDivision();
    assertTrue(e.queryKind() == PExpression.DIVISION);
    assertTrue(e.queryLeft().toString().equals("a*b"));
    assertTrue(e.queryRight().toString().equals("c*2"));
    assertTrue(e.toString().equals("(a*b)/(c*2)"));
    assertFalse(e.queryConstant());
    assertTrue(e.queryParameters().size() == 3);
    assertTrue(e.equals(createDivision()));
    assertTrue(e.add(5).toString().equals("(a*b)/(c*2)+5"));
    assertTrue(e.multiply(5).toString().equals("5*((a*b)/(c*2))"));
  }

  @Test
  public void testDivisionSubstituteAssign() {
    PExpression e = createDivision();
    Assignment ass = new Assignment("a", 2, "b", 3, "c", 2);
    assertTrue(e.evaluate(ass) == 1);
    ass = new Assignment("a", 2, "b", 3, "c", 1);
    assertTrue(e.evaluate(ass) == 3);
    Substitution subst = new Substitution("b", new ConstantExpression(2));
    assertTrue(e.substitute(subst).toString().equals("(2*a)/(2*c)"));
  }

  @Test
  public void testNestedDivision() {
    PExpression e = new DivExpression(createDivision(), new ConstantExpression(2));
    assertTrue(e.toString().equals("((a*b)/(c*2))/2"));
  }

  @Test
  public void testModuloToString() {
    PExpression m = new ModExpression(
                      new ProductExpression(new ParameterExpression("a"),
                                            new ParameterExpression("b")),
                      new ConstantExpression(2));
    assertTrue(m.toString().equals("(a*b)%2"));
    assertTrue(m.multiply(2).toString().equals("2*((a*b)%2)"));
  }

  @Test
  public void testMinimumBasics() {
    PExpression m = new MinExpression(new ParameterExpression("a"), new ParameterExpression("b"));
    assertTrue(m.queryKind() == PExpression.MINIMUM);
    assertTrue(m.evaluate(new Assignment("a", 3, "b", 4)) == 3);
    assertTrue(m.toString().equals("min(a,b)"));
  }

  @Test
  public void testMaximumBasics() {
    PExpression m = new MaxExpression(
                      new ProductExpression(new ParameterExpression("a"),
                                            new ConstantExpression(2)),
                      new ParameterExpression("b"));
    assertTrue(m.queryKind() == PExpression.MAXIMUM);
    assertTrue(m.evaluate(new Assignment("a", 1, "b", 4)) == 4);
    assertTrue(m.toString().equals("max(a*2,b)"));
  }

  @Test
  public void testFunctionExpressionBasics() {
    TreeMap<Integer,Integer> map = new TreeMap<Integer,Integer>();
    map.put(0, 0);
    Function f = new Function("f", map, "i", (new ParameterExpression("i")).add(-1));
    PExpression expr = new SumExpression(new ParameterExpression("a"),
                          new ModExpression(new ParameterExpression("b"),
                                            new ConstantExpression(2)));
    FunctionExpression fe = new FunctionExpression(f, expr);

    assertTrue(fe.queryKind() == PExpression.FUNCTION);
    assertTrue(fe.queryLeft() == null);
    assertTrue(fe.queryRight().equals(expr));
    assertFalse(fe.queryConstant());
    assertTrue(fe.toString().equals("f(a+b%2)"));
    assertTrue(fe.equals(new FunctionExpression(f, expr)));

    Assignment ass = new Assignment("a", 3, "b", 4);
    assertTrue(fe.evaluate(ass) == 2);
    ass = new Assignment("a", -1, "b", 1);
    assertTrue(fe.evaluate(ass) == 0);

    Substitution subst = new Substitution("a", new ParameterExpression("b"));
    assertTrue(fe.substitute(subst).toString().equals("f(b+b%2)"));
  }
}

