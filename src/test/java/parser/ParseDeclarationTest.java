package logic.parameter;

import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.ParameterList;
import logic.parameter.ParamBoolVar;
import logic.range.RangeVariable;
import logic.range.ParamRangeVar;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParseDeclarationTest {
  @Test
  public void testReadBasicBooleanDeclaration() {
    try {
      VariableList lst = new VariableList();
      InputReader.declare("myvar :: Bool", lst);
      assertTrue(lst.isDeclared("myvar"));
      assertTrue(lst.queryBooleanVariable("myvar").toString().equals("myvar"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadBasicRangeDeclaration() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("myvar :: Int ∈ {12..15}", lst);
    assertTrue(lst.isDeclared("myvar"));
    RangeVariable rv = lst.queryRangeVariable("myvar");
    assertTrue(rv.toString().equals("myvar"));
    assertTrue(rv.queryMinimum() == 12);
    assertTrue(rv.queryMaximum() == 15);
  }

  @Test
  public void testReadBasicRangeDeclarationWithRestriction() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("myvar :: Int ∈ {12..15} with myvar != 14", lst);
    assertTrue(lst.isDeclared("myvar"));
    RangeVariable rv = lst.queryRangeVariable("myvar");
    assertTrue(rv.toString().equals("myvar"));
    assertTrue(rv.queryGeqAtom(14).equals(rv.queryGeqAtom(15)));
  }

  @Test
  public void testReadEasyParamBoolVarDeclaration() {
    try {
      VariableList lst = new VariableList();
      InputReader.declare("pv[i] :: Bool for i ∈ {1..10}", lst);
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
      InputReader.declare("pv[i, j, k] :: Bool for " +
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

  @Test
  public void testReadBasicParamRangeVar() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("var[i,j] :: Int ∈ {6..8} for i ∈ {1..9}, j ∈ {1..9}", lst);
    assertTrue(lst.isDeclared("var"));
    ParamRangeVar v = lst.queryParametrisedRangeVariable("var");
    ParameterList params = v.queryParameters();
    assertTrue(params.size() == 2);
    assertTrue(params.get(0).toString().equals("i ∈ {1..9}"));
    assertTrue(params.get(1).toString().equals("j ∈ {1..9}"));
    Assignment ass = new Assignment("i", 2, "j", 5);
    RangeVariable x = v.queryVar(ass);
    assertTrue(x.queryMinimum() == 6);
    assertTrue(x.queryMaximum() == 8);
    assertTrue(x.queryGeqAtom(7).toString().equals("var[2,5]≥7"));
    assertTrue(x.queryGeqAtom(9).toString().equals("¬TRUE"));
    assertTrue(x.queryGeqAtom(6).toString().equals("TRUE"));
  }

  @Test
  public void testReadComplexParamRangeVar() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("var[i,j] :: Int ∈ {0..j} with var != i for i ∈ {1..9}, j ∈ {i..10}", lst);
    assertTrue(lst.isDeclared("var"));
    ParamRangeVar v = lst.queryParametrisedRangeVariable("var");
    ParameterList params = v.queryParameters();
    assertTrue(params.size() == 2);
    assertTrue(params.get(0).toString().equals("i ∈ {1..9}"));
    assertTrue(params.get(1).toString().equals("j ∈ {i..10}"));
    Assignment ass = new Assignment("i", 2, "j", 5);
    RangeVariable x = v.queryVar(ass);
    assertTrue(x.queryMinimum() == 0);
    assertTrue(x.queryMaximum() == 5);
    assertTrue(x.queryGeqAtom(1).toString().equals("var[2,5]≥1"));
    assertTrue(x.queryGeqAtom(2).toString().equals("var[2,5]≥3"));
  }

  @Test(expected = language.parser.ParserException.class)
  public void testDeclareVariableTwice() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("myvar :: Bool", lst);
    InputReader.declare("myvar[i] :: Bool for i ∈ {1..5}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testInappropriateDependency() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("pv[i, j, k] :: Bool for " +
      "j ∈ {0..i+1} with j != 2, i ∈ {1..10}, k ∈ {i-3..7}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testInappropriateRangeRestriction() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("myvar :: Int ∈ {1..10} with myvar > i", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testInappropriateParamRangeRestriction() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("field[x,y] :: Int ∈ {0..10} with field > z for x ∈ {1..4}, y ∈ {1..4}",
                        lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testInappropriateParamRangeRange() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("field[x,y] :: Int ∈ {0..z} for x ∈ {1..4}, y ∈ {1..4}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testInappropriateParameterorderBool() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("bing[a,b] :: Bool for b ∈ {1..10}, a ∈ {1..5}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testInappropriateParameterorderRange() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("bing[a,b] :: Int ∈ {0..10} for b ∈ {1..10}, a ∈ {1..5}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testMissingSomeParameters() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("bing[a,b] :: Bool for a ∈ {1..5}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testMissingAllParameters() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("bing[a,b] :: Int ∈ {0..10}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testExpressionsInBoolDeclaration() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("hello[i+1] :: Bool for i ∈ {0..3}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testExpressionsInRangeDeclaration() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("hello[i+1] :: Int ∈ {0..10} for i ∈ {0..3}", lst);
  }
}

