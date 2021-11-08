import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.parameter.*;
import logic.number.*;
import logic.number.range.RangeVariable;
import logic.number.range.ParamRangeVar;
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

  /** Helper function for tests that compare a range variable with a constant. */
  private ClauseCollector setupIntegerRangeTest(int min, int max, boolean eq, int c) {
    Variable.reset();
    Equals formula = new Equals(makeRangeVar("x", min, max), makeConstant(c), eq);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    formula.addClauses(col);
    return col;
  }

  /** Helper function for tests that compare a constant with a range variable. */
  private ClauseCollector setupIntegerRangeTest(int c, boolean eq, int min, int max) {
    Variable.reset();
    Equals formula = new Equals(makeConstant(c), makeRangeVar("x", min, max), eq);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    formula.addClauses(col);
    return col;
  }

  /** Helper function for tests that compare two range variables in different ranges. */
  private ClauseCollector setupRangeRangeTest(int min1, int max1, boolean eq, int min2, int max2) {
    Variable.reset();
    Equals formula = new Equals(makeRangeVar("x", min1, max1), makeRangeVar("y", min2, max2), eq);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    col.addToMemory("rangevar y");
    formula.addClauses(col);
    return col;
  }

  @Test
  public void testBasicToString() {
    Equals eq = new Equals(makeRangeVar("x", 0, 3), makeConstant(4), true);
    assertTrue(eq.toString().equals("x = 4"));
    assertTrue(eq.negate().toString().equals("x ≠ 4"));
  }

  @Test
  public void testAddClausesEqualsBelowRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, true, 1);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesEqualsMinimumRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, true, 3);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬x≥4"));
  }

  @Test
  public void testAddClausesEqualsMiddleOfRange() {
    ClauseCollector col = setupIntegerRangeTest(4, true, 3, 6);
    assertTrue(col.size() == 2);
    assertTrue(col.contains("x≥4"));
    assertTrue(col.contains("¬x≥5"));
  }

  @Test
  public void testAddClausesEqualsMaximumRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, true, 6);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("x≥6"));
  }

  @Test
  public void testAddClausesEqualsAboveRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, true, 7);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesEqualsDefinitelyEqual() {
    ClauseCollector col = setupIntegerRangeTest(3, true, 3, 3);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testAddClausesNeqBelowRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, false, 1);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testAddClausesNeqMinimumRange() {
    ClauseCollector col = setupIntegerRangeTest(3, false, 3, 6);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("x≥4"));
  }

  @Test
  public void testAddClausesNeqMiddleOfRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, false, 4);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("x≥5 ∨ ¬x≥4"));
  }

  @Test
  public void testAddClausesNeqMaximumRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, false, 6);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬x≥6"));
  }

  @Test
  public void testAddClausesNeqAboveRange() {
    ClauseCollector col = setupIntegerRangeTest(7, false, 3, 6);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testAddClausesNeqDefinitelyEqual() {
    ClauseCollector col = setupIntegerRangeTest(3, 3, false, 3);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesEqualsOverlappingButNotEqualRange() {
    ClauseCollector col = setupRangeRangeTest(1, 5, true, 3, 8);
    assertTrue(col.size() == 6);
    assertTrue(col.contains("x≥3"));
    assertTrue(col.contains("x≥4 ∨ ¬y≥4"));
    assertTrue(col.contains("x≥5 ∨ ¬y≥5"));
    assertTrue(col.contains("¬y≥6"));
    assertTrue(col.contains("¬x≥4 ∨ y≥4"));
    assertTrue(col.contains("¬x≥5 ∨ y≥5"));
  }

  @Test
  public void testAddClausesEqualsSameRange() {
    ClauseCollector col = setupRangeRangeTest(1, 4, true, 1, 4);
    assertTrue(col.size() == 6);
    assertTrue(col.contains("x≥2 ∨ ¬y≥2"));
    assertTrue(col.contains("x≥3 ∨ ¬y≥3"));
    assertTrue(col.contains("x≥4 ∨ ¬y≥4"));
    assertTrue(col.contains("¬x≥2 ∨ y≥2"));
    assertTrue(col.contains("¬x≥3 ∨ y≥3"));
    assertTrue(col.contains("¬x≥4 ∨ y≥4"));
  }

  @Test
  public void testAddClausesEqualsOuterVersusInnerRange() {
    ClauseCollector col = setupRangeRangeTest(1, 8, true, 3, 5);
    assertTrue(col.size() == 6);
    assertTrue(col.contains("x≥3"));
    assertTrue(col.contains("x≥4 ∨ ¬y≥4"));
    assertTrue(col.contains("x≥5 ∨ ¬y≥5"));
    assertTrue(col.contains("¬x≥6"));
    assertTrue(col.contains("¬x≥4 ∨ y≥4"));
    assertTrue(col.contains("¬x≥5 ∨ y≥5"));
  }

  @Test
  public void testAddClausesNeqLowerRangeVersusHigherRange() {
    ClauseCollector col = setupRangeRangeTest(1, 5, false, 3, 8);
    assertTrue(col.size() == 3);
    assertTrue(col.contains("x≥4 ∨ ¬x≥3 ∨ y≥4"));
    assertTrue(col.contains("x≥5 ∨ ¬x≥4 ∨ y≥5 ∨ ¬y≥4"));
    assertTrue(col.contains("¬x≥5 ∨ y≥6 ∨ ¬y≥5"));
  }

  @Test
  public void testAddClausesNeqSameRange() {
    ClauseCollector col = setupRangeRangeTest(1, 4, false, 1, 4);
    assertTrue(col.size() == 4);
    assertTrue(col.contains("x≥2 ∨ y≥2"));
    assertTrue(col.contains("x≥3 ∨ ¬x≥2 ∨ y≥3 ∨ ¬y≥2"));
    assertTrue(col.contains("x≥4 ∨ ¬x≥3 ∨ y≥4 ∨ ¬y≥3"));
    assertTrue(col.contains("¬x≥4 ∨ ¬y≥4"));
  }

  @Test
  public void testAddClausesNeqOuterVersusInnerRange() {
    ClauseCollector col = setupRangeRangeTest(1, 8, false, 3, 5);
    assertTrue(col.size() == 3);
    assertTrue(col.contains("x≥4 ∨ ¬x≥3 ∨ y≥4"));
    assertTrue(col.contains("x≥5 ∨ ¬x≥4 ∨ y≥5 ∨ ¬y≥4"));
    assertTrue(col.contains("x≥6 ∨ ¬x≥5 ∨ ¬y≥5"));
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
}

