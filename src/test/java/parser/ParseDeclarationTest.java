package logic.parameter;

import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.ParameterList;
import logic.parameter.ParamBoolVar;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParseDeclarationTest {
  @Test
  public void testReadBasicBooleanDeclaration() {
    try {
      VariableList lst = new VariableList();
      InputReader.readDeclarationFromString("declare myvar :: Bool", lst);
      assertTrue(lst.isDeclared("myvar"));
      assertTrue(lst.queryBooleanVariable("myvar").toString().equals("myvar"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadEasyParamBoolVarDeclaration() {
    try {
      VariableList lst = new VariableList();
      InputReader.readDeclarationFromString("declare pv[i] :: Bool for i ∈ {1..10}", lst);
      assertTrue(lst.isDeclared("pv"));
      ParamBoolVar v = lst.queryParametrisedBooleanVariable("pv");
      ParameterList params = v.queryParameters();
      assertTrue(params.size() == 1);
      assertTrue(params.get(0).queryName().equals("i"));
      assertTrue(params.get(0).queryMinimum().evaluate(null) == 1);
      assertTrue(params.get(0).queryMaximum().evaluate(null) == 10);
      assertTrue(params.get(0).queryRestriction().isTop());
      assertTrue(v.toString().equals("pv[i]"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadComplexParamBoolVarDeclaration() {
    try {
      VariableList lst = new VariableList();
      InputReader.readDeclarationFromString("declare pv[i, j, k] :: Bool for " +
        "i ∈ {1..10}, j ∈ {0..i+1} with j != 2, k ∈ {i-2..7}", lst);
      assertTrue(lst.isDeclared("pv"));
      ParamBoolVar v = lst.queryParametrisedBooleanVariable("pv");
      ParameterList params = v.queryParameters();
      assertTrue(params.size() == 3);
      assertTrue(params.get(0).queryName().equals("i"));
      assertTrue(params.get(0).queryMinimum().evaluate(null) == 1);
      assertTrue(params.get(0).queryMaximum().evaluate(null) == 10);
      assertTrue(params.get(0).queryRestriction().isTop());
      assertTrue(params.get(1).queryName().equals("j"));
      assertTrue(params.get(1).queryMinimum().evaluate(null) == 0);
      assertTrue(params.get(1).queryMaximum().toString().equals("i+1"));
      assertTrue(params.get(1).queryRestriction().queryKind() == PConstraint.RELATION);
      assertTrue(params.get(2).queryName().equals("k"));
      assertTrue(params.get(2).queryMinimum().toString().equals("i-2"));
      assertTrue(params.get(2).queryMaximum().evaluate(null) == 7);
      assertTrue(v.toString().equals("pv[i,j,k]"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test(expected = language.parser.ParserException.class)
  public void testDeclareVariableTwice() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.readDeclarationFromString("declare myvar :: Bool", lst);
    InputReader.readDeclarationFromString("declare myvar[i] :: Bool for i ∈ {1..5}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testInappropriateDependency() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.readDeclarationFromString("declare pv[i, j, k] :: Bool for " +
      "j ∈ {0..i+1} with j != 2, i ∈ {1..10}, k ∈ {i-3..7}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testInappropriateParameterorder() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.readDeclarationFromString("declare bing[a,b] :: Bool for " +
      "b ∈ {1..10}, a ∈ {1..5}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testMissingSomeParameters() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.readDeclarationFromString("declare bing[a,b] :: Bool for " +
      "a ∈ {1..5}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testMissingAllParameters() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.readDeclarationFromString("declare bing[a,b] :: Bool", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testExpressionsInDeclaration() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.readDeclarationFromString("declare hello[i+1] :: Bool for i ∈ {0..3}", lst);
  }
}

