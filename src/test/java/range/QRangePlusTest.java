import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.parameter.PExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.range.*;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;
import java.util.Set;

public class QRangePlusTest {
  private PExpression expr(String txt) {
    try { return InputReader.readPExpressionFromString(txt); }
    catch (ParserException e) { return null; }
  }

  private QuantifiedRangeVariable createVariable() {
    try {
      VariableList lst = new VariableList();
      InputReader.declare("qrpt[i] :: Int ∈ {0..i} for i ∈ {1..10}", lst);
      ParamRangeVar qrpt = lst.queryParametrisedRangeVariable("qrpt");
      return new QuantifiedRangeVariable(qrpt, new Substitution());
    } catch (ParserException e) { return null; }
  }

  private QuantifiedRangeConstant createConstant(String txt) {
    return new QuantifiedRangeConstant(expr(txt), new Variable("FALSE"), new Variable("TRUE"));
  }

  private QuantifiedRangePlus createPlus(String txt) {
    return new QuantifiedRangePlus(createConstant(txt), createVariable());
  }

  @Test
  public void testToString() {
    assertTrue(createPlus("j + 1").toString().equals("j+1 ⊕ qrpt[i]"));
  }

  @Test
  public void testParameters() {
    Set<String> params = createPlus("i + j -1").queryParameters();
    assertTrue(params.size() == 2);
    assertTrue(params.contains("i"));
    assertTrue(params.contains("j"));
  }

  @Test
  public void testNotClosed() {
    assertFalse(createPlus("1").queryClosed());
  }

  @Test
  public void testClosed() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("x[i] :: Int ∈ {0..10} for i ∈ {1..5}", lst);
    ParamRangeVar x = lst.queryParametrisedRangeVariable("x");
    Substitution subst = new Substitution("i", expr("7"));
    QuantifiedRangeVariable qrv = new QuantifiedRangeVariable(x, subst);
    QuantifiedRangePlus plus = new QuantifiedRangePlus(qrv, createConstant("3"));
    assertTrue(plus.queryClosed());
  }

  @Test
  public void testSubstitute() {
    Substitution subst = new Substitution("i", expr("c * d"), "j", expr("a+1"));
    QuantifiedRangeInteger c = createPlus("1+j").substitute(subst);
    assertTrue(c.toString().equals("a+2 ⊕ qrpt[c*d]"));
    assertTrue(c.queryParameters().size() == 3);
  }

  @Test
  public void testInstantiateOneSideConstant() {
    Assignment ass = new Assignment("i", 3, "j", -1);
    RangeInteger c = createPlus("i+j").instantiate(ass);
    assertTrue(c.toString().equals("(qrpt[3] ⊕2)"));  // no space before the 2, because it's a shift
    assertTrue(c instanceof RangeShift);
    assertTrue(c.queryMinimum() == 2);
    assertTrue(c.queryMaximum() == 5);
  }

  @Test
  public void testInstantiateBothSidesConstant() {
    QuantifiedRangePlus p = new QuantifiedRangePlus(createConstant("i+1"), createConstant("i"));
    Assignment ass = new Assignment("i", 8);
    RangeInteger c = p.instantiate(ass);
    assertTrue(c instanceof RangeConstant);
    assertTrue(c.queryMinimum() == 17);
  }

  @Test
  public void testInstantiateOneSideConstantVariable() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("x[i] :: Int ∈ {1..i} for i ∈ {1..10}", lst);
    ParamRangeVar x = lst.queryParametrisedRangeVariable("x");
    QuantifiedRangeVariable x1 = new QuantifiedRangeVariable(x, new Substitution());
    QuantifiedRangeVariable x2 = new QuantifiedRangeVariable(x, new Substitution("i", expr("j")));
    QuantifiedRangePlus p = new QuantifiedRangePlus(x1, x2);
    Assignment ass = new Assignment("i", 3, "j", 1);
    RangeInteger i = p.instantiate(ass);
    assertTrue(i.toString().equals("(x[3] ⊕1)"));
    assertTrue(i instanceof RangeShift);
  }

  @Test
  public void testInstantiateNeitherSideConstant() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("x[i] :: Int ∈ {1..i} for i ∈ {1..10}", lst);
    ParamRangeVar x = lst.queryParametrisedRangeVariable("x");
    QuantifiedRangeVariable x1 = new QuantifiedRangeVariable(x, new Substitution());
    QuantifiedRangeVariable x2 = new QuantifiedRangeVariable(x, new Substitution("i", expr("j")));
    QuantifiedRangePlus p = new QuantifiedRangePlus(x1, x2);
    Assignment ass = new Assignment("i", 3, "j", 7);
    RangeInteger i = p.instantiate(ass);
    assertTrue(i.toString().equals("bplus(2, 10, x[3] ⊕ x[7])"));
    assertTrue(i instanceof RangePlus);
  }
}

