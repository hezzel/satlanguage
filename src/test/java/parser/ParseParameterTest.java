package logic.parameter;

import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.PExpression;
import logic.parameter.Parameter;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParseParameterTest {
  @Test
  public void testReadSimpleParameter() {
    try {
      Parameter p = InputReader.readParameterFromString("i ∈ { 1..9 }");
      assertTrue(p.queryName().equals("i"));
      assertTrue(p.queryMinimum().evaluate(null) == 1);
      assertTrue(p.queryMaximum().evaluate(null) == 9);
      assertTrue(p.queryRestriction().isTop());
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadComplicatedParameter() {
    try {
      Parameter p = InputReader.readParameterFromString("c1 ∈ {j+ 1..9-h* 3} with c1> h ∧h != 12");
      assertTrue(p.queryName().equals("c1"));
      assertTrue(p.queryMinimum().toString().equals("j+1"));
      assertTrue(p.queryMaximum().queryKind() == PExpression.SUM);
      assertTrue(p.queryMaximum().toString().equals("9+-1*h*3"));
      assertTrue(p.queryRestriction().toString().equals("h < c1 ∧ h ≠ 12"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testIllegalParameterName() {
    try {
      Parameter p = InputReader.readParameterFromString("i^ ∈ {j+ 1..9-h* 3} with j> h ∧h != 12");
    }
    catch (ParserException exc) {
      return;
    }
    assertTrue("Expected an exception on parameter name i^", false);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testParameterWithParamvar() throws ParserException {
    InputReader.readParameterFromString("i ∈ {1..x[1,2]}");
  }
}

