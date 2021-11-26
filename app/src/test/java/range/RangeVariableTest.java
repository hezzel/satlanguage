import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Solution;
import logic.parameter.Parameter;
import logic.number.range.RangeVariable;
import language.parser.InputReader;
import language.parser.ParserException;
import java.util.TreeSet;

public class RangeVariableTest {
  private Atom truth() { return new Atom(new Variable("TRUE"), true); }

  @Test
  public void testSimpleRVBasics() {
    RangeVariable x = new RangeVariable("x", 1, 5, truth());
    assertTrue(x.queryMinimum() == 1);
    assertTrue(x.queryMaximum() == 5);
    assertTrue(x.toString().equals("x"));
    assertTrue(x.queryRangeDescription().equals("{1..5}"));
    assertTrue(x.queryGeqAtom(0).equals(truth()));
    assertTrue(x.queryGeqAtom(1).equals(truth()));
    assertTrue(x.queryGeqAtom(2).toString().equals("x≥2"));
    assertTrue(x.queryGeqAtom(3).toString().equals("x≥3"));
    assertTrue(x.queryGeqAtom(4).toString().equals("x≥4"));
    assertTrue(x.queryGeqAtom(5).toString().equals("x≥5"));
    assertTrue(x.queryGeqAtom(6).equals(truth().negate()));
  }

  @Test
  public void testSimpleVariableClauses() {
    Variable.reset();
    RangeVariable x = new RangeVariable("x", 1, 5, truth());
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
    RangeVariable x = new RangeVariable(parameter, truth());
    assertTrue(x.queryMinimum() == 1);
    assertTrue(x.queryMaximum() == 5);
    assertTrue(x.toString().equals("x"));
    assertTrue(x.queryRangeDescription().equals("{1..5} with x < 3 ∨ 4 < x"));
    assertTrue(x.queryGeqAtom(0).equals(truth()));
    assertTrue(x.queryGeqAtom(1).equals(truth()));
    assertTrue(x.queryGeqAtom(2).toString().equals("x≥2"));
    assertTrue(x.queryGeqAtom(5).toString().equals("x≥5"));
    assertTrue(x.queryGeqAtom(3).equals(x.queryGeqAtom(5)));
    assertTrue(x.queryGeqAtom(5).equals(x.queryGeqAtom(5)));
    assertTrue(x.queryGeqAtom(6).equals(truth().negate()));
  }

  @Test
  public void testWithoutMiddleRangeClauses() throws ParserException {
    Variable.reset();
    Parameter parameter = InputReader.readParameterFromString("x ∈ {1..5} with x ≤ 2 ∨ x > 4");
    RangeVariable x = new RangeVariable(parameter, truth());
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬x≥5 ∨ x≥2"));
  }

  @Test
  public void testRVWithOnlyMiddleRange() throws ParserException {
    Variable.reset();
    Parameter parameter = InputReader.readParameterFromString("x ∈ {1..5} with x ≥ 2 ∧ x ≤ 4");
    RangeVariable x = new RangeVariable(parameter, truth());
    assertTrue(x.queryMinimum() == 2);
    assertTrue(x.queryMaximum() == 4);
    assertTrue(x.queryGeqAtom(0).equals(truth()));
    assertTrue(x.queryGeqAtom(1).equals(truth()));
    assertTrue(x.queryGeqAtom(2).equals(truth()));
    assertTrue(x.queryGeqAtom(3).toString().equals("x≥3"));
    assertTrue(x.queryGeqAtom(4).toString().equals("x≥4"));
    assertTrue(x.queryGeqAtom(5).equals(truth().negate()));
    assertTrue(x.queryGeqAtom(6).equals(truth().negate()));
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬x≥4 ∨ x≥3"));
  }

  @Test
  public void testRVWithSingularRange() throws ParserException {
    Variable.reset();
    Parameter parameter = InputReader.readParameterFromString("x ∈ {1..5} with x = 3");
    RangeVariable x = new RangeVariable(parameter, truth());
    assertTrue(x.queryMinimum() == 3);
    assertTrue(x.queryMaximum() == 3);
    assertTrue(x.queryGeqAtom(2).equals(truth()));
    assertTrue(x.queryGeqAtom(3).equals(truth()));
    assertTrue(x.queryGeqAtom(4).equals(truth().negate()));
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    assertTrue(col.size() == 0);
  }

  @Test(expected = java.lang.Error.class)
  public void testRVWithEmptyRange() throws ParserException {
    Variable.reset();
    Parameter parameter = InputReader.readParameterFromString("x ∈ {1..5} with x < 0 ∨ x > 12");
    RangeVariable x = new RangeVariable(parameter, truth());
  }

  @Test(expected = java.lang.Error.class)
  public void testRVWithExtraParameterInConstraint() throws ParserException {
    Parameter parameter = InputReader.readParameterFromString("x ∈ {1..5} with x < y");
    RangeVariable x = new RangeVariable(parameter, truth());
  }

  @Test
  public void testSolution() throws ParserException {
    RangeVariable vi = new RangeVariable("x", -1, 4, truth());
    TreeSet<Integer> truevars = new TreeSet<Integer>();
    truevars.add(truth().queryIndex());
    truevars.add(vi.queryGeqAtom(0).queryIndex());
    truevars.add(vi.queryGeqAtom(1).queryIndex());
    Solution solution = new Solution(truevars);
    assertTrue(vi.getValue(solution) == 1);
  }
}

