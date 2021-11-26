import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.parameter.*;
import logic.number.*;
import logic.number.range.RangeVariable;
import logic.number.range.ParamRangeVar;
import logic.number.binary.BinaryVariable;
import logic.number.binary.ParamBinaryVar;
import logic.formula.Formula;
import logic.formula.Equals;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;

public class EqualsTest {
  private Atom truth() {
    return new Atom(new Variable("TRUE"), true);
  }

  private QuantifiedInteger makeRangeVar(String name, int min, int max) {
    RangeVariable ri = new RangeVariable(name, min, max, truth());
    return new VariableInteger(ri);
  }

  private QuantifiedInteger makeConstant(int num) {
    return new QuantifiedConstant(new ConstantExpression(num), truth());
  }

  @Test
  public void testBasicToString() {
    Equals eq = new Equals(makeRangeVar("x", 0, 3), makeConstant(4), true);
    assertTrue(eq.toString().equals("x = 4"));
    assertTrue(eq.negate().toString().equals("x ≠ 4"));
  }

  @Test
  public void testBasics() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x[i,j] :: Number ∈ {0..10} for i ∈ {1..3}, j ∈ {i+1..4}", vars);
    ParamRangeVar x = vars.queryParametrisedRangeVariable("x");
    Substitution subst = new Substitution("i", InputReader.readPExpressionFromString("a+j"),
                                          "j", InputReader.readPExpressionFromString("b"));
    QuantifiedVariable xajb = new QuantifiedVariable(x, subst);
    QuantifiedVariable xij = new QuantifiedVariable(x, new Substitution());
    Equals eq = new Equals(xajb, xij, true);    // x[a+j,c] ≥ x[i,j]

    assertTrue(eq.toString().equals("x[a+j,b] = x[i,j]"));
    assertTrue(eq.negate().toString().equals("x[a+j,b] ≠ x[i,j]"));
    assertTrue(eq.queryAtom() == null);
    assertFalse(eq.queryClosed());

    subst = new Substitution("a", InputReader.readPExpressionFromString("3"),
                             "b", InputReader.readPExpressionFromString("i"));
    Formula form = eq.substitute(subst);
    assertTrue(form.toString().equals("x[j+3,i] = x[i,j]"));
    assertFalse(form.queryClosed());

