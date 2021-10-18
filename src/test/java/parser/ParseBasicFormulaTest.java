import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.formula.*;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParseBasicFormulaTest {
  @Test
  public void testReadPositiveAtom() {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    try {
      Formula form = InputReader.readFormulaFromString("x", vars);
      assertTrue(form.queryAtom().toString().equals("x"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadNegativeAtom() {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    try {
      Formula form = InputReader.readFormulaFromString("¬x", vars);
      assertTrue(form.queryAtom().queryNegative());
      assertTrue(form.queryAtom().queryVariable().equals(new Variable("x")));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testDoubleNegation() {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    try {
      Formula form = InputReader.readFormulaFromString("¬-x", vars);
      assertFalse(form.queryAtom().queryNegative());
      assertTrue(form.queryAtom().queryVariable().toString().equals("x"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadBracketFormula() {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    try {
      Formula form = InputReader.readFormulaFromString("(x)", vars);
      assertFalse(form.queryAtom().queryNegative());
      assertTrue(form.queryAtom().queryVariable().toString().equals("x"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadUnregisteredVariable() throws ParserException {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    Formula form = InputReader.readFormulaFromString("y", vars);
  }

  @Test
  public void testReadSimpleConjunction() {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    vars.registerBooleanVariable("z");
    try {
      Formula form = InputReader.readFormulaFromString("x∧- y ∧  z", vars);
      assertTrue(form instanceof And);
      assertTrue(form.toString().equals("x ∧ ¬y ∧ z"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadSimpleImplication() {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    try {
      Formula form = InputReader.readFormulaFromString("x  → ¬y", vars);
      assertTrue(form instanceof Implication);
      assertTrue(form.toString().equals("x → ¬y"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
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
  public void testReadSimpleIfThenElse() {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    vars.registerBooleanVariable("z");
    try {
      Formula form = InputReader.readFormulaFromString("ite(x,y,-z /\\ x)", vars);
      assertTrue(form instanceof IfThenElse);
      assertTrue(form.toString().equals("ite(x, y, ¬z ∧ x)"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadDisjunctionWithBrackets() {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    vars.registerBooleanVariable("z");
    try {
      Formula form = InputReader.readFormulaFromString("x ∨ (-y ∨  z)", vars);
      assertTrue(form instanceof Or);
      assertTrue(form.toString().equals("x ∨ ¬y ∨ z"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadNegationAboveBrackets() {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    vars.registerBooleanVariable("z");
    try {
      Formula form = InputReader.readFormulaFromString("x ∨ -(-y ∧  z)", vars);
      assertTrue(form instanceof Or);
      assertTrue(form.toString().equals("x ∨ y ∨ ¬z"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testComplicatedAndOrFormula() {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    vars.registerBooleanVariable("z");
    try {
      Formula form = InputReader.readFormulaFromString("(x ∨ ¬y) ∧ -(-y ∧  (z ∨ x))", vars);
      assertTrue(form.toString().equals("(x ∨ ¬y) ∧ (y ∨ (¬z ∧ ¬x))"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testJunctionImplicationMixture() {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    vars.registerBooleanVariable("z");
    try {
      Formula form = InputReader.readFormulaFromString("x ∨ ¬z → y ∧ z", vars);
      assertTrue(form instanceof Implication);
      Formula tr = ((Implication)form).translate();
      assertTrue(tr.toString().equals("(¬x ∧ z) ∨ (y ∧ z)"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
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
  public void testIffMixture() {
    VariableList vars = new VariableList();
    vars.registerBooleanVariable("x");
    vars.registerBooleanVariable("y");
    vars.registerBooleanVariable("z");
    try {
      Formula form = InputReader.readFormulaFromString("x ∨ ¬z ↔ (y -> z)", vars);
      assertTrue(form instanceof Iff);
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }
}

