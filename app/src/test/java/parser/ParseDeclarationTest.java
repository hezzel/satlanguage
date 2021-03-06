import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.Assignment;
import logic.parameter.ParameterList;
import logic.parameter.PConstraint;
import logic.parameter.ParamBoolVar;
import logic.number.range.RangeVariable;
import logic.number.range.ParamRangeVar;
import logic.number.binary.BinaryVariable;
import logic.number.binary.ParamBinaryVar;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;
import language.parser.DefinitionData;

public class ParseDeclarationTest {
  @Test
  public void testReadBasicBooleanDeclaration() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("myvar :: Bool", lst);
    assertTrue(lst.isDeclared("myvar"));
    assertTrue(lst.queryBooleanVariable("myvar").toString().equals("myvar"));
  }

  @Test
  public void testReadBasicRangeDeclaration() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("myvar :: Number ∈ {12..15}", lst);
    assertTrue(lst.isDeclared("myvar"));
    RangeVariable rv = lst.queryRangeVariable("myvar");
    assertTrue(rv.toString().equals("myvar"));
    assertTrue(rv.queryMinimum() == 12);
    assertTrue(rv.queryMaximum() == 15);
  }

  @Test
  public void testReadBasicRangeDeclarationWithRestriction() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("myvar :: Number ∈ {12..15} with myvar != 14", lst);
    assertTrue(lst.isDeclared("myvar"));
    RangeVariable rv = lst.queryRangeVariable("myvar");
    assertTrue(rv.toString().equals("myvar"));
    assertTrue(rv.queryGeqAtom(14).equals(rv.queryGeqAtom(15)));
  }

  @Test
  public void testReadBasicIntDeclaration() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("myvar :: Int12", lst);
    assertTrue(lst.isDeclared("myvar"));
    BinaryVariable bv = lst.queryBinaryVariable("myvar");
    assertTrue(bv.toString().equals("myvar"));
    assertTrue(bv.length() == 12);
    assertTrue(bv.queryNegativeBit().toString().equals("myvar⟨-⟩"));
  }

  @Test
  public void testReadBasicNatDeclaration() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("myvar :: Nat7", lst);
    assertTrue(lst.isDeclared("myvar"));
    BinaryVariable bv = lst.queryBinaryVariable("myvar");
    assertTrue(bv.toString().equals("myvar"));
    assertTrue(bv.length() == 7);
    assertTrue(bv.queryMinimum() == 0);
    assertTrue(bv.queryNegativeBit().toString().equals("¬TRUE"));
  }

  @Test
  public void testReadBasicBinaryDeclarationWithMinmax() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("myvar :: Int? ∈ {3..18}", lst);
    assertTrue(lst.isDeclared("myvar"));
    BinaryVariable bv = lst.queryBinaryVariable("myvar");
    assertTrue(bv.toString().equals("myvar"));
    assertTrue(bv.length() == 5);
    assertTrue(bv.queryMinimum() == 3);
    assertTrue(bv.queryMaximum() == 18);
    assertTrue(bv.queryNegativeBit().toString().equals("¬TRUE"));
  }

  @Test
  public void testReadEasyParamBoolVarDeclaration() throws ParserException {
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

  @Test
  public void testReadComplexParamBoolVarDeclaration() throws ParserException {
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

  @Test
  public void testReadBasicParamRangeVar() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("var[i,j] :: Number ∈ {6..8} for i ∈ {1..9}, j ∈ {1..9}", lst);
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
    InputReader.declare("var[i,j] :: Number ∈ {0..j} with var != i for i ∈ {1..9}, j ∈ {i..10}", lst);
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

  @Test
  public void testReadSimpleParamBinaryVar() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("var[i,j] :: Int8 for i ∈ {1..9}, j ∈ {i..10}", lst);
    assertTrue(lst.isDeclared("var"));
    ParamBinaryVar v = lst.queryParametrisedBinaryVariable("var");
    ParameterList params = v.queryParameters();
    assertTrue(params.get(0).toString().equals("i ∈ {1..9}"));
    assertTrue(params.get(1).toString().equals("j ∈ {i..10}"));
    Assignment ass = new Assignment("i", 2, "j", 5);
    BinaryVariable v25 = v.queryVar(ass);
    assertTrue(v25.length() == 8);
    assertTrue(v25.queryMinimum() == -256);
    assertTrue(v25.queryMaximum() == 255);
    assertTrue(v25.queryBit(3).toString().equals("var[2,5]⟨3⟩"));
  }

  @Test
  public void testReadSimpleParamBinaryVarNat() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("var[i,j] :: Nat8 for i ∈ {1..5}, j ∈ {1..5}", lst);
    assertTrue(lst.isDeclared("var"));
    ParamBinaryVar v = lst.queryParametrisedBinaryVariable("var");
    Assignment ass = new Assignment("i", 3, "j", 3);
    BinaryVariable v33 = v.queryVar(ass);
    assertTrue(v33.length() == 8);
    assertTrue(v33.queryMinimum() == 0);
    assertTrue(v33.queryMaximum() == 255);
  }

  @Test
  public void testReadParamBinaryVarWithMinmax() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("x[i] :: Int? ∈ {1..i} for i ∈ {1..10}", lst);
    assertTrue(lst.isDeclared("x"));
    ParamBinaryVar x = lst.queryParametrisedBinaryVariable("x");
    BinaryVariable x1 = x.queryVar(new Assignment("i", 1));
    BinaryVariable x9 = x.queryVar(new Assignment("i", 9));
    assertTrue(x1.queryMinimum() == 1);
    assertTrue(x1.queryMaximum() == 1);
    assertTrue(x1.length() == 1);
    assertTrue(x9.queryMinimum() == 1);
    assertTrue(x9.queryMaximum() == 9);
    assertTrue(x9.length() == 4);
    assertTrue(x1.queryBit(2).toString().equals("¬TRUE"));
    assertTrue(x9.queryBit(2).toString().equals("x[9]⟨2⟩"));
  }

  @Test(expected = language.parser.ParserException.class)
  public void testDeclareVariableTwice() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("myvar :: Bool", lst);
    InputReader.declare("myvar[i] :: Int3 for i ∈ {1..5}", lst);
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
    InputReader.declare("myvar :: Number ∈ {1..10} with myvar > i", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testInappropriateParamRangeRestriction() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("field[x,y] :: Number ∈ {0..10} with field > z for x ∈ {1..4}, y ∈ {1..4}",
                        lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testInappropriateParamRangeRange() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("field[x,y] :: Number ∈ {0..z} for x ∈ {1..4}, y ∈ {1..4}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testInappropriateParameterorderBool() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("bing[a,b] :: Bool for b ∈ {1..10}, a ∈ {1..5}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testInappropriateParameterorderRange() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("bing[a,b] :: Number ∈ {0..10} for b ∈ {1..10}, a ∈ {1..5}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testMissingSomeParameters() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("bing[a,b] :: Bool for a ∈ {1..5}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testMissingAllParameters() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("bing[a,b] :: Number ∈ {0..10}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testExpressionsInBoolDeclaration() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("hello[i+1] :: Bool for i ∈ {0..3}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testExpressionsInRangeDeclaration() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("hello[i+1] :: Number ∈ {0..10} for i ∈ {0..3}", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testDeclareVariableDefinedAsProperty() throws ParserException {
    VariableList lst = new VariableList();
    DefinitionData defs = new DefinitionData();
    InputReader.readPropertyFromString("property p { (1,1) ; (2,2) ; }", defs);
    InputReader.declare("p[i] :: Bool for i ∈ {1..10}", lst, defs);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testDeclareVariableArgumentDefinedAsEnum() throws ParserException {
    VariableList lst = new VariableList();
    DefinitionData defs = new DefinitionData();
    InputReader.readEnumFromString("enum e { x ; y }", defs);
    InputReader.declare("p[e] :: Bool for e ∈ {1..10}", lst, defs);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testRedeclarateBinary() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("a :: Int7", lst);
    InputReader.declare("a :: Nat8", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testBinaryIntegerWithRangeRestriction() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("a :: Int? ∈ {1..3} with a != 2", lst);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testBinaryIntegerWithRangeRelyingOnVariables() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("a :: Int? ∈ {1..i}", lst);
  }
}

