import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.formula.*;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParseBasicFormulaTest {
  @Test
  public void testReadPositiveAtom() throws ParserException {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    Formula form = InputReader.readFormulaFromString("x", vars);
    assertTrue(form.queryAtom().toString().equals("x"));
  }

  @Test
  public void testReadNegativeAtom() throws ParserException {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    Formula form = InputReader.readFormulaFromString("¬x", vars);
    assertTrue(form.queryAtom().queryNegative());
    assertTrue(form.queryAtom().queryVariable().equals(new Variable("x")));
  }

  @Test
  public void testDoubleNegation() throws ParserException {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    Formula form = InputReader.readFormulaFromString("¬-x", vars);
    assertFalse(form.queryAtom().queryNegative());
    assertTrue(form.queryAtom().queryVariable().toString().equals("x"));
  }

  @Test
  public void testReadBracketFormula() throws ParserException {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    Formula form = InputReader.readFormulaFromString("(x)", vars);
    assertFalse(form.queryAtom().queryNegative());
    assertTrue(form.queryAtom().queryVariable().toString().equals("x"));
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadUnregisteredVariable() throws ParserException {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    Formula form = InputReader.readFormulaFromString("y", vars);
  }

  @Test
  public void testReadSimpleConjunction() throws ParserException {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    vars.registerBooleanVariable("z");
    Formula form = InputReader.readFormulaFromString("x∧- y ∧  z", vars);
    assertTrue(form instanceof And);
    assertTrue(form.toString().equals("x ∧ ¬y ∧ z"));
  }

  @Test
  public void testReadSimpleImplication() throws ParserException {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    Formula form = InputReader.readFormulaFromString("x  → ¬y", vars);
    assertTrue(form instanceof Implication);
    assertTrue(form.toString().equals("x → ¬y"));
  }

  @Test
  public void testReadSimpleIff() {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    try {
      Formula form = InputReader.readFormulaFromString("x  <-> ¬y", vars);
      assertTrue(form instanceof Iff);
      assertTrue(form.toString().equals("x ↔ ¬y"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadSimpleIfThenElse() throws ParserException {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    vars.registerBooleanVariable("z");
    Formula form = InputReader.readFormulaFromString("ite(x,y,-z /\\ x)", vars);
    assertTrue(form instanceof IfThenElse);
    assertTrue(form.toString().equals("ite(x, y, ¬z ∧ x)"));
  }

  @Test
  public void testReadDisjunctionWithBrackets() throws ParserException {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    vars.registerBooleanVariable("z");
    Formula form = InputReader.readFormulaFromString("x ∨ (-y ∨  z)", vars);
    assertTrue(form instanceof Or);
    assertTrue(form.toString().equals("x ∨ ¬y ∨ z"));
  }

  @Test
  public void testReadNegationAboveBrackets() throws ParserException {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    vars.registerBooleanVariable("z");
    Formula form = InputReader.readFormulaFromString("x ∨ -(-y ∧  z)", vars);
    assertTrue(form instanceof Or);
    assertTrue(form.toString().equals("x ∨ y ∨ ¬z"));
  }

  @Test
  public void testComplicatedAndOrFormula() throws ParserException {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    vars.registerBooleanVariable("z");
    Formula form = InputReader.readFormulaFromString("(x ∨ ¬y) ∧ -(-y ∧  (z ∨ x))", vars);
    assertTrue(form.toString().equals("(x ∨ ¬y) ∧ (y ∨ (¬z ∧ ¬x))"));
  }

  @Test
  public void testJunctionImplicationMixture() throws ParserException {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    vars.registerBooleanVariable("z");
    Formula form = InputReader.readFormulaFromString("x ∨ ¬z → y ∧ z", vars);
    assertTrue(form instanceof Implication);
    Formula tr = ((Implication)form).translate();
    assertTrue(tr.toString().equals("(¬x ∧ z) ∨ (y ∧ z)"));
  }

  @Test(expected = language.parser.ParserException.class)
  public void testImplicationContainingImplication() throws ParserException {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    vars.registerBooleanVariable("z");
    Formula form = InputReader.readFormulaFromString("x → y → z", vars);
  }

  @Test
  public void testIffMixture() throws ParserException {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    vars.registerBooleanVariable("z");
    Formula form = InputReader.readFormulaFromString("x ∨ ¬z ↔ (y -> z)", vars);
    assertTrue(form instanceof Iff);
  }
}

