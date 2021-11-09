import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.parameter.PExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.parameter.ParamBoolVar;
import logic.number.range.ParamRangeVar;
import logic.number.range.RangeInteger;
import logic.number.ClosedInteger;
import logic.number.QuantifiedInteger;
import logic.number.QuantifiedConstant;
import logic.number.QuantifiedVariable;
import logic.formula.Formula;
import logic.formula.AtomicFormula;
import logic.formula.Or;
import logic.formula.QuantifiedAtom;
import logic.formula.QuantifiedConditionalInteger;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;
import java.util.Set;
import java.util.ArrayList;

public class QuantifiedConditionalIntegerTest {
  private Formula makeAtom(String varname, boolean value) {
    Variable x = new Variable(varname);
    return new AtomicFormula(new Atom(x, value));
  }

  private PExpression expr(String txt) {
    try { return InputReader.readPExpressionFromString(txt); }
    catch (ParserException e) { return null; }
  }

  private QuantifiedVariable createVariable(String index) {
    try {
      VariableList lst = new VariableList();
      InputReader.declare("ivar[i] :: Number ∈ {0..i} for i ∈ {1..10}", lst);
      ParamRangeVar ivar = lst.queryParametrisedRangeVariable("ivar");
      PExpression par = InputReader.readPExpressionFromString(index);
      return new QuantifiedVariable(ivar, new Substitution("i", par));
    } catch (ParserException e) { return null; }
  }

  private Formula createParamBoolVar(String index) {
  try {
      VariableList lst = new VariableList();
      InputReader.declare("bvar[j] :: Bool for j ∈ {0..10}", lst);
      ParamBoolVar bvar = lst.queryParametrisedBooleanVariable("bvar");
      PExpression par = InputReader.readPExpressionFromString(index);
      return new QuantifiedAtom(bvar, true, new Substitution("j", par));
    } catch (ParserException e) { return null; }
  }

  private QuantifiedConstant createConstant(String txt) {
    return new QuantifiedConstant(expr(txt), new Atom(new Variable("TRUE"), true));
  }

  private QuantifiedConditionalInteger createTest() {
    Formula formula = new Or(makeAtom("x", true), createParamBoolVar("j-1"));
    QuantifiedInteger value = createVariable("i+3");
    Atom truth = new Atom(new Variable("TRUE"), true);
    return new QuantifiedConditionalInteger(formula, value, truth);
  }

  @Test
  public void testBasics() {
    QuantifiedInteger test = createTest();
    assertTrue(test.toString().equals("(x ∨ bvar[j-1]) ? ivar[i+3]"));
  }

  @Test
  public void testParameters() {
    QuantifiedInteger test = createTest();
    assertTrue(test.queryParameters().size() == 2);
    assertTrue(test.queryParameters().contains("j"));
    assertTrue(test.queryParameters().contains("i"));
    assertFalse(test.queryClosed());
  }

  @Test
  public void testSubstitute() {
    QuantifiedInteger test = createTest();
    Substitution subst = new Substitution("i", expr("a+1"), "j", expr("4"));
    test = test.substitute(subst);
    assertTrue(test.toString().equals("(x ∨ bvar[3]) ? ivar[a+4]"));
  }

  @Test
  public void testInstantiate() {
    QuantifiedInteger test = createTest();
    Assignment ass = new Assignment("i", 3, "j", 6);
    ClosedInteger ri = test.instantiate(ass);
    assertTrue(ri.toString().equals("⟦x ∨ bvar[5]⟧?ivar[6]"));
    ClauseCollector col = new ClauseCollector();
    ri.getRange().addWelldefinednessClauses(col);
    assertTrue(col.contains("¬x ∨ ⟦x ∨ bvar[5]⟧"));      // col has x ∨ bvar[5] <-> ⟦x ∨ bvar[5]⟧
    assertTrue(col.contains("¬bvar[5] ∨ ⟦x ∨ bvar[5]⟧"));
    assertTrue(col.contains("x ∨ bvar[5] ∨ ¬⟦x ∨ bvar[5]⟧"));
  }
}

