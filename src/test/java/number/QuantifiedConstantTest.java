import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.parameter.PExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.number.QuantifiedInteger;
import logic.number.QuantifiedConstant;
import logic.number.ConstantInteger;
import language.parser.InputReader;
import language.parser.ParserException;
import java.util.Set;

public class QuantifiedConstantTest {
  private Atom truth() {
    return new Atom(new Variable("TRUE"), true);
  }

  private PExpression expr(String txt) {
    try { return InputReader.readPExpressionFromString(txt); }
    catch (ParserException e) { return null; }
  }

  private QuantifiedConstant createConstant() {
    return new QuantifiedConstant(expr("a + b - 3 * a"), truth());
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
    QuantifiedConstant c = new QuantifiedConstant(expr("1"), truth());
    assertTrue(c.queryClosed());
  }

  @Test
  public void testSubstitute() {
    Substitution subst = new Substitution("a", expr("a * c"));
    QuantifiedInteger c = createConstant().substitute(subst);
    assertTrue(c.toString().equals("a*c+b-3*a*c"));
    assertTrue(c.queryParameters().size() == 3);
  }

  @Test
  public void testInstantiate() {
    Assignment ass = new Assignment("a", 1, "b", 3);
    ConstantInteger c = createConstant().instantiate(ass);
    assertTrue(c.queryMinimum() == 1);
    assertTrue(c.queryMaximum() == 1);
  }

  @Test
  public void testNullInstantiate() {
    QuantifiedConstant q = new QuantifiedConstant(expr("1+2"), truth());
    ConstantInteger c = q.instantiate(null);
    assertTrue(c.queryMinimum() == 3);
    assertTrue(c.queryMaximum() == 3);
  }
}

