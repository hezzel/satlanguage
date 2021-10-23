import org.junit.Test;
import static org.junit.Assert.*;

import logic.formula.*;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParseQuantifiedFormulaTest {
  @Test
  public void testReadSimpleParametrisedBooleanVariable() {
    VariableList vars = new VariableList();
    try {
      InputReader.declare("x[i] :: Bool for i ∈ {1..10}", vars);
      Formula form = InputReader.readFormulaFromString("x[b]", vars);
      assertTrue(form instanceof QuantifiedAtom);
      assertFalse(form.queryClosed());
      assertTrue(form.toString().equals("x[b]"));
      assertTrue(form.queryAtom() == null);
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadClosedParametrisedBooleanVariable() {
    VariableList vars = new VariableList();
    try {
      InputReader.declare("x[i] :: Bool for i ∈ {1..10}", vars);
      Formula form = InputReader.readFormulaFromString("x[2]", vars);
      assertTrue(form instanceof QuantifiedAtom);
      assertTrue(form.queryClosed());
      assertTrue(form.queryAtom() != null);
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadParametrisedBooleanVariableWithExpressions() {
    VariableList vars = new VariableList();
    try {
      InputReader.declare("x[i,j] :: Bool for i ∈ {1..4}, j ∈ {1..4}", vars);
      Formula form = InputReader.readFormulaFromString("x[i - 1, a]", vars);
      assertTrue(form instanceof QuantifiedAtom);
      assertFalse(form.queryClosed());
      assertTrue(form.toString().equals("x[i-1,a]"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testBasicForall() {
    VariableList vars = new VariableList();
    try {
      InputReader.declare("x[i,j] :: Bool for i ∈ {1..4}, j ∈ {1..4}", vars);
      Formula form = InputReader.readFormulaFromString("∀ i ∈ {2..5}.x[i- 1, a]", vars);
      assertTrue(form instanceof Forall);
      assertFalse(form.queryClosed());
      assertTrue(form.toString().equals("∀ i ∈ {2..5}. x[i-1,a]"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testBasicExists() {
    VariableList vars = new VariableList();
    try {
      InputReader.declare("x[i] :: Bool for i ∈ {1..10}", vars);
      Formula form = InputReader.readFormulaFromString("∃ i ∈ {1..6}.x[i]", vars);
      assertTrue(form instanceof Exists);
      assertTrue(form.queryClosed());
      assertTrue(form.toString().equals("∃ i ∈ {1..6}. x[i]"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testNestedQuantifiers() {
    VariableList vars = new VariableList();
    try {
      InputReader.declare("x[i,j] :: Bool for i ∈ {1..4}, j ∈ {1..4}",
                                            vars);
      Formula form = InputReader.readFormulaFromString(
        "∀ j ∈ {1..4}.∃ i ∈ {j-1..4}.x[j,i] ∧ x[i,j]", vars);
      assertTrue(form instanceof Forall);
      assertTrue(form.queryClosed());
      assertTrue(form.toString().equals("∀ j ∈ {1..4}. ∃ i ∈ {j-1..4}. x[j,i] ∧ x[i,j]"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testQuantifierNegation() {
    VariableList vars = new VariableList();
    try {
      InputReader.declare("x[i] :: Bool for i ∈ {1..10}", vars);
      Formula form = InputReader.readFormulaFromString("¬∃ i ∈ {1..6}.x[i]", vars);
      assertTrue(form instanceof Forall);
      assertTrue(form.toString().equals("∀ i ∈ {1..6}. ¬x[i]"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testDoubleQuantifierNegation() {
    VariableList vars = new VariableList();
    try {
      InputReader.declare("x[i] :: Bool for i ∈ {1..10}", vars);
      Formula form = InputReader.readFormulaFromString("--∃ i ∈ {1..6}.x[i]", vars);
      assertTrue(form instanceof Exists);
      assertTrue(form.toString().equals("∃ i ∈ {1..6}. x[i]"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testOccurrenceOnRightOfImplication() {
    VariableList vars = new VariableList();
    try {
      InputReader.declare("x[i] :: Bool for i ∈ {1..10}", vars);
      InputReader.declare("y    :: Bool", vars);
      Formula form = InputReader.readFormulaFromString("y → ∃ i ∈ {1..6}.x[i]", vars);
      assertTrue(form instanceof Implication);
      assertTrue(form.toString().equals("y → (∃ i ∈ {1..6}. x[i])"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testOccurrenceOnLeftOfImplication() {
    VariableList vars = new VariableList();
    try {
      InputReader.declare("x[i] :: Bool for i ∈ {1..10}", vars);
      InputReader.declare("y    :: Bool", vars);
      Formula form = InputReader.readFormulaFromString("∃ i ∈ {1..6}.x[i] → y", vars);
      assertTrue(form instanceof Exists);
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testOccurrenceOnRightOfAnd() {
    VariableList vars = new VariableList();
    try {
      InputReader.declare("x[i] :: Bool for i ∈ {1..10}", vars);
      InputReader.declare("y    :: Bool", vars);
      Formula form = InputReader.readFormulaFromString("y ∧ ∀ i ∈ {1..6}.x[i]", vars);
      assertTrue(form instanceof And);
      assertTrue(form.toString().equals("y ∧ (∀ i ∈ {1..6}. x[i])"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testOccurrenceOnMiddleOfOr() {
    VariableList vars = new VariableList();
    try {
      InputReader.declare("x[i] :: Bool for i ∈ {1..10}", vars);
      InputReader.declare("y    :: Bool", vars);
      Formula form = InputReader.readFormulaFromString("¬y ∨ ∀ i ∈ {1..6}.x[i] ∨ y", vars);
      assertTrue(form instanceof Or);
      assertTrue(form.toString().equals("¬y ∨ (∀ i ∈ {1..6}. x[i] ∨ y)"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test(expected = language.parser.ParserException.class)
  public void testParamBoolWithParamRangeVar() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x[i] :: Bool for i ∈ {1..10}", vars);
    InputReader.declare("y[j] :: Int ∈ {1..10} for j ∈ {1..5}", vars);
    Formula form = InputReader.readFormulaFromString("∀ j ∈ {1..5}.x[y[j]]", vars);
  }
}

