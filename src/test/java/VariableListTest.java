import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.parameter.Parameter;
import logic.parameter.ParameterList;
import logic.parameter.ParamBoolVar;
import language.parser.InputReader;
import language.parser.ParserException;
import logic.VariableList;

public class VariableListTest {
  @Test
  public void testEmptyList() {
    VariableList lst = new VariableList();
    assertTrue(lst.isDeclared("FALSE"));
    assertTrue(lst.isDeclared("TRUE"));
    assertFalse(lst.isDeclared("x"));
    assertTrue(lst.toString().equals(""));
  }

  @Test(expected = java.lang.Error.class)
  public void testIllegalRegistrationWithIndex() {
    VariableList lst = new VariableList();
    lst.registerBooleanVariable("x[1]");
  }

  @Test(expected = java.lang.Error.class)
  public void testIllegalRegistrationWithUnicodeBrackets() {
    VariableList lst = new VariableList();
    lst.registerBooleanVariable("⟦x⟧");
  }

  @Test
  public void testContainsBasicBoolAfterRegistering() {
    VariableList lst = new VariableList();
    Variable z = lst.registerBooleanVariable("z");
    assertTrue(z.equals(new Variable("z")));
    assertTrue(lst.isDeclared("z"));
  }

  @Test
  public void testContainsParamBoolAfterRegistering() throws ParserException {
    VariableList lst = new VariableList();
    Parameter i = InputReader.readParameterFromString("i ∈ {0..5}");
    Parameter j = InputReader.readParameterFromString("j ∈ {i+1..7}");
    Parameter k = InputReader.readParameterFromString("k ∈ {0..5} with j != k");
    ParameterList params = new ParameterList(i, j, k);
    ParamBoolVar xyz = lst.registerParametrisedBooleanVariable("xyz", params);
    assertTrue(lst.isDeclared("xyz"));
    assertTrue(lst.queryParametrisedBooleanVariable("xyz") == xyz);
    assertTrue(xyz.toString().equals("xyz[i,j,k]"));
    assertTrue(lst.toString().equals("declare xyz[i,j,k] :: Bool for i ∈ {0..5}, j ∈ {i+1..7}, " +
      "k ∈ {0..5} with j ≠ k\n"));
  }

  @Test
  public void testTrueAndFalse() {
    VariableList lst = new VariableList();
    assertTrue(lst.queryTrueVariable().equals(new Variable("TRUE")));
    assertTrue(lst.queryFalseVariable().equals(new Variable("FALSE")));
  }
}

