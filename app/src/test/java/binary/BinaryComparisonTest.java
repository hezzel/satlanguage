import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.number.binary.*;
import java.util.ArrayList;
import java.util.TreeSet;

public class BinaryComparisonTest {
  private Atom truth() {
    return new Atom(new Variable("TRUE"), true);
  }

  private ClauseCollector wrap(ArrayList<Clause> clauses) {
    ClauseCollector ret = new ClauseCollector();
    for (int i = 0; i < clauses.size(); i++) ret.addClause(clauses.get(i));
    return ret;
  }

  private ClauseCollector makeKind(String kind, BinaryInteger a, BinaryInteger b) {
    if (kind.equals("geq")) return wrap(BinaryComparison.generateGeqClauses(a, b));
    if (kind.equals("smaller")) return wrap(BinaryComparison.generateSmallerClauses(a, b));
    if (kind.equals("equal")) return wrap(BinaryComparison.generateEqualClauses(a, b));
    if (kind.equals("neq")) return wrap(BinaryComparison.generateNeqClauses(a, b));
    return null;
  }

  private void setValue(ClauseCollector col, String name, int len, int value) {
    BinaryConstant c = new BinaryConstant(value, new Atom(new Variable("TRUE"), true));
    for (int i = 0; i < len; i++) {
      col.force(name + "⟨" + i + "⟩", c.queryBit(i).toString().equals("TRUE"));
    }   
    col.force(name + "⟨-⟩", c.queryNegativeBit().toString().equals("TRUE"));
  }

  /** Helper function for tests that do a comparison between a variable and a constant */
  private ClauseCollector setupVariableConstantTest(int len, boolean allowNegative, String kind,
                                                    int constant) {
    BinaryVariable x = new BinaryVariable("x", len, allowNegative, truth());
    BinaryConstant c = new BinaryConstant(constant, truth());
    return makeKind(kind, x, c);
  }

  /** Helper function for tests that do a comparison between a constant and a variable */
  private ClauseCollector setupConstantVariableTest(int len, boolean allowNegative, String kind,
                                                    int constant) {
    BinaryVariable x = new BinaryVariable("x", len, allowNegative, truth());
    BinaryConstant c = new BinaryConstant(constant, truth());
    return makeKind(kind, c, x);
  }

  /** Helper function for tests that compare two variables */
  private ClauseCollector setupVariableVariableTest(int len1, boolean neg1, String kind,
                                                    int len2, boolean neg2) {
    BinaryVariable x = new BinaryVariable("x", len1, neg1, truth());
    BinaryVariable y = new BinaryVariable("y", len2, neg2, truth());
    return makeKind(kind, x, y);
  }

  private void testCompare(int len1, boolean neg1, int value1, String kind,
                           int len2, boolean neg2, int value2, boolean outcome) {
    ClauseCollector col = setupVariableVariableTest(len1, neg1, kind, len2, neg2);
    col.force("TRUE", true);
    setValue(col, "x", len1, value1);
    setValue(col, "y", len2, value2);
    if (outcome) assertTrue(col.checkSatisfiable());
    else assertFalse(col.checkSatisfiable());
  }

  private void testCompare(int len, boolean neg, int value, String kind, int constant,
                           boolean outcome) {
    ClauseCollector col = setupVariableConstantTest(len, neg, kind, constant);
    col.force("TRUE", true);
    setValue(col, "x", len, value);
    if (outcome) assertTrue(col.checkSatisfiable());
    else assertFalse(col.checkSatisfiable());
  }

  private void testCompare(int constant, String kind, int len, boolean neg, int value,
                           boolean outcome) {
    ClauseCollector col = setupConstantVariableTest(len, neg, kind, constant);
    col.force("TRUE", true);
    setValue(col, "x", len, value);
    if (outcome) assertTrue(col.checkSatisfiable());
    else assertFalse(col.checkSatisfiable());
  }

  @Test
  public void testGeqPositiveVariables() {
    testCompare(5, true, 16, "geq", 4, true, 12, true);
    testCompare(5, false, 16, "geq", 4, false, 12, true);
    testCompare(5, false, 16, "geq", 4, true, 12, true);
    testCompare(5, true, 16, "geq", 4, false, 12, true);
    testCompare(7, false, 93, "geq", 7, true, 93, true);
    testCompare(7, false, 93, "geq", 7, true, 94, false);
    testCompare(4, true, 12, "geq", 5, false, 16, false);
  }

