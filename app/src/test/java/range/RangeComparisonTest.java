import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.number.range.*;
import java.util.ArrayList;
import java.util.TreeSet;

public class RangeComparisonTest {
  private Atom truth() {
    return new Atom(new Variable("TRUE"), true);
  }

  private TreeSet<String> wrap(ArrayList<Clause> clauses) {
    TreeSet<String> ret = new TreeSet<String>();
    for (int i = 0; i < clauses.size(); i++) ret.add(clauses.get(i).toString());
    return ret;
  }

  private TreeSet<String> makeKind(String kind, RangeInteger a, RangeInteger b) {
    if (kind.equals("geq")) return wrap(RangeComparison.generateGeqClauses(a, b));
    if (kind.equals("smaller")) return wrap(RangeComparison.generateSmallerClauses(a, b));
    if (kind.equals("equal")) return wrap(RangeComparison.generateEqualClauses(a, b));
    if (kind.equals("neq")) return wrap(RangeComparison.generateNeqClauses(a, b));
    return null;
  }

  /** Helper function for tests that do a comparison between a variable and a constant */
  private TreeSet<String> setupIntegerRangeTest(int min, int max, String kind, int constant) {
    RangeVariable x = new RangeVariable("x", min, max, truth());
    RangeConstant c = new RangeConstant(constant, truth());
    return makeKind(kind, x, c);
  }

  /** Helper function for tests that do a comparison between a constant and a variable */
  private TreeSet<String> setupReverseIntegerRangeTest(int min, int max, String kind, int constant) {
    RangeVariable x = new RangeVariable("x", min, max, truth());
    RangeConstant c = new RangeConstant(constant, truth());
    return makeKind(kind, c, x);
  }

  /** Helper function for tests that compare two range variables in different ranges */
  private TreeSet<String> setupRangeRangeTest(int min1, int max1, String kind, int min2, int max2) {
    RangeVariable x = new RangeVariable("x", min1, max1, truth());
    RangeVariable y = new RangeVariable("y", min2, max2, truth());
    return makeKind(kind, x, y);
  }

