import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.parameter.PExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.range.QuantifiedRangeInteger;
import logic.range.QuantifiedRangeConstant;
import logic.range.RangeConstant;
import language.parser.InputReader;
import language.parser.ParserException;
import java.util.Set;

public class QRangeConstantTest {
  private PExpression expr(String txt) {
    try { return InputReader.readPExpressionFromString(txt); }
    catch (ParserException e) { return null; }
  }

  private QuantifiedRangeConstant createConstant() {
    return new QuantifiedRangeConstant(expr("a + b - 3 * a"),
                                       new Variable("FALSE"), new Variable("TRUE"));
  }

  @Test
  public void testToString() {
    assertTrue(createConstant().toString().equals("a+b-3*a"));
  }

  @Test
  public void testParameters() {
    Set<String> params = createConstant().queryParameters();
    assertTrue(params.size() == 2);
    assertTrue(params.contains("a"));
    assertTrue(params.contains("b"));
  }

  @Test
  public void testClosed() {
    assertFalse(createConstant().queryClosed());
    QuantifiedRangeConstant c = new QuantifiedRangeConstant(expr("1"),
                                                            new Variable("FALSE"),
                                                            new Variable("TRUE"));
    assertTrue(c.queryClosed());
  }

  @Test
  public void testSubstitute() {
    Substitution subst = new Substitution("a", expr("a * c"));
    QuantifiedRangeInteger c = createConstant().substitute(subst);
    assertTrue(c.toString().equals("a*c+b-3*a*c"));
    assertTrue(c.queryParameters().size() == 3);
  }

  @Test
  public void testInstantiate() {
    Assignment ass = new Assignment("a", 1, "b", 3);
    RangeConstant c = createConstant().instantiate(ass);
    assertTrue(c.queryMinimum() == 1);
    assertTrue(c.queryMaximum() == 1);
  }

  @Test
  public void testNullInstantiate() {
    QuantifiedRangeConstant q = new QuantifiedRangeConstant(expr("1+2"),
                                             new Variable("FALSE"), new Variable("TRUE"));
    RangeConstant c = q.instantiate(null);
    assertTrue(c.queryMinimum() == 3);
    assertTrue(c.queryMaximum() == 3);
  }
}

