import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.parameter.Assignment;
import logic.parameter.Parameter;
import logic.parameter.ParameterList;
import logic.range.VariableInteger;
import logic.range.ParamRangeVar;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParamRangeVarTest {
  private Variable falsehood() { return new Variable("FALSE"); }
  private Variable truth() { return new Variable("TRUE"); }

  @Test
  public void testSimpleRangeVar() throws ParserException {
    ParameterList lst = new ParameterList(InputReader.readParameterFromString("i ∈ {1..10}"));
    ParamRangeVar v = new ParamRangeVar("xx", lst, 3, 6, falsehood(), truth());
    VariableInteger xx1 = v.queryVar(new Assignment("i", 1));
    assertTrue(xx1.queryMinimum() == 3);
    assertTrue(xx1.queryMaximum() == 6);
    assertTrue(xx1.toString().equals("xx[1]"));
    assertTrue(xx1.queryGeqVariable(5).toString().equals("xx[1]≥5"));
  }

  @Test(expected = java.lang.Error.class)
  public void testSimpleRangeVarOutsideDomain() throws ParserException {
    ParameterList lst = new ParameterList(InputReader.readParameterFromString("i ∈ {1..10}"));
    ParamRangeVar v = new ParamRangeVar("xx", lst, 3, 6, falsehood(), truth());
    VariableInteger xx1 = v.queryVar(new Assignment("i", 0));
  }

  @Test
  public void testComplexRangeVar() throws ParserException {
    ParameterList lst = new ParameterList(
      InputReader.readParameterFromString("i ∈ {1..10}"),
      InputReader.readParameterFromString("j ∈ {i..10}"));
    Parameter count = InputReader.readParameterFromString("xx ∈ {i..j+1} with xx != 5");
    ParamRangeVar v = new ParamRangeVar(count, lst, falsehood(), truth());
    VariableInteger xx35 = v.queryVar(new Assignment("i", 3, "j", 5));
    assertTrue(xx35.toString().equals("xx[3,5]"));
    assertTrue(xx35.queryMinimum() == 3);
    assertTrue(xx35.queryMaximum() == 6);
    assertTrue(xx35.queryGeqVariable(3).toString().equals("TRUE"));
    assertTrue(xx35.queryGeqVariable(4).toString().equals("xx[3,5]≥4"));
    assertTrue(xx35.queryGeqVariable(5).toString().equals("xx[3,5]≥6"));
    assertTrue(xx35.queryGeqVariable(6).toString().equals("xx[3,5]≥6"));
    assertTrue(xx35.queryGeqVariable(7).toString().equals("FALSE"));
  }
}

