import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.parameter.PExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.parameter.Parameter;
import logic.number.range.ParamRangeVar;
import logic.number.ClosedInteger;
import logic.number.QuantifiedInteger;
import logic.number.QuantifiedConstant;
import logic.number.QuantifiedVariable;
import logic.number.QuantifiedPlus;
import logic.number.QuantifiedSum;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;
import java.util.Set;
import java.util.ArrayList;

public class QuantifiedSumTest {
  private PExpression expr(String txt) {
    try { return InputReader.readPExpressionFromString(txt); }
    catch (ParserException e) { return null; }
  }

  private QuantifiedVariable createVariable(String index) {
    try {
      VariableList lst = new VariableList();
      InputReader.declare("v[i] :: Number ∈ {0..i} for i ∈ {1..10}", lst);
      ParamRangeVar v = lst.queryParametrisedRangeVariable("v");
      PExpression par = InputReader.readPExpressionFromString(index);
      return new QuantifiedVariable(v, new Substitution("i", par));
    } catch (ParserException e) { return null; }
  }

  private Atom truth() {
    return new Atom(new Variable("TRUE"), true);
  }

  private QuantifiedConstant createConstant(String txt) {
    return new QuantifiedConstant(expr(txt), truth());
  }

  private ArrayList<Parameter> params(String p1, String p2, String p3) {
    ArrayList<Parameter> ret = new ArrayList<Parameter>();
    try {
      ret.add(InputReader.readParameterFromString(p1));
      ret.add(InputReader.readParameterFromString(p2));
      if (p3 != null) ret.add(InputReader.readParameterFromString(p3));
    } catch (ParserException e) { return null; }
    return ret;
  }

  @Test
  public void testSimpleToString() {
    QuantifiedVariable x = createVariable("i");
    Parameter p = new Parameter("i", 1, 10);
    QuantifiedSum sum = new QuantifiedSum(p, x, ClosedInteger.RANGE, truth());
    assertTrue(sum.toString().equals("Σ { v[i] | i ∈ {1..10} }"));
  }

  @Test
  public void testComplexToString() {
    ArrayList<Parameter> ps = params("i ∈ {1..5}", "j ∈ {0..i} with i % 2 = 0", "k ∈ {a..b}");
    QuantifiedVariable x = createVariable("a+j");
    QuantifiedInteger expr =
      new QuantifiedPlus(x, createConstant("j - 1"), ClosedInteger.RANGE, truth());
    QuantifiedSum sum = new QuantifiedSum(ps, expr, ClosedInteger.RANGE, truth());
    assertTrue(sum.toString().equals(
      "Σ { v[a+j] ⊕ j-1 | i ∈ {1..5}, j ∈ {0..i} with i%2 = 0, k ∈ {a..b} }"));
  }

  @Test
  public void testParametersOnlyInArgumentsList() {
    ArrayList<Parameter> ps = params("i ∈ {1..a}", "j ∈ {b..2} with j % c + i = 0", null);
    QuantifiedVariable x = createVariable("1");
    QuantifiedSum sum = new QuantifiedSum(ps, x, ClosedInteger.RANGE, truth());
    Set<String> parameters = sum.queryParameters();
    assertTrue(parameters.size() == 3);
    assertTrue(parameters.contains("a"));
    assertTrue(parameters.contains("b"));
    assertTrue(parameters.contains("c"));
  }

  @Test
  public void testLaterParameterOccursInEarlier() {
    ArrayList<Parameter> ps = params("i ∈ {1..j}", "j ∈ {1..3} with j != k", "k ∈ {i..4}");
    QuantifiedVariable x = createVariable("1");
    QuantifiedSum sum = new QuantifiedSum(ps, x, ClosedInteger.RANGE, truth());
    Set<String> parameters = sum.queryParameters();
    assertTrue(parameters.size() == 2);
    assertTrue(parameters.contains("j"));
    assertTrue(parameters.contains("k"));
  }

  @Test
  public void testQuantifiersRemovedFromParameters() {
    ArrayList<Parameter> ps = params("i ∈ {1..5}", "j ∈ {1..5}", null);
    QuantifiedVariable x = createVariable("i+j-k");
    QuantifiedSum sum = new QuantifiedSum(ps, x, ClosedInteger.RANGE, truth());
    Set<String> parameters = sum.queryParameters();
    assertTrue(parameters.size() == 1);
    assertTrue(parameters.contains("k"));
  }

  @Test
  public void testSubstitute() {
    ArrayList<Parameter> ps = params("i ∈ {a..b} with c != i", "j ∈ {i..5} with i + 1 != j",
                                     "k ∈ {1..j}");
    QuantifiedVariable x = createVariable("i+j-k+a+b");
    QuantifiedConstant u = createConstant("u+c");
    QuantifiedPlus plus = new QuantifiedPlus(u, x, ClosedInteger.RANGE, truth());
    QuantifiedSum sum = new QuantifiedSum(ps, plus, ClosedInteger.RANGE, truth());
    Substitution subst = new Substitution("i", expr("1"), "j", expr("2"), "k", expr("x"));
    subst.put("a", expr("3"));
    subst.put("b", expr("4"));
    subst.put("c", expr("y"));
    subst.put("u", expr("z"));
    QuantifiedInteger s = sum.substitute(subst);
    assertTrue(s.toString().equals(
      "Σ { z+y ⊕ v[i+j-k+7] | i ∈ {3..4} with y ≠ i, j ∈ {i..5} with i+1 ≠ j, k ∈ {1..j} }"));
  }

  @Test
  public void testAddComponents() {
    ArrayList<Parameter> ps = params("i ∈ {1..a} with b != i", "j ∈ {i..3}", null);
    QuantifiedVariable x = createVariable("i+2*j-1");
    QuantifiedConstant u = createConstant("u");
    QuantifiedSum sum = new QuantifiedSum(ps, new QuantifiedPlus(x, u, ClosedInteger.RANGE,
                                          truth()), ClosedInteger.RANGE, truth());
    Assignment ass = new Assignment("a", 4, "b", 3, "u", 0);
    ass.put("i", 2);
    ass.put("j", 3);
    ArrayList<ClosedInteger> components = new ArrayList<ClosedInteger>();
    sum.addComponents(0, ass, components);
    assertTrue(components.size() == 5);
    assertTrue(components.get(0).toString().equals("v[2]"));    // i = 1, j = 1
    assertTrue(components.get(1).toString().equals("v[4]"));    // i = 1, j = 2
    assertTrue(components.get(2).toString().equals("v[6]"));    // i = 1, j = 3
    assertTrue(components.get(3).toString().equals("v[5]"));    // i = 2, j = 2
    assertTrue(components.get(4).toString().equals("v[7]"));    // i = 2, j = 3
    // no case for i = 3, since then i = b; no case for i = 4, since then {i..3} is empty
  }

  @Test
  public void testInstantiate() {
    ArrayList<Parameter> ps = params("i ∈ {1..k}", "j ∈ {0..k-1}", null);
    QuantifiedVariable x = createVariable("k*j+i");
    QuantifiedSum sum = new QuantifiedSum(ps, x, ClosedInteger.RANGE, truth());
    Assignment ass = new Assignment("k", 3);
    ClosedInteger result = sum.instantiate(ass);
    assertTrue(result.toString().equals(
      "((((v[1] ⊕ v[4]) ⊕ v[7]) ⊕ (v[2] ⊕ v[5])) ⊕ ((v[8] ⊕ v[3]) ⊕ (v[6] ⊕ v[9])))"));
  }
}

