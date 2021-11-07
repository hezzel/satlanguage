import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.PExpression;
import logic.parameter.ParameterExpression;
import logic.VariableList;
import language.execution.VariableExpression;
import language.execution.ParamVarExpression;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParseExtendedPExpressionTest {
  @Test
  public void testReadRangeVariable() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x :: Number ∈ {1..10}", vars);
    PExpression e = InputReader.readExtendedPExpressionFromString("x", vars);
    assertTrue(e instanceof VariableExpression);
    assertTrue(e.toString().equals("x"));
    assertTrue(e.queryParameters().size() == 0);
  }

  @Test
  public void testReadBinaryVariable() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x :: Int? ∈ {1..10}", vars);
    PExpression e = InputReader.readExtendedPExpressionFromString("x", vars);
    assertTrue(e instanceof VariableExpression);
    assertTrue(e.toString().equals("x"));
    assertTrue(e.queryParameters().size() == 0);
  }

  @Test
  public void testReadUndeclaredIntegerVariable() throws ParserException {
    VariableList vars = new VariableList();
    PExpression e = InputReader.readExtendedPExpressionFromString("x", vars);
    assertTrue(e instanceof ParameterExpression);
    assertTrue(e.toString().equals("x"));
    assertTrue(e.queryParameters().size() == 1);
  }

  @Test
  public void testReadParamRangeVar() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x[i] :: Number ∈ {1..i} for i ∈ {1..10}", vars);
    PExpression e = InputReader.readExtendedPExpressionFromString("x[ j+ 1]", vars);
    assertTrue(e instanceof ParamVarExpression);
    assertTrue(e.toString().equals("x[j+1]"));
  }

  @Test
  public void testReadParamBinaryVar() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x[i] :: Nat8 for i ∈ {1..10}", vars);
    PExpression e = InputReader.readExtendedPExpressionFromString("x[ j+ 1]", vars);
    assertTrue(e instanceof ParamVarExpression);
    assertTrue(e.toString().equals("x[j+1]"));
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadUndeclaredParamVariable() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.readExtendedPExpressionFromString("x[0]", vars);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadParamRangeVariableWithWrongParamCount() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x[i,j] :: Number ∈ {1..i} for i ∈ {1..10}, j ∈ {1..10}", vars);
    PExpression e = InputReader.readExtendedPExpressionFromString("x[ j+ 1]", vars);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadParamBinaryVariableWithWrongParamCount() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x[i,j] :: Int3 for i ∈ {1..10}", vars);
    PExpression e = InputReader.readExtendedPExpressionFromString("x[0,1]", vars);
  }

  @Test
  public void testReadExtendedPExpression() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("y :: Number ∈ {1..10}", vars);
    InputReader.declare("x[i,j] :: Number ∈ {1..10} for i ∈ {1..5}, j ∈ {i..5}", vars);
    InputReader.declare("z[a] :: Int8 for a ∈ {1..4}", vars);
    InputReader.declare("n :: Int? ∈ {1..9}", vars);
    PExpression e = InputReader.readExtendedPExpressionFromString("i + x[j+y+z[n], x[y,1]]", vars);
    assertTrue(e.toString().equals("i+x[j+y+z[n],x[y,1]]"));
  }
}

