import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.formula.*;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParseComparisonTest {
  @Test
  public void testReadSimpleGeq() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x :: Int ∈ {1..10}", vars);
    Formula form = InputReader.readFormulaFromString("x ≥ 6", vars);
    assertTrue(form.toString().equals("x ≥ 6"));
  }

  @Test
  public void testReadSimpleLeq() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x[i] :: Int ∈ {1..10} for i ∈ {1..5}", vars);
    Formula form = InputReader.readFormulaFromString("x[i] <= 6", vars);
    assertTrue(form.toString().equals("6 ≥ x[i]"));
  }

  @Test
  public void testReadSimpleSmaller() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x[i] :: Int ∈ {i..10} for i ∈ {1..5}", vars);
    Formula form = InputReader.readFormulaFromString("x[8]<x[a]", vars);
    assertTrue(form.toString().equals("x[8] < x[a]"));
  }

  @Test
  public void testReadSimpleGreater() throws ParserException {
    VariableList vars = new VariableList();
    Formula form = InputReader.readFormulaFromString("i > 6", vars);
    assertTrue(form.toString().equals("6 < i"));
  }

  @Test
  public void testReadSimpleEquals() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x[i] :: Int ∈ {1..10} for i ∈ {1..5}", vars);
    Formula form = InputReader.readFormulaFromString("3+i = x[4]", vars);
    assertTrue(form.toString().equals("3+i = x[4]"));
  }

  @Test
  public void testReadSimpleNeq() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("y :: Int ∈ {0..1}", vars);
    InputReader.declare("x[i] :: Int ∈ {1..10} for i ∈ {1..5}", vars);
    Formula form = InputReader.readFormulaFromString("y != x[4]", vars);
    assertTrue(form.toString().equals("y ≠ x[4]"));
  }

  @Test
  public void testEqualityWithExtraPluses() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x[i] :: Int ∈ {1..10} for i ∈ {1..5}", vars);
    Formula form = InputReader.readFormulaFromString("3+x[1]+7 = x[4]", vars);
    assertTrue(form.toString().equals("x[1] ⊕ 3+7 = x[4]"));
  }

  @Test
  public void testGeqWithMultipleVariables() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x[i] :: Int ∈ {1..10} for i ∈ {1..5}", vars);
    InputReader.declare("y    :: Int ∈ {1..10}", vars);
    Formula form = InputReader.readFormulaFromString("0 >= x[1] ⊕ y + x[4]", vars);
    assertTrue(form.toString().equals("0 ≥ x[1] ⊕ y ⊕ x[4]"));
  }

  @Test
  public void testSmallerWithMinus() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("y    :: Int ∈ {1..10}", vars);
    Formula form = InputReader.readFormulaFromString("y < i - 1", vars);
    assertTrue(form.toString().equals("y < i-1"));
  }

  @Test
  public void testNeqWithUnnecessaryRangePlus() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("y    :: Int ∈ {1..10}", vars);
    Formula form = InputReader.readFormulaFromString("y != i ⊕ 1", vars);
    assertTrue(form.toString().equals("y ≠ i+1"));
  }

  @Test
  public void testConditionalExpressions() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x    :: Bool", vars);
    InputReader.declare("y[i] :: Bool for i ∈ {1..5}", vars);
    InputReader.declare("z[i] :: Int ∈ {1..10} for i ∈ {1..5}", vars);
    Formula form = InputReader.readFormulaFromString("x?3 > (¬x → y[j]) ? z[a+1]", vars);
    assertTrue(form.toString().equals("(¬x → y[j]) ? z[a+1] < x ? 3"));
  }

  @Test
  public void testSumExpression() throws ParserException{
    VariableList vars = new VariableList();
    InputReader.declare("x[i] :: Bool for i ∈ {1..5}", vars);
    Formula form =
      InputReader.readFormulaFromString("Σ { x[i]?i | i ∈ {1..a} with i % 2 = 0 } ≥ 5", vars);
    assertTrue(form.toString().equals("Σ { x[i] ? i | i ∈ {1..a} with i%2 = 0 } ≥ 5"));
  }

  @Test(expected = language.parser.ParserException.class)
  public void testComparisonWithUndeclaredVariable() throws ParserException {
    VariableList vars = new VariableList();
    Formula form = InputReader.readFormulaFromString("x[i] <= 6", vars);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testEqualityWithNestedVariables() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x[i] :: Int ∈ {1..10} for i ∈ {1..10}", vars);
    Formula form = InputReader.readFormulaFromString("x[x[4]] = 3", vars);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testConditionalExpressionWithoutBrackets() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x :: Bool", vars);
    InputReader.declare("y :: Bool", vars);
    Formula form = InputReader.readFormulaFromString("3 = x ∧ y ? 4", vars);
  }
}

