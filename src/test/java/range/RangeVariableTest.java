import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
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
    Variable f = new Variable("FALSE");
    RangeVariable x = new RangeVariable("x", 1, 5, f, t);
    assertTrue(x.queryMinimum() == 1);
    assertTrue(x.queryMaximum() == 5);
    assertTrue(x.toString().equals("x"));
    assertTrue(x.queryRangeDescription().equals("{1..5}"));
    assertTrue(x.queryGeqVariable(0).equals(t));
    assertTrue(x.queryGeqVariable(1).equals(t));
    assertTrue(x.queryGeqVariable(2).toString().equals("x≥2"));
    assertTrue(x.queryGeqVariable(3).toString().equals("x≥3"));
    assertTrue(x.queryGeqVariable(4).toString().equals("x≥4"));
    assertTrue(x.queryGeqVariable(5).toString().equals("x≥5"));
    assertTrue(x.queryGeqVariable(6).equals(f));
  }

  @Test
  public void testSimpleVariableClauses() {
    Variable.reset();
    Variable t = new Variable("TRUE");
    Variable f = new Variable("FALSE");
    RangeVariable x = new RangeVariable("x", 1, 5, f, t);
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
    Variable f = new Variable("FALSE");
    RangeVariable x = new RangeVariable(parameter, f, t);
    assertTrue(x.queryMinimum() == 1);
    assertTrue(x.queryMaximum() == 5);
    assertTrue(x.toString().equals("x"));
    assertTrue(x.queryRangeDescription().equals("{1..5} with x < 3 ∨ 4 < x"));
    assertTrue(x.queryGeqVariable(0).equals(t));
    assertTrue(x.queryGeqVariable(1).equals(t));
    assertTrue(x.queryGeqVariable(2).toString().equals("x≥2"));
    assertTrue(x.queryGeqVariable(5).toString().equals("x≥5"));
    assertTrue(x.queryGeqVariable(3).equals(x.queryGeqVariable(5)));
    assertTrue(x.queryGeqVariable(5).equals(x.queryGeqVariable(5)));
    assertTrue(x.queryGeqVariable(6).equals(f));
  }

  @Test
  public void testWithoutMiddleRangeClauses() throws ParserException {
    Variable.reset();
    Parameter parameter = InputReader.readParameterFromString("x ∈ {1..5} with x ≤ 2 ∨ x > 4");
    Variable t = new Variable("TRUE");
    Variable f = new Variable("FALSE");
    RangeVariable x = new RangeVariable(parameter, f, t);
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
    Variable f = new Variable("FALSE");
    RangeVariable x = new RangeVariable(parameter, f, t);
    assertTrue(x.queryMinimum() == 2);
    assertTrue(x.queryMaximum() == 4);
    assertTrue(x.queryGeqVariable(0).equals(t));
    assertTrue(x.queryGeqVariable(1).equals(t));
    assertTrue(x.queryGeqVariable(2).equals(t));
    assertTrue(x.queryGeqVariable(3).toString().equals("x≥3"));
    assertTrue(x.queryGeqVariable(4).toString().equals("x≥4"));
    assertTrue(x.queryGeqVariable(5).equals(f));
    assertTrue(x.queryGeqVariable(6).equals(f));
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
    Variable f = new Variable("FALSE");
    RangeVariable x = new RangeVariable(parameter, f, t);
    assertTrue(x.queryMinimum() == 3);
    assertTrue(x.queryMaximum() == 3);
    assertTrue(x.queryGeqVariable(2).equals(t));
    assertTrue(x.queryGeqVariable(3).equals(t));
    assertTrue(x.queryGeqVariable(4).equals(f));
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    assertTrue(col.size() == 0);
  }

  @Test(expected = java.lang.Error.class)
  public void testRVWithEmptyRange() throws ParserException {
    Variable.reset();
    Parameter parameter = InputReader.readParameterFromString("x ∈ {1..5} with x < 0 ∨ x > 12");
    Variable t = new Variable("TRUE");
    Variable f = new Variable("FALSE");
    RangeVariable x = new RangeVariable(parameter, f, t);
  }

  @Test(expected = java.lang.Error.class)
  public void testRVWithExtraParameterInConstraint() throws ParserException {
    Parameter parameter = InputReader.readParameterFromString("x ∈ {1..5} with x < y");
    Variable t = new Variable("TRUE");
    Variable f = new Variable("FALSE");
    RangeVariable x = new RangeVariable(parameter, f, t);
  }

  @Test
  public void testSolution() throws ParserException {
    Variable t = new Variable("TRUE");
    Variable f = new Variable("FALSE");
    RangeVariable vi = new RangeVariable("x", -1, 4, f, t);
    TreeSet<Integer> truevars = new TreeSet<Integer>();
    truevars.add(t.queryIndex());
    truevars.add(vi.queryGeqVariable(0).queryIndex());
    truevars.add(vi.queryGeqVariable(1).queryIndex());
    Solution solution = new Solution(truevars);
    assertTrue(vi.getValue(solution) == 1);
  }
}

