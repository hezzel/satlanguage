import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.PConstraint;
import logic.VariableList;
import language.execution.VariableConstraint;
import language.execution.ParamBoolVarConstraint;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParseExtendedPConstraintTest {
  @Test
  public void testReadBooleanVariable() {
    VariableList vars = new VariableList();
    try {
      InputReader.readDeclarationFromString("declare x :: Bool", vars);
      PConstraint c = InputReader.readExtendedPConstraintFromString("x", vars);
      assertTrue(c instanceof VariableConstraint);
      assertTrue(c.toString().equals("x"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadUndeclaredBooleanVariable() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.readExtendedPConstraintFromString("x", vars);
  }

  @Test
  public void testReadParamBoolVar() {
    VariableList vars = new VariableList();
    try {
      InputReader.readDeclarationFromString("declare x[i] :: Bool for i ∈ {1..10}", vars);
      PConstraint c = InputReader.readExtendedPConstraintFromString("x[ j+ 1]", vars);
      assertTrue(c instanceof ParamBoolVarConstraint);
      assertTrue(c.toString().equals("x[j+1]"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadUndeclaredParamBooleanVariable() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.readExtendedPConstraintFromString("x[0]", vars);
  }

  @Test
  public void testReadExtendedPConstraint() {
    VariableList vars = new VariableList();
    try {
      InputReader.readDeclarationFromString("declare y :: Bool", vars);
      InputReader.readDeclarationFromString("declare x[i] :: Bool for i ∈ {1..10}", vars);
      PConstraint c = InputReader.readExtendedPConstraintFromString("i < j ∧ (y ∨ x[i+j])", vars);
      assertTrue(c.toString().equals("i < j ∧ (y ∨ x[i+j])"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }
}