  @Test
  public void testGeqNegativeAndMixedVariables() {
    testCompare(8, true, 88, "geq", 8, true, -100, true);
    testCompare(8, true, -100, "geq", 8, false, 88, false);
    testCompare(8, true, -12, "geq", 7, true, -79, true);
    testCompare(5, true, -13, "geq", 5, true, -13, true);
    testCompare(5, true, -30, "geq", 5, true, -13, false);
  }

  @Test
  public void testGeqVariablesAndConstants() {
    testCompare(8, true, 78, "geq", 69, true);
    testCompare(69, "geq", 8, true, 78, false);
    testCompare(8, false, 0, "geq", -1000, true);
    testCompare(-7, "geq", 6, true, -14, true);
    testCompare(-8, "geq", 6, true, -7, false);
    testCompare(8, true, -75, "geq", -75, true);
  }

  @Test
  public void testSmallerPositiveVariables() {
    testCompare(4, true, 12, "smaller", 5, false, 16, true);
    testCompare(6, false, 7, "smaller", 4, false, 12, true);
    testCompare(6, true, 15, "smaller", 4, false, 15, false);
    testCompare(2, false, 0, "smaller", 3, true, 1, true);
    testCompare(5, true, 16, "smaller", 4, true, 12, false);
    testCompare(5, false, 17, "smaller", 4, false, 12, false);
    testCompare(5, false, 19, "smaller", 4, true, 15, false);
  }

  @Test
  public void testSmallerNegativeAndMixedVariables() {
    testCompare(8, true, 88, "smaller", 8, true, -100, false);
    testCompare(8, true, -100, "smaller", 8, false, 88, true);
    testCompare(8, true, -12, "smaller", 7, true, -79, false);
    testCompare(8, true, -79, "smaller", 7, true, -20, true);
    testCompare(7, true, -50, "smaller", 7, true, -50, false);
  }

  @Test
  public void testSmallerVariablesAndConstants() {
    testCompare(8, true, 59, "smaller", 76, true);
    testCompare(69, "smaller", 8, true, 78, true);
    testCompare(8, false, 0, "smaller", -1000, false);
    testCompare(-7, "smaller", 6, true, -14, false);
    testCompare(8, true, -17, "smaller", 17, true);
    testCompare(8, true, -17, "smaller", -12, true);
    testCompare(-9, "smaller", 5, true, -3, true);
  }

  @Test
  public void testEqualsPositiveVariables() {
    testCompare(5, true, 19, "equal", 5, true, 19, true);
    testCompare(5, false, 19, "equal", 7, true, 19, true);
    testCompare(12, false, 100, "equal", 8, true, 17, false);
  }

  @Test
  public void testEqualsNegativeAndMixedVariables() {
    testCompare(8, true, -14, "equal", 6, true, -14, true);
    testCompare(4, true, -2, "equal", 5, true, 1, false);
    testCompare(7, true, -9, "equal", 7, false, 9, false);
    testCompare(0, true, 0, "equal", 2, false, 0, true);
  }

  @Test
  public void testEqualsVariableAndConstant() {
    testCompare(8, true, 69, "equal", 69, true);
    testCompare(8, true, -212, "equal", -212, true);
    testCompare(8, true, -212, "equal", -211, false);
    testCompare(26, "equal", 6, false, 26, true);
    testCompare(49, "equal", 6, false, 26, false);
  }

  @Test
  public void testNeqPositiveVariables() {
    testCompare(5, true, 19, "neq", 5, true, 19, false);
    testCompare(5, false, 19, "neq", 7, true, 19, false);
    testCompare(12, false, 100, "neq", 8, true, 17, true);
    testCompare(5, true, 13, "neq", 7, true, 14, true);
  }

  @Test
  public void testNeqNegativeAndMixedVariables() {
    testCompare(8, true, -14, "neq", 6, true, -14, false);
    testCompare(4, true, -2, "neq", 5, true, 1, true);
    testCompare(7, true, -9, "neq", 7, false, 9, true);
    testCompare(0, true, 0, "neq", 2, false, 0, false);
  }

  @Test
  public void testNeqVariableAndConstant() {
    testCompare(8, true, 69, "neq", 69, false);
    testCompare(8, true, -212, "neq", -212, false);
    testCompare(8, true, -212, "neq", -211, true);
    testCompare(26, "neq", 6, false, 26, false);
    testCompare(49, "neq", 6, false, 26, true);
  }
}

