import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.parameter.*;
import logic.number.*;
import logic.number.range.*;
import logic.formula.Formula;
import logic.formula.Geq;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;

public class GeqTest {
  private QuantifiedRangeInteger makeVar(String name, int min, int max) {
    RangeInteger ri = new RangeVariable(name, min, max, new Atom(new Variable("TRUE"), true));
    return new QuantifiedRangeWrapper(ri);
  }

  private QuantifiedRangeInteger makeConstant(int num) {
    return new QuantifiedRangeWrapper(new RangeConstant(num, new Atom(new Variable("TRUE"), true)));
  }

  /** Helper function for tests that compare an integer with a constant. */
  private ClauseCollector setupIntegerRangeTest(int min, int max, boolean geq, int c) {
    Geq formula = new Geq(makeVar("x", min, max), makeConstant(c), geq);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    formula.addClauses(col);
    return col;
  }

  /** Helper function for tests that compare a constant with an integer. */
  private ClauseCollector setupReverseIntegerRangeTest(int min, int max, boolean geq, int c) {
    Geq formula = new Geq(makeConstant(c), makeVar("x", min, max), geq);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    formula.addClauses(col);
    return col;
  }

  /** Helper function for tests that compare two variables in different ranges. */
  private ClauseCollector setupRangeRangeTest(int min1, int max1, boolean geq, int min2, int max2) {
    Geq formula = new Geq(makeVar("x", min1, max1), makeVar("y", min2, max2), geq);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    col.addToMemory("rangevar y");
    formula.addClauses(col);
    return col;
  }

  @Test
  public void testBasicToString() {
    Geq geq = new Geq(makeVar("x", 0, 3), makeConstant(4), true);
    assertTrue(geq.toString().equals("x ≥ 4"));
    geq = geq.negate();
    assertTrue(geq.toString().equals("x < 4"));
  }