    form = form.instantiate(new Assignment("i", 7, "j", 4));
    assertTrue(form.toString().equals("x[7,7] = x[7,4]"));
    assertTrue(form.queryClosed());
    assertTrue(form.negate().toString().equals("x[7,7] ≠ x[7,4]"));
  }

  @Test
  public void testAddClausesEqualsRelevantBound() {
    Variable.reset();
    QuantifiedInteger plus = new QuantifiedPlus(makeRangeVar("x", 0, 5), makeRangeVar("y", 0, 5),
                                                ClosedInteger.RANGE, truth());
    Equals formula = new Equals(makeConstant(1), plus, true);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    col.addToMemory("rangevar y");
    formula.addClauses(col);
    // the clauses should only check if x⊕y is ≤ 0, = 1 or ≥ 2
    assertTrue(col.contains("¬x≥1 ∨ x⊕y≥1")); // x ≥ 1 → x⊕y ≥ 1
    assertTrue(col.contains("¬x≥2 ∨ x⊕y≥2")); // x ≥ 2 → x⊕y ≥ 2
    assertTrue(col.contains("¬y≥1 ∨ x⊕y≥1")); // y ≥ 1 → x⊕y ≥ 1
    assertTrue(col.contains("¬y≥2 ∨ x⊕y≥2")); // y ≥ 2 → x⊕y ≥ 2
    assertTrue(col.contains("¬x≥1 ∨ ¬y≥1 ∨ x⊕y≥2")); // x ≥ 1 ∧ y ≥ 1 → x⊕y ≥ 2
    assertTrue(col.contains("x≥1 ∨ y≥1 ∨ ¬x⊕y≥1")); // x ≤ 0 ∧ y ≤ 0 → x⊕y ≤ 0
    assertTrue(col.contains("x≥2 ∨ y≥1 ∨ ¬x⊕y≥2")); // x ≤ 1 ∧ y ≤ 0 → x⊕y ≤ 1
    assertTrue(col.contains("x≥1 ∨ y≥2 ∨ ¬x⊕y≥2")); // x ≤ 0 ∧ y ≤ 1 → x⊕y ≤ 1
    assertTrue(col.contains("x⊕y≥1"));  // for the Equals: we want x⊕y to be at least 1
    assertTrue(col.contains("¬x⊕y≥2"));  // for the Equals: we want x⊕y to be at most 1
    assertTrue(col.size() == 10);
  }

  @Test
  public void testAddClausesDefSingleAtom() {
    Variable.reset();
    Equals formula = new Equals(makeRangeVar("x", 1, 5), makeConstant(3), true);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    Atom atom = new Atom(new Variable("myvar"), true);
    formula.addClausesDef(atom, col);
    assertTrue(col.size() == 3);
    assertTrue(col.contains("x≥3 ∨ ¬myvar"));       // myvar → x ≥ 3
    assertTrue(col.contains("¬x≥4 ∨ ¬myvar"));      // myvar → x ≤ 3 (x < 4)
    assertTrue(col.contains("x≥4 ∨ ¬x≥3 ∨ myvar")); // x ≥ 3 ∧ x < 4 → myvar
  }

  @Test
  public void testAddClausesDefMultipleAtoms() {
    Variable.reset();
    Equals formula = new Equals(makeRangeVar("x", 1, 5), makeRangeVar("y", 3, 7), false);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    col.addToMemory("rangevar y");
    Atom atom = new Atom(new Variable("myvar"), true);
    formula.addClausesDef(atom, col);
    assertTrue(col.size() == 9);
    // atom → x ≠ y  =  ¬atom ∨ x ≠ y,
    // where x ≠ y   =   x = 3 → y ≥ 4 AND x = 4 → y < 4 ∨ y ≥ 5 AND x ≥ 5 → y < 5 ∨ y ≥ 6
    assertTrue(col.contains("x≥4 ∨ ¬x≥3 ∨ y≥4 ∨ ¬myvar"));
    assertTrue(col.contains("x≥5 ∨ ¬x≥4 ∨ y≥5 ∨ ¬y≥4 ∨ ¬myvar"));
    assertTrue(col.contains("¬x≥5 ∨ y≥6 ∨ ¬y≥5 ∨ ¬myvar"));
    // x ≠ y → atom  =  x = y ∨ atom, where x = y  =  x≥3 AND x≥4 <-> y≥4 AND x≥5 <-> y≥5 AND ¬y≥6
    assertTrue(col.contains("x≥3 ∨ myvar"));
    assertTrue(col.contains("x≥4 ∨ ¬y≥4 ∨ myvar"));
    assertTrue(col.contains("¬x≥4 ∨ y≥4 ∨ myvar"));
    assertTrue(col.contains("x≥5 ∨ ¬y≥5 ∨ myvar"));
    assertTrue(col.contains("¬x≥5 ∨ y≥5 ∨ myvar"));
    assertTrue(col.contains("¬y≥6 ∨ myvar"));
  }

  @Test
  public void testBinaryEqualityWithMinus() {
    QuantifiedInteger a = new VariableInteger(new BinaryVariable("a", 0, 260, truth()));
    QuantifiedInteger b = new VariableInteger(new BinaryVariable("b", 0, 260, truth()));
    Equals formula1 = new Equals(a, new QuantifiedConstant(260, truth()), true);
    Equals formula2 = new Equals(b, new QuantifiedPlus(a,
      new QuantifiedConstant(-35, truth()), ClosedInteger.BINARY, truth()), true);
    ClauseCollector col = new ClauseCollector();
    formula1.addClauses(col);
    formula2.addClauses(col);
    col.force("TRUE", true);
    assertTrue(col.checkSatisfiable());
  }
}