  @Test
  public void testGeqBelowRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "geq", 1);
    assertTrue(test.size() == 0);
  }

  @Test
  public void testAddClausesGeqMinimumRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "geq", 3);
    assertTrue(test.size() == 0);
  }

  @Test
  public void testAddClausesGeqMiddleOfRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "geq", 4);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("x≥4"));
  }

  @Test
  public void testAddClausesGeqMaximumRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "geq", 6);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("x≥6"));
  }

  @Test
  public void testAddClausesGeqAboveRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "geq", 7);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesSmallerBelowRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "smaller", 1);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesSmallerMinimumRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "smaller", 3);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesSmallerMiddleOfRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "smaller", 4);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("¬x≥4"));
  }

  @Test
  public void testAddClausesSmallerMaximumRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "smaller", 6);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("¬x≥6"));
  }

  @Test
  public void testAddClausesSmallerAboveRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "smaller", 7);
    assertTrue(test.size() == 0);
  }

  @Test
  public void testAddClausesReverseGeqBelowRange() {
    TreeSet<String> test = setupReverseIntegerRangeTest(3, 6, "geq", 1);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesReverseGeqMinimumRange() {
    TreeSet<String> test = setupReverseIntegerRangeTest(3, 6, "geq", 3);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("¬x≥4"));
  }

  @Test
  public void testAddClausesReverseGeqMiddleOfRange() {
    TreeSet<String> test = setupReverseIntegerRangeTest(3, 6, "geq", 4);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("¬x≥5"));
  }

  @Test
  public void testAddClausesReverseGeqMaximumRange() {
    TreeSet<String> test = setupReverseIntegerRangeTest(3, 6, "geq", 6);
    assertTrue(test.size() == 0);
  }

  @Test
  public void testAddClausesReverseGeqAboveRange() {
    TreeSet<String> test = setupReverseIntegerRangeTest(3, 6, "geq", 7);
    assertTrue(test.size() == 0);
  }

  @Test
  public void testAddClausesReverseSmallerBelowRange() {
    TreeSet<String> test = setupReverseIntegerRangeTest(3, 6, "smaller", 1);
    assertTrue(test.size() == 0);
  }

  @Test
  public void testAddClausesReverseSmallerMinimumRange() {
    TreeSet<String> test = setupReverseIntegerRangeTest(3, 6, "smaller", 3);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("x≥4"));
  }

  @Test
  public void testAddClausesReverseSmallerMiddleOfRange() {
    TreeSet<String> test = setupReverseIntegerRangeTest(3, 6, "smaller", 4);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("x≥5"));
  }

  @Test
  public void testAddClausesReverseSmallerMaximumRange() {
    TreeSet<String> test = setupReverseIntegerRangeTest(3, 6, "smaller", 6);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesReverseSmallerAboveRange() {
    TreeSet<String> test = setupReverseIntegerRangeTest(3, 6, "smaller", 7);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesGeqLowerRangeVersusHigherRange() {
    TreeSet<String> test = setupRangeRangeTest(1, 5, "geq", 3, 8);
    assertTrue(test.size() == 4);
    assertTrue(test.contains("x≥3"));
    assertTrue(test.contains("x≥4 ∨ ¬y≥4"));
    assertTrue(test.contains("x≥5 ∨ ¬y≥5"));
    assertTrue(test.contains("¬y≥6"));
  }

  @Test
  public void testAddClausesGeqHigherRangeVersusLowerRange() {
    TreeSet<String> test = setupRangeRangeTest(3, 8, "geq", 1, 5);
    assertTrue(test.size() == 2);
    assertTrue(test.contains("x≥4 ∨ ¬y≥4"));
    assertTrue(test.contains("x≥5 ∨ ¬y≥5"));
  }

  @Test
  public void testAddClausesGeqSameRange() {
    TreeSet<String> test = setupRangeRangeTest(1, 4, "geq", 1, 4);
    assertTrue(test.size() == 3);
    assertTrue(test.contains("x≥2 ∨ ¬y≥2"));
    assertTrue(test.contains("x≥3 ∨ ¬y≥3"));
    assertTrue(test.contains("x≥4 ∨ ¬y≥4"));
  }

  @Test
  public void testAddClausesGeqOuterVersusInnerRange() {
    TreeSet<String> test = setupRangeRangeTest(1, 8, "geq", 3, 5);
    assertTrue(test.size() == 3);
    assertTrue(test.contains("x≥3"));
    assertTrue(test.contains("x≥4 ∨ ¬y≥4"));
    assertTrue(test.contains("x≥5 ∨ ¬y≥5"));
  }

  @Test
  public void testAddClausesGeqInnerVersusOuterRange() {
    TreeSet<String> test = setupRangeRangeTest(3, 5, "geq", 1, 8);
    assertTrue(test.size() == 3);
    assertTrue(test.contains("¬y≥6"));
    assertTrue(test.contains("x≥4 ∨ ¬y≥4"));
    assertTrue(test.contains("x≥5 ∨ ¬y≥5"));
  }

  @Test
  public void testAddClausesSmallerLowerRangeVersusHigherRange() {
    TreeSet<String> test = setupRangeRangeTest(1, 5, "smaller", 3, 8);
    assertTrue(test.size() == 3);
    assertTrue(test.contains("¬x≥3 ∨ y≥4"));
    assertTrue(test.contains("¬x≥4 ∨ y≥5"));
    assertTrue(test.contains("¬x≥5 ∨ y≥6"));
  }

  @Test
  public void testAddClausesSmallerHigherRangeVersusLowerRange() {
    TreeSet<String> test = setupRangeRangeTest(3, 8, "smaller", 1, 5);
    assertTrue(test.size() == 3);
    assertTrue(test.contains("y≥4"));
    assertTrue(test.contains("¬x≥4 ∨ y≥5"));
    assertTrue(test.contains("¬x≥5"));
  }

  @Test
  public void testAddClausesSmallerSameRange() {
    TreeSet<String> test = setupRangeRangeTest(1, 4, "smaller", 1, 4);
    assertTrue(test.size() == 4);
    assertTrue(test.contains("y≥2"));
    assertTrue(test.contains("¬x≥2 ∨ y≥3"));
    assertTrue(test.contains("¬x≥3 ∨ y≥4"));
    assertTrue(test.contains("¬x≥4"));
  }

  @Test
  public void testAddClausesSmallerOuterVersusInnerRange() {
    TreeSet<String> test = setupRangeRangeTest(1, 8, "smaller", 3, 5);
    assertTrue(test.size() == 3);
    assertTrue(test.contains("¬x≥3 ∨ y≥4"));
    assertTrue(test.contains("¬x≥4 ∨ y≥5"));
    assertTrue(test.contains("¬x≥5"));
  }

  @Test
  public void testAddClausesSmallerInnerVersusOuterRange() {
    TreeSet<String> test = setupRangeRangeTest(3, 5, "smaller", 1, 8);
    assertTrue(test.size() == 3);
    assertTrue(test.contains("y≥4"));
    assertTrue(test.contains("¬x≥4 ∨ y≥5"));
    assertTrue(test.contains("¬x≥5 ∨ y≥6"));
  }

  @Test
  public void testAddClausesEqualsBelowRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "equal", 1); 
    assertTrue(test.size() == 1); 
    assertTrue(test.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesEqualsMinimumRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "equal", 3); 
    assertTrue(test.size() == 1); 
    assertTrue(test.contains("¬x≥4"));
  }

  @Test
  public void testAddClausesEqualsMiddleOfRange() {
    TreeSet<String> test = setupReverseIntegerRangeTest(3, 6, "equal", 4); 
    assertTrue(test.size() == 2); 
    assertTrue(test.contains("x≥4"));
    assertTrue(test.contains("¬x≥5"));
  }

  @Test
  public void testAddClausesEqualsMaximumRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "equal", 6);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("x≥6"));
  }

  @Test
  public void testAddClausesEqualsAboveRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "equal", 7);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesEqualsDefinitelyEqual() {
    TreeSet<String> test = setupReverseIntegerRangeTest(3, 3, "equal", 3);
    assertTrue(test.size() == 0);
  }

  @Test
  public void testAddClausesNeqBelowRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "neq", 1);
    assertTrue(test.size() == 0);
  }

  @Test
  public void testAddClausesNeqMinimumRange() {
    TreeSet<String> test = setupReverseIntegerRangeTest(3, 6, "neq", 3);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("x≥4"));
  }

  @Test
  public void testAddClausesNeqMiddleOfRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "neq", 4);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("x≥5 ∨ ¬x≥4"));
  }

  @Test
  public void testAddClausesNeqMaximumRange() {
    TreeSet<String> test = setupIntegerRangeTest(3, 6, "neq", 6);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("¬x≥6"));
  }

  @Test
  public void testAddClausesNeqAboveRange() {
    TreeSet<String> test = setupReverseIntegerRangeTest(3, 6, "neq", 7);
    assertTrue(test.size() == 0);
  }

  @Test
  public void testAddClausesNeqDefinitelyEqual() {
    TreeSet<String> test = setupIntegerRangeTest(3, 3, "neq", 3);
    assertTrue(test.size() == 1);
    assertTrue(test.contains("¬TRUE"));
  }

  @Test
  public void testAddClausesEqualsOverlappingButNotEqualRange() {
    TreeSet<String> test = setupRangeRangeTest(1, 5, "equal", 3, 8);
    assertTrue(test.size() == 6);
    assertTrue(test.contains("x≥3"));
    assertTrue(test.contains("x≥4 ∨ ¬y≥4"));
    assertTrue(test.contains("x≥5 ∨ ¬y≥5"));
    assertTrue(test.contains("¬y≥6"));
    assertTrue(test.contains("¬x≥4 ∨ y≥4"));
    assertTrue(test.contains("¬x≥5 ∨ y≥5"));
  }

  @Test
  public void testAddClausesEqualsSameRange() {
    TreeSet<String> test = setupRangeRangeTest(1, 4, "equal", 1, 4);
    assertTrue(test.size() == 6);
    assertTrue(test.contains("x≥2 ∨ ¬y≥2"));
    assertTrue(test.contains("x≥3 ∨ ¬y≥3"));
    assertTrue(test.contains("x≥4 ∨ ¬y≥4"));
    assertTrue(test.contains("¬x≥2 ∨ y≥2"));
    assertTrue(test.contains("¬x≥3 ∨ y≥3"));
    assertTrue(test.contains("¬x≥4 ∨ y≥4"));
  }

  @Test
  public void testAddClausesEqualsOuterVersusInnerRange() {
    TreeSet<String> test = setupRangeRangeTest(1, 8, "equal", 3, 5);
    assertTrue(test.size() == 6);
    assertTrue(test.contains("x≥3"));
    assertTrue(test.contains("x≥4 ∨ ¬y≥4"));
    assertTrue(test.contains("x≥5 ∨ ¬y≥5"));
    assertTrue(test.contains("¬x≥6"));
    assertTrue(test.contains("¬x≥4 ∨ y≥4"));
    assertTrue(test.contains("¬x≥5 ∨ y≥5"));
  }

  @Test
  public void testAddClausesNeqLowerRangeVersusHigherRange() {
    Variable.reset();
    TreeSet<String> test = setupRangeRangeTest(1, 5, "neq", 3, 8);
    assertTrue(test.size() == 3);
    assertTrue(test.contains("x≥4 ∨ ¬x≥3 ∨ y≥4"));
    assertTrue(test.contains("x≥5 ∨ ¬x≥4 ∨ y≥5 ∨ ¬y≥4"));
    assertTrue(test.contains("¬x≥5 ∨ y≥6 ∨ ¬y≥5"));
  }

  @Test
  public void testAddClausesNeqSameRange() {
    TreeSet<String> test = setupRangeRangeTest(1, 4, "neq", 1, 4);
    assertTrue(test.size() == 4);
    assertTrue(test.contains("x≥2 ∨ y≥2"));
    assertTrue(test.contains("x≥3 ∨ ¬x≥2 ∨ y≥3 ∨ ¬y≥2"));
    assertTrue(test.contains("x≥4 ∨ ¬x≥3 ∨ y≥4 ∨ ¬y≥3"));
    assertTrue(test.contains("¬x≥4 ∨ ¬y≥4"));
  }

  @Test
  public void testAddClausesNeqOuterVersusInnerRange() {
    TreeSet<String> test = setupRangeRangeTest(1, 8, "neq", 3, 5);
    assertTrue(test.size() == 3);
    assertTrue(test.contains("x≥4 ∨ ¬x≥3 ∨ y≥4"));
    assertTrue(test.contains("x≥5 ∨ ¬x≥4 ∨ y≥5 ∨ ¬y≥4"));
    assertTrue(test.contains("x≥6 ∨ ¬x≥5 ∨ ¬y≥5"));
  }
}

