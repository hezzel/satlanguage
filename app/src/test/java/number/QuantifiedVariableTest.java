import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.parameter.PExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.number.range.RangeVariable;
import logic.number.range.ParamRangeVar;
import logic.number.binary.BinaryVariable;
import logic.number.binary.ParamBinaryVar;
import logic.number.VariableInteger;
import logic.number.QuantifiedInteger;
import logic.number.QuantifiedVariable;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;
import java.util.Set;

public class QuantifiedVariableTest {
  private PExpression expr(String txt) {
    try { return InputReader.readPExpressionFromString(txt); }
    catch (ParserException e) { return null; }
  }

  private QuantifiedVariable createVariable() {
    try {
      VariableList lst = new VariableList();
      InputReader.declare("qrvt[i,j] :: Number ∈ {0..j} with qrvt != i for i ∈ {1..5}, j ∈ {i+1..6}",
                          lst);
      ParamRangeVar qrvt = lst.queryParametrisedRangeVariable("qrvt");
      Substitution subst = new Substitution("i", expr("a+b-1"), "j", expr("b"), "b", expr("1"));
      return new QuantifiedVariable(qrvt, subst);
    } catch (ParserException e) { return null; }
  }

  @Test
  public void testToString() {
    assertTrue(createVariable().toString().equals("qrvt[a+b-1,b]"));
  }

  @Test
  public void testParameters() {
    Set<String> params = createVariable().queryParameters();
    assertTrue(params.size() == 2);
    assertTrue(params.contains("a"));
    assertTrue(params.contains("b"));
  }

  @Test
  public void testNotClosed() {
    assertFalse(createVariable().queryClosed());
  }

  @Test
  public void testClosed() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("x[i] :: Nat8 for i ∈ {1..5}", lst);
    ParamBinaryVar x = lst.queryParametrisedBinaryVariable("x");
    Substitution subst = new Substitution("i", expr("7"));
    QuantifiedVariable qrv = new QuantifiedVariable(x, subst);
    assertTrue(qrv.toString().equals("x[7]"));
    assertTrue(qrv.queryClosed());
  }

  @Test
  public void testSubstitute() {
    Substitution subst = new Substitution("a", expr("c * d"));
    QuantifiedInteger c = createVariable().substitute(subst);
    assertTrue(c.toString().equals("qrvt[c*d+b-1,b]"));
    assertTrue(c.queryParameters().size() == 3);
  }

  @Test
  public void testInstantiate() {
    Assignment ass = new Assignment("a", -1, "b", 3);
    VariableInteger c = createVariable().instantiate(ass);
    assertTrue(c.toString().equals("qrvt[1,3]"));
    assertTrue(c.queryMinimum() == 0);
    assertTrue(c.queryMaximum() == 3);
  }

  @Test
  public void testNullInstantiate() throws ParserException {
    VariableList lst = new VariableList();
    InputReader.declare("x[i] :: Int? ∈ {0..10} for i ∈ {1..5}", lst);
    ParamBinaryVar x = lst.queryParametrisedBinaryVariable("x");
    Substitution subst = new Substitution("i", expr("3"));
    QuantifiedVariable qrv = new QuantifiedVariable(x, subst);
    VariableInteger c = qrv.instantiate(null);
    assertTrue(c.toString().equals("x[3]"));
    assertTrue(c.queryMinimum() == 0);
    assertTrue(c.queryMaximum() == 10);
  }
}