  @Test
  public void testAddClausesGeqBelowRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, true, 1);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testAddClausesGeqMinimumRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, true, 3);
    assertTrue(col.size() == 0);  // trivially satisfied
  }

  @Test
  public void testAddClausesGeqMiddleOfRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, true, 4);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("x≥4"));
  }

  @Test
  public void testAddClausesGeqMaximumRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, true, 6);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("x≥6"));
  }

  @Test
  public void testAddClausesGeqAboveRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, true, 7);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesSmallerBelowRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, false, 1);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesSmallerMinimumRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, false, 3);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesSmallerMiddleOfRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, false, 4);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬x≥4"));
  }

  @Test
  public void testAddClausesSmallerMaximumRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, false, 6);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬x≥6"));
  }

  @Test
  public void testAddClausesSmallerAboveRange() {
    ClauseCollector col = setupIntegerRangeTest(3, 6, false, 7);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testAddClausesReverseGeqBelowRange() {
    ClauseCollector col = setupReverseIntegerRangeTest(3, 6, true, 1);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesReverseGeqMinimumRange() {
    ClauseCollector col = setupReverseIntegerRangeTest(3, 6, true, 3);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬x≥4"));
  }

  @Test
  public void testAddClausesReverseGeqMiddleOfRange() {
    ClauseCollector col = setupReverseIntegerRangeTest(3, 6, true, 4);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬x≥5"));
  }

  @Test
  public void testAddClausesReverseGeqMaximumRange() {
    ClauseCollector col = setupReverseIntegerRangeTest(3, 6, true, 6);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testAddClausesReverseGeqAboveRange() {
    ClauseCollector col = setupReverseIntegerRangeTest(3, 6, true, 7);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testAddClausesReverseSmallerBelowRange() {
    ClauseCollector col = setupReverseIntegerRangeTest(3, 6, false, 1);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testAddClausesReverseSmallerMinimumRange() {
    ClauseCollector col = setupReverseIntegerRangeTest(3, 6, false, 3);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("x≥4"));
  }

  @Test
  public void testAddClausesReverseSmallerMiddleOfRange() {
    ClauseCollector col = setupReverseIntegerRangeTest(3, 6, false, 4);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("x≥5"));
  }

  @Test
  public void testAddClausesReverseSmallerMaximumRange() {
    ClauseCollector col = setupReverseIntegerRangeTest(3, 6, false, 6);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesReverseSmallerAboveRange() {
    ClauseCollector col = setupReverseIntegerRangeTest(3, 6, false, 7);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesGeqLowerRangeVersusHigherRange() {
    ClauseCollector col = setupRangeRangeTest(1, 5, true, 3, 8);
    assertTrue(col.size() == 4);
    assertTrue(col.contains("x≥3"));
    assertTrue(col.contains("x≥4 ∨ ¬y≥4"));
    assertTrue(col.contains("x≥5 ∨ ¬y≥5"));
    assertTrue(col.contains("¬y≥6"));
  }

  @Test
  public void testAddClausesGeqHigherRangeVersusLowerRange() {
    ClauseCollector col = setupRangeRangeTest(3, 8, true, 1, 5);
    assertTrue(col.size() == 2);
    assertTrue(col.contains("x≥4 ∨ ¬y≥4"));
    assertTrue(col.contains("x≥5 ∨ ¬y≥5"));
  }

  @Test
  public void testAddClausesGeqSameRange() {
    ClauseCollector col = setupRangeRangeTest(1, 4, true, 1, 4);
    assertTrue(col.size() == 3);
    assertTrue(col.contains("x≥2 ∨ ¬y≥2"));
    assertTrue(col.contains("x≥3 ∨ ¬y≥3"));
    assertTrue(col.contains("x≥4 ∨ ¬y≥4"));
  }

  @Test
  public void testAddClausesGeqOuterVersusInnerRange() {
    ClauseCollector col = setupRangeRangeTest(1, 8, true, 3, 5);
    assertTrue(col.size() == 3);
    assertTrue(col.contains("x≥3"));
    assertTrue(col.contains("x≥4 ∨ ¬y≥4"));
    assertTrue(col.contains("x≥5 ∨ ¬y≥5"));
  }

  @Test
  public void testAddClausesGeqInnerVersusOuterRange() {
    ClauseCollector col = setupRangeRangeTest(3, 5, true, 1, 8);
    assertTrue(col.size() == 3);
    assertTrue(col.contains("¬y≥6"));
    assertTrue(col.contains("x≥4 ∨ ¬y≥4"));
    assertTrue(col.contains("x≥5 ∨ ¬y≥5"));
  }

  @Test
  public void testAddClausesSmallerLowerRangeVersusHigherRange() {
    ClauseCollector col = setupRangeRangeTest(1, 5, false, 3, 8);
    assertTrue(col.size() == 3);
    assertTrue(col.contains("¬x≥3 ∨ y≥4"));
    assertTrue(col.contains("¬x≥4 ∨ y≥5"));
    assertTrue(col.contains("¬x≥5 ∨ y≥6"));
  }

  @Test
  public void testAddClausesSmallerHigherRangeVersusLowerRange() {
    ClauseCollector col = setupRangeRangeTest(3, 8, false, 1, 5);
    assertTrue(col.size() == 3);
    assertTrue(col.contains("y≥4"));
    assertTrue(col.contains("¬x≥4 ∨ y≥5"));
    assertTrue(col.contains("¬x≥5"));
  }

  @Test
  public void testAddClausesSmallerSameRange() {
    ClauseCollector col = setupRangeRangeTest(1, 4, false, 1, 4);
    assertTrue(col.size() == 4);
    assertTrue(col.contains("y≥2"));
    assertTrue(col.contains("¬x≥2 ∨ y≥3"));
    assertTrue(col.contains("¬x≥3 ∨ y≥4"));
    assertTrue(col.contains("¬x≥4"));
  }

  @Test
  public void testAddClausesSmallerOuterVersusInnerRange() {
    ClauseCollector col = setupRangeRangeTest(1, 8, false, 3, 5);
    assertTrue(col.size() == 3);
    assertTrue(col.contains("¬x≥3 ∨ y≥4"));
    assertTrue(col.contains("¬x≥4 ∨ y≥5"));
    assertTrue(col.contains("¬x≥5"));
  }

  @Test
  public void testAddClausesSmallerInnerVersusOuterRange() {
    ClauseCollector col = setupRangeRangeTest(3, 5, false, 1, 8);
    assertTrue(col.size() == 3);
    assertTrue(col.contains("y≥4"));
    assertTrue(col.contains("¬x≥4 ∨ y≥5"));
    assertTrue(col.contains("¬x≥5 ∨ y≥6"));
  }

  @Test
  public void testAddClausesDefSingleAtom() {
    Geq formula = new Geq(makeVar("x", 1, 5), makeConstant(3), true);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    Atom atom = new Atom(new Variable("myvar"), true);
    formula.addClausesDef(atom, col);
    assertTrue(col.size() == 2);
    assertTrue(col.contains("x≥3 ∨ ¬myvar"));
    assertTrue(col.contains("¬x≥3 ∨ myvar"));
  }

  @Test
  public void testAddClausesGeqRelevantBoundRight() {
    Variable.reset();
    QuantifiedRangeInteger plus = new QuantifiedRangePlus(makeVar("x", 0, 5), makeVar("y", 0, 5));
    QuantifiedRangeInteger z = makeVar("z", 1, 2); 
    Geq formula = new Geq(plus, z, true); 
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    col.addToMemory("rangevar y");
    col.addToMemory("rangevar z");
    formula.addClauses(col);
    // we only really need to know whether x⊕y is at most 0, equal to 1, or at least 2
    assertTrue(col.contains("¬x≥1 ∨ x⊕y≥1"));   // x ≥ 1 → x⊕y ≥ 1
    assertTrue(col.contains("¬x≥2 ∨ x⊕y≥2"));   // x ≥ 2 → x⊕y ≥ 2
    assertTrue(col.contains("¬y≥1 ∨ x⊕y≥1"));   // y ≥ 1 → x⊕y ≥ 1
    assertTrue(col.contains("¬y≥2 ∨ x⊕y≥2"));   // y ≥ 2 → x⊕y ≥ 2
    assertTrue(col.contains("¬x≥1 ∨ ¬y≥1 ∨ x⊕y≥2"));   // x ≥ 1 ∧ y ≥ 1 → x⊕y ≥ 2
    assertTrue(col.contains("x≥1 ∨ y≥1 ∨ ¬x⊕y≥1")); // x ≤ 0 ∧ y ≤ 0 → x⊕y ≤ 0
    assertTrue(col.contains("x≥1 ∨ y≥2 ∨ ¬x⊕y≥2")); // x ≤ 0 ∧ y ≤ 1 → x⊕y ≤ 1
    assertTrue(col.contains("x≥2 ∨ y≥1 ∨ ¬x⊕y≥2")); // x ≤ 1 ∧ y ≤ 0 → x⊕y ≤ 1
    assertTrue(col.contains("x⊕y≥1"));          // x⊕y is at least zmin
    assertTrue(col.contains("¬z≥2 ∨ x⊕y≥2"));   // z≥2 → x⊕y≥2
    assertTrue(col.size() == 10);
  }

  @Test
  public void testAddClausesGeqRelevantBoundLeft() {
    Variable.reset();
    QuantifiedRangeInteger plus = new QuantifiedRangePlus(makeVar("x", 0, 5), makeVar("y", 0, 5));
    QuantifiedRangeInteger z = makeVar("z", 1, 2); 
    Geq formula = new Geq(z, plus, true); 
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    col.addToMemory("rangevar y");
    col.addToMemory("rangevar z");
    formula.addClauses(col);
    // we only really need to know whether x⊕y is at most 1, equal to 2, or at least 3
    assertTrue(col.contains("¬x≥2 ∨ x⊕y≥2"));        // x ≥ 2 → x⊕y ≥ 2
    assertTrue(col.contains("¬x≥3 ∨ x⊕y≥3"));        // x ≥ 2 → x⊕y ≥ 3
    assertTrue(col.contains("¬y≥2 ∨ x⊕y≥2"));        // y ≥ 2 → x⊕y ≥ 2
    assertTrue(col.contains("¬y≥3 ∨ x⊕y≥3"));        // y ≥ 2 → x⊕y ≥ 3
    assertTrue(col.contains("¬x≥1 ∨ ¬y≥1 ∨ x⊕y≥2")); // x ≥ 1 ∧ y ≥ 1 → x⊕y ≥ 2
    assertTrue(col.contains("¬x≥1 ∨ ¬y≥2 ∨ x⊕y≥3")); // x ≥ 1 ∧ y ≥ 2 → x⊕y ≥ 3
    assertTrue(col.contains("¬x≥2 ∨ ¬y≥1 ∨ x⊕y≥3")); // x ≥ 2 ∧ y ≥ 1 → x⊕y ≥ 3
    assertTrue(col.contains("x≥1 ∨ y≥2 ∨ ¬x⊕y≥2"));  // x ≤ 0 ∧ y ≤ 1 → x⊕y ≤ 1
    assertTrue(col.contains("x≥2 ∨ y≥1 ∨ ¬x⊕y≥2"));  // x ≤ 1 ∧ y ≤ 0 → x⊕y ≤ 1
    assertTrue(col.contains("x≥2 ∨ y≥2 ∨ ¬x⊕y≥3"));  // x ≤ 1 ∧ y ≤ 1 → x⊕y ≤ 2
    assertTrue(col.contains("x≥3 ∨ y≥1 ∨ ¬x⊕y≥3"));  // x ≤ 2 ∧ y ≤ 0 → x⊕y ≤ 2
    assertTrue(col.contains("x≥1 ∨ y≥3 ∨ ¬x⊕y≥3"));  // x ≤ 0 ∧ y ≤ 2 → x⊕y ≤ 2
    assertTrue(col.contains("¬x⊕y≥3"));              // x⊕y is at most 2 = zmax
    assertTrue(col.contains("z≥2 ∨ ¬x⊕y≥2"));        // z≤1 → x⊕y≤1
    assertTrue(col.size() == 14);
  }

  @Test
  public void testAddClausesDefMultipleAtoms() {
    Variable.reset();
    Geq formula = new Geq(makeVar("x", 1, 5), makeVar("y", 3, 7), false);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    col.addToMemory("rangevar y");
    Atom atom = new Atom(new Variable("myvar"), true);
    formula.addClausesDef(atom, col);
    assertTrue(col.size() == 7);
    // atom → x < y  =  ¬atom ∨ x < y, where x < y = ¬x≥3 ∨ y≥4 AND ¬x≥4 ∨ y≥5 AND ¬x≥5 ∨ y≥6
    assertTrue(col.contains("¬x≥3 ∨ y≥4 ∨ ¬myvar"));
    assertTrue(col.contains("¬x≥4 ∨ y≥5 ∨ ¬myvar"));
    assertTrue(col.contains("¬x≥5 ∨ y≥6 ∨ ¬myvar"));
    // x < y → atom  =  x ≥ y ∨ atom, where  x ≥ y = x≥3 AND ¬y≥4 ∨ x≥4 AND ¬y≥5 ∨ x≥5 AND ¬y≥6
    assertTrue(col.contains("x≥3 ∨ myvar"));
    assertTrue(col.contains("x≥4 ∨ ¬y≥4 ∨ myvar"));
    assertTrue(col.contains("x≥5 ∨ ¬y≥5 ∨ myvar"));
    assertTrue(col.contains("¬y≥6 ∨ myvar"));
  }

  @Test
  public void testBasics() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x[i,j] :: Int ∈ {0..10} for i ∈ {1..3}, j ∈ {i+1..4}", vars);
    ParamRangeVar x = vars.queryParametrisedRangeVariable("x");
    Substitution subst = new Substitution("i", InputReader.readPExpressionFromString("a+j"),
                                          "j", InputReader.readPExpressionFromString("b"));
    QuantifiedRangeVariable xajb = new QuantifiedRangeVariable(x, subst);
    QuantifiedRangeVariable xij = new QuantifiedRangeVariable(x, new Substitution());
    Geq geq = new Geq(xajb, xij, true);    // x[a+j,c] ≥ x[i,j]

    assertTrue(geq.toString().equals("x[a+j,b] ≥ x[i,j]"));
    assertTrue(geq.negate().toString().equals("x[a+j,b] < x[i,j]"));
    assertTrue(geq.queryAtom() == null);
    assertFalse(geq.queryClosed());

    subst = new Substitution("a", InputReader.readPExpressionFromString("3"),
                             "b", InputReader.readPExpressionFromString("i"));
    Formula form = geq.substitute(subst);
    assertTrue(form.toString().equals("x[j+3,i] ≥ x[i,j]"));
    assertFalse(form.queryClosed());

    form = form.instantiate(new Assignment("i", 7, "j", 4));
    assertTrue(form.toString().equals("x[7,7] ≥ x[7,4]"));
    assertTrue(form.queryClosed());
    assertTrue(form.negate().toString().equals("x[7,7] < x[7,4]"));
  }
}

