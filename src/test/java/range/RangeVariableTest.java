import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Solution;
import logic.parameter.Parameter;
import logic.range.RangeVariable;
import language.parser.InputReader;
import language.parser.ParserException;
import java.util.TreeSet;

public class RangeVariableTest {
  @Test
  public void testSimpleRVBasics() {
    Variable t = new Variable("TRUE");
    RangeVariable x = new RangeVariable("x", 1, 5, t);
    assertTrue(x.queryMinimum() == 1);
    assertTrue(x.queryMaximum() == 5);
    assertTrue(x.toString().equals("x"));
    assertTrue(x.queryRangeDescription().equals("{1..5}"));
    assertTrue(x.queryGeqAtom(0).equals(new Atom(t, true)));
    assertTrue(x.queryGeqAtom(1).equals(new Atom(t, true)));
    assertTrue(x.queryGeqAtom(2).toString().equals("x≥2"));
    assertTrue(x.queryGeqAtom(3).toString().equals("x≥3"));
    assertTrue(x.queryGeqAtom(4).toString().equals("x≥4"));
    assertTrue(x.queryGeqAtom(5).toString().equals("x≥5"));
    assertTrue(x.queryGeqAtom(6).equals(new Atom(t, false)));
  }

  @Test
  public void testSimpleVariableClauses() {
    Variable.reset();
    Variable t = new Variable("TRUE");
    RangeVariable x = new RangeVariable("x", 1, 5, t);
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    assertTrue(col.size() == 3);
    assertTrue(col.contains("¬x≥5 ∨ x≥4"));
    assertTrue(col.contains("¬x≥4 ∨ x≥3"));
    assertTrue(col.contains("¬x≥3 ∨ x≥2"));
  }

  @Test
  public void testRVWithoutMiddleRangeBasics() throws ParserException {
    Parameter parameter = InputReader.readParameterFromString("x ∈ {1..5} with x ≤ 2 ∨ x > 4");
    Variable t = new Variable("TRUE");
    RangeVariable x = new RangeVariable(parameter, t);
    assertTrue(x.queryMinimum() == 1);
    assertTrue(x.queryMaximum() == 5);
    assertTrue(x.toString().equals("x"));
    assertTrue(x.queryRangeDescription().equals("{1..5} with x < 3 ∨ 4 < x"));
    assertTrue(x.queryGeqAtom(0).equals(new Atom(t, true)));
    assertTrue(x.queryGeqAtom(1).equals(new Atom(t, true)));
    assertTrue(x.queryGeqAtom(2).toString().equals("x≥2"));
    assertTrue(x.queryGeqAtom(5).toString().equals("x≥5"));
    assertTrue(x.queryGeqAtom(3).equals(x.queryGeqAtom(5)));
    assertTrue(x.queryGeqAtom(5).equals(x.queryGeqAtom(5)));
    assertTrue(x.queryGeqAtom(6).equals(new Atom(t, false)));
  }

  @Test
  public void testWithoutMiddleRangeClauses() throws ParserException {
    Variable.reset();
    Parameter parameter = InputReader.readParameterFromString("x ∈ {1..5} with x ≤ 2 ∨ x > 4");
    Variable t = new Variable("TRUE");
    RangeVariable x = new RangeVariable(parameter, t);
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬x≥5 ∨ x≥2"));
  }

  @Test
  public void testRVWithOnlyMiddleRange() throws ParserException {
    Variable.reset();
    Parameter parameter = InputReader.readParameterFromString("x ∈ {1..5} with x ≥ 2 ∧ x ≤ 4");
    Variable t = new Variable("TRUE");
    RangeVariable x = new RangeVariable(parameter, t);
    assertTrue(x.queryMinimum() == 2);
    assertTrue(x.queryMaximum() == 4);
    assertTrue(x.queryGeqAtom(0).equals(new Atom(t, true)));
    assertTrue(x.queryGeqAtom(1).equals(new Atom(t, true)));
    assertTrue(x.queryGeqAtom(2).equals(new Atom(t, true)));
    assertTrue(x.queryGeqAtom(3).toString().equals("x≥3"));
    assertTrue(x.queryGeqAtom(4).toString().equals("x≥4"));
    assertTrue(x.queryGeqAtom(5).equals(new Atom(t, false)));
    assertTrue(x.queryGeqAtom(6).equals(new Atom(t, false)));
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬x≥4 ∨ x≥3"));
  }

  @Test
  public void testRVWithSingularRange() throws ParserException {
    Variable.reset();
    Parameter parameter = InputReader.readParameterFromString("x ∈ {1..5} with x = 3");
    Variable t = new Variable("TRUE");
    RangeVariable x = new RangeVariable(parameter, t);
    assertTrue(x.queryMinimum() == 3);
    assertTrue(x.queryMaximum() == 3);
    assertTrue(x.queryGeqAtom(2).equals(new Atom(t, true)));
    assertTrue(x.queryGeqAtom(3).equals(new Atom(t, true)));
    assertTrue(x.queryGeqAtom(4).equals(new Atom(t, false)));
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    assertTrue(col.size() == 0);
  }

  @Test(expected = java.lang.Error.class)
  public void testRVWithEmptyRange() throws ParserException {
    Variable.reset();
    Parameter parameter = InputReader.readParameterFromString("x ∈ {1..5} with x < 0 ∨ x > 12");
    Variable t = new Variable("TRUE");
    RangeVariable x = new RangeVariable(parameter, t);
  }

  @Test(expected = java.lang.Error.class)
  public void testRVWithExtraParameterInConstraint() throws ParserException {
    Parameter parameter = InputReader.readParameterFromString("x ∈ {1..5} with x < y");
    Variable t = new Variable("TRUE");
    RangeVariable x = new RangeVariable(parameter, t);
  }

  @Test
  public void testSolution() throws ParserException {
    Variable t = new Variable("TRUE");
    RangeVariable vi = new RangeVariable("x", -1, 4, t);
    TreeSet<Integer> truevars = new TreeSet<Integer>();
    truevars.add(t.queryIndex());
    truevars.add(vi.queryGeqAtom(0).queryIndex());
    truevars.add(vi.queryGeqAtom(1).queryIndex());
    Solution solution = new Solution(truevars);
    assertTrue(vi.getValue(solution) == 1);
  }
}

