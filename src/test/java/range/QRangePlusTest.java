import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.parameter.PExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.number.*;
import logic.number.range.*;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;
import java.util.Set;
import java.util.ArrayList;

public class QRangePlusTest {
  private PExpression expr(String txt) {
    try { return InputReader.readPExpressionFromString(txt); }
    catch (ParserException e) { return null; }
  }

  private QuantifiedRangeVariable createVariable(String index) {
    try {
      VariableList lst = new VariableList();
      InputReader.declare("qrpt[i] :: Int ∈ {0..i} for i ∈ {1..10}", lst);
      ParamRangeVar qrpt = lst.queryParametrisedRangeVariable("qrpt");
      PExpression par = InputReader.readPExpressionFromString(index);
      return new QuantifiedRangeVariable(qrpt, new Substitution("i", par));
    } catch (ParserException e) { return null; }
  }

  private QuantifiedRangeConstant createConstant(String txt) {
    return new QuantifiedRangeConstant(expr(txt), new Atom(new Variable("TRUE"), true));
  }

  /** Creates x[i] + <the expression given by txt> */
  private QuantifiedRangePlus createBinaryPlus(String txt) {
    return new QuantifiedRangePlus(createConstant(txt), createVariable("i"));
  }
  
  /** Creates { x[i] , x[j] } */
  private ArrayList<QuantifiedRangeInteger> createParts(String i, String j) {
    ArrayList<QuantifiedRangeInteger> ret = new ArrayList<QuantifiedRangeInteger>();
    ret.add(createVariable(i));
    ret.add(createVariable(j));
    return ret;
  }

  @Test
  public void testSimpleToString() {
    assertTrue(createBinaryPlus("j + 1").toString().equals("j+1 ⊕ qrpt[i]"));
  }

  @Test
  public void testVarplusToString() {
    QuantifiedRangeInteger ri = new QuantifiedRangePlus(createParts("i", "j"));
    assertTrue(ri.toString().equals("qrpt[i] ⊕ qrpt[j]"));
  }

  @Test
  public void testTripleVarplusToString() {
    ArrayList<QuantifiedRangeInteger> parts = createParts("i", "i+1");
    parts.add(createVariable("j"));
    QuantifiedRangeInteger ri = new QuantifiedRangePlus(parts);
    assertTrue(ri.toString().equals("qrpt[i] ⊕ qrpt[i+1] ⊕ qrpt[j]"));
  }

  @Test
  public void testParameters() {
    Set<String> params = createBinaryPlus("i + j -1").queryParameters();
    assertTrue(params.size() == 2);
    assertTrue(params.contains("i"));
    assertTrue(params.contains("j"));
  }

  @Test
  public void testNotClosedWithPExpression() {
    assertFalse(createBinaryPlus("1").queryClosed());
  }

  @Test
  public void testNotClosedWithVariables() throws ParserException {
    ArrayList<QuantifiedRangeInteger> parts = createParts("1", "j");
    parts.add(createVariable("5"));
    QuantifiedRangeInteger ri = new QuantifiedRangePlus(parts);
    assertFalse(ri.queryClosed());
  }

  @Test
  public void testClosed() throws ParserException {
    ArrayList<QuantifiedRangeInteger> parts = createParts("1", "7");
    parts.add(createVariable("5"));
    QuantifiedRangeInteger ri = new QuantifiedRangePlus(parts);
    assertTrue(ri.queryClosed());
  }

  @Test
  public void testSubstitute() {
    Substitution subst = new Substitution("i", expr("c * d"), "j", expr("a+1"));
    QuantifiedRangeInteger c = createBinaryPlus("1+j").substitute(subst);
    assertTrue(c.toString().equals("a+2 ⊕ qrpt[c*d]"));
    assertTrue(c.queryParameters().size() == 3);
  }

  @Test
  public void testInstantiateOneSideConstant() {
    Assignment ass = new Assignment("i", 3, "j", -1);
    RangeInteger c = createBinaryPlus("i+j").instantiate(ass);
    assertTrue(c.toString().equals("(qrpt[3] ⊕2)"));  // no space before the 2, because it's a shift
    assertTrue(c instanceof RangeShift);
    assertTrue(c.queryMinimum() == 2);
    assertTrue(c.queryMaximum() == 5);
  }

  @Test
  public void testInstantiateThreeSidesConstant() {
    ArrayList<QuantifiedRangeInteger> parts = new ArrayList<QuantifiedRangeInteger>();
    parts.add(createConstant("i+1"));
    parts.add(createConstant("i"));
    parts.add(createConstant("i-1"));
    QuantifiedRangePlus p = new QuantifiedRangePlus(parts);
    Assignment ass = new Assignment("i", 8);
    RangeInteger c = p.instantiate(ass);
    assertTrue(c instanceof RangeConstant);
    assertTrue(c.queryMinimum() == 24);
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
  public void testInstantiateTwoVariables() throws ParserException {
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

  @Test
  public void testInstantiateFiveVariables() {
    ArrayList<QuantifiedRangeInteger> parts = new ArrayList<QuantifiedRangeInteger>();
    parts.add(createVariable("i"));
    parts.add(createVariable("i+1"));
    parts.add(createVariable("i+2"));
    parts.add(createVariable("j"));
    parts.add(createVariable("j+2"));
    QuantifiedRangeInteger plus = new QuantifiedRangePlus(parts);
    Assignment ass = new Assignment("i", 1, "j", 5);
    RangeInteger i = plus.instantiate(ass);
    assertTrue(i.toString().equals(
      "bplus(0, 18, " +
        "bplus(0, 6, bplus(0, 3, qrpt[1] ⊕ qrpt[2]) ⊕ qrpt[3]) ⊕ " +
        "bplus(0, 12, qrpt[5] ⊕ qrpt[7]))"));
  }
}

