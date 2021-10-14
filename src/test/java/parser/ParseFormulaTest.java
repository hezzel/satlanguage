package logic.parameter;

import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.formula.*;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParseFormulaTest {
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
}

