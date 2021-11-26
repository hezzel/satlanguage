import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.parameter.PExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.number.*;
import logic.number.range.ParamRangeVar;
import logic.number.binary.ParamBinaryVar;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;
import java.util.Set;
import java.util.ArrayList;

public class QuantifiedPlusTest {
  private Atom truth() {
    return new Atom(new Variable("TRUE"), true);
  }
  
  private PExpression expr(String txt) {
    try { return InputReader.readPExpressionFromString(txt); }
    catch (ParserException e) { return null; }
  }

  private QuantifiedVariable createVariable(String index) {
    try {
      VariableList lst = new VariableList();
      InputReader.declare("qrpt[i] :: Number ∈ {0..i} for i ∈ {1..10}", lst);
      ParamRangeVar qrpt = lst.queryParametrisedRangeVariable("qrpt");
      PExpression par = InputReader.readPExpressionFromString(index);
      return new QuantifiedVariable(qrpt, new Substitution("i", par));
    } catch (ParserException e) { return null; }
  }

  private QuantifiedConstant createConstant(String txt) {
    return new QuantifiedConstant(expr(txt), truth());
  }

  /** Creates <the expression given by txt> + x[i] */
  private QuantifiedPlus createPlusWithTwoArgs(String txt) {
    return new QuantifiedPlus(createConstant(txt), createVariable("i"),
                              ClosedInteger.RANGE, truth());
  }
  
  /** Creates { x[i] , x[j] } */
  private ArrayList<QuantifiedInteger> createParts(String i, String j) {
    ArrayList<QuantifiedInteger> ret = new ArrayList<QuantifiedInteger>();
    ret.add(createVariable(i));
    ret.add(createVariable(j));
    return ret;
  }

  @Test
  public void testSimpleToString() {
    assertTrue(createPlusWithTwoArgs("j + 1").toString().equals("j+1 ⊕ qrpt[i]"));
  }

  @Test
  public void testVarplusToString() {
    QuantifiedInteger ri = new QuantifiedPlus(createParts("i", "j"), ClosedInteger.RANGE, truth());
    assertTrue(ri.toString().equals("qrpt[i] ⊕ qrpt[j]"));
  }

  @Test
  public void testTripleVarplusToString() {
    ArrayList<QuantifiedInteger> parts = createParts("i", "i+1");
    parts.add(createVariable("j"));
    QuantifiedInteger ri = new QuantifiedPlus(parts, ClosedInteger.RANGE, truth());
    assertTrue(ri.toString().equals("qrpt[i] ⊕ qrpt[i+1] ⊕ qrpt[j]"));
  }

  @Test
  public void testParameters() {
    Set<String> params = createPlusWithTwoArgs("i + j -1").queryParameters();
    assertTrue(params.size() == 2);
    assertTrue(params.contains("i"));
    assertTrue(params.contains("j"));
  }

  @Test
  public void testNotClosedWithPExpression() {
    assertFalse(createPlusWithTwoArgs("1").queryClosed());
  }

  @Test
  public void testNotClosedWithVariables() throws ParserException {
    ArrayList<QuantifiedInteger> parts = createParts("1", "j");
    parts.add(createVariable("5"));
    QuantifiedInteger ri = new QuantifiedPlus(parts, ClosedInteger.RANGE, truth());
    assertFalse(ri.queryClosed());
  }

  @Test
  public void testClosed() throws ParserException {
    ArrayList<QuantifiedInteger> parts = createParts("1", "7");
    parts.add(createVariable("5"));
    QuantifiedInteger ri = new QuantifiedPlus(parts, ClosedInteger.RANGE, truth());
    assertTrue(ri.queryClosed());
  }

  @Test
  public void testSubstitute() {
    Substitution subst = new Substitution("i", expr("c * d"), "j", expr("a+1"));
    QuantifiedInteger c = createPlusWithTwoArgs("1+j").substitute(subst);
    assertTrue(c.toString().equals("a+2 ⊕ qrpt[c*d]"));
    assertTrue(c.queryParameters().size() == 3);
  }

  @Test
  public void testInstantiateOneSideConstant() {
    Assignment ass = new Assignment("i", 3, "j", -1);
    ClosedInteger c = createPlusWithTwoArgs("i+j").instantiate(ass);
    assertTrue(c.toString().equals("(qrpt[3] ⊕ 2)"));
    assertTrue(c.queryMinimum() == 2);
    assertTrue(c.queryMaximum() == 5);
  }

  @Test
  public void testInstantiateThreeSidesConstant() {
    ArrayList<QuantifiedInteger> parts = new ArrayList<QuantifiedInteger>();
    parts.add(createConstant("i+1"));
    parts.add(createConstant("i"));
    parts.add(createConstant("i-1"));
    QuantifiedPlus p = new QuantifiedPlus(parts, ClosedInteger.BOTH, truth());
    Assignment ass = new Assignment("i", 8);
    ClosedInteger c = p.instantiate(ass);
    assertTrue(c instanceof ConstantInteger);
    assertTrue(c.queryMinimum() == 24);
  }

  @Test
  public void testInstantiateOneSideConstantVariable() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("x[i] :: Int? ∈ {1..i} for i ∈ {1..10}", lst);
    ParamBinaryVar x = lst.queryParametrisedBinaryVariable("x");
    QuantifiedVariable x1 = new QuantifiedVariable(x, new Substitution());
    QuantifiedVariable x2 = new QuantifiedVariable(x, new Substitution("i", expr("j")));
    QuantifiedPlus p = new QuantifiedPlus(x1, x2, ClosedInteger.BINARY, truth());
    Assignment ass = new Assignment("i", 3, "j", 1);
    ClosedInteger i = p.instantiate(ass);
    assertTrue(i.toString().equals("(x[3] ⊞ 1)"));
  }

  @Test
  public void testInstantiateTwoVariables() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("x[i] :: Nat5 for i ∈ {1..10}", lst);
    ParamBinaryVar x = lst.queryParametrisedBinaryVariable("x");
    QuantifiedVariable x1 = new QuantifiedVariable(x, new Substitution());
    QuantifiedVariable x2 = new QuantifiedVariable(x, new Substitution("i", expr("j")));
    QuantifiedPlus p = new QuantifiedPlus(x1, x2, ClosedInteger.BINARY, truth());
    Assignment ass = new Assignment("i", 3, "j", 7);
    ClosedInteger i = p.instantiate(ass);
    assertTrue(i.toString().equals("(x[3] ⊞ x[7])"));
  }

  @Test
  public void testInstantiateFiveVariables() {
    ArrayList<QuantifiedInteger> parts = new ArrayList<QuantifiedInteger>();
    parts.add(createVariable("i"));
    parts.add(createVariable("i+1"));
    parts.add(createVariable("i+2"));
    parts.add(createVariable("j"));
    parts.add(createVariable("j+2"));
    QuantifiedInteger plus = new QuantifiedPlus(parts, ClosedInteger.RANGE, truth());
    Assignment ass = new Assignment("i", 1, "j", 5);
    ClosedInteger i = plus.instantiate(ass);
    assertTrue(i.toString().equals("(((qrpt[1] ⊕ qrpt[2]) ⊕ qrpt[3]) ⊕ (qrpt[5] ⊕ qrpt[7]))"));
  }
}

