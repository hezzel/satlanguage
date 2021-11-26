import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.parameter.Assignment;
import logic.parameter.Parameter;
import logic.parameter.ParameterList;
import logic.parameter.PExpression;
import logic.number.binary.BinaryVariable;
import logic.number.binary.ParamBinaryVar;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParamBinaryVarTest {
  private Atom truth() { return new Atom(new Variable("TRUE"), true); }

  @Test
  public void testSimpleBinaryVar() throws ParserException {
    ParameterList lst = new ParameterList(InputReader.readParameterFromString("i ∈ {1..10}"));
    ParamBinaryVar x = new ParamBinaryVar("x", lst, 8, true, truth());
    BinaryVariable x7 = x.queryVar(new Assignment("i", 7));
    assertTrue(x7.queryMinimum() == -256);
    assertTrue(x7.queryMaximum() == 255);
    assertTrue(x7.toString().equals("x[7]"));
    assertTrue(x7.queryBit(5).toString().equals("x[7]⟨5⟩"));
  }

  @Test
  public void testSimpleNonNegativeBinaryVar() throws ParserException {
    ParameterList lst = new ParameterList(InputReader.readParameterFromString("i ∈ {1..10}"));
    ParamBinaryVar y = new ParamBinaryVar("y", lst, 7, false, truth());
    BinaryVariable y1 = y.queryVar(new Assignment("i", 1));
    assertTrue(y1.queryMinimum() == 0);
    assertTrue(y1.queryMaximum() == 127);
    assertTrue(y1.toString().equals("y[1]"));
    assertTrue(y1.queryNegativeBit().toString().equals("¬TRUE"));
    assertTrue(y1.queryBit(2).toString().equals("y[1]⟨2⟩"));
    ClauseCollector col = new ClauseCollector();
    y.addWelldefinednessClauses(col);
    assertTrue(col.size() == 0);
  }

  @Test(expected = java.lang.Error.class)
  public void testSimpleBinaryVarOutsideDomain() throws ParserException {
    ParameterList lst = new ParameterList(InputReader.readParameterFromString("i ∈ {1..10}"));
    ParamBinaryVar x = new ParamBinaryVar("x", lst, 8, true, truth());
    BinaryVariable x1 = x.queryVar(new Assignment("i", 0));
  }

  @Test
  public void testBinaryVariableWithRange() throws ParserException {
    ParameterList lst = new ParameterList(InputReader.readParameterFromString("i ∈ {1..10}"));
    PExpression min = InputReader.readPExpressionFromString("0");
    PExpression max = InputReader.readPExpressionFromString("2*i");
    ParamBinaryVar x = new ParamBinaryVar("x", lst, min, max, truth());
    BinaryVariable x4 = x.queryVar(new Assignment("i", 4));
    assertTrue(x4.queryMinimum() == 0);
    assertTrue(x4.queryMaximum() == 8);
    assertTrue(x4.length() == 4);
    assertTrue(x4.queryNegativeBit().toString().equals("¬TRUE"));
    assertTrue(x4.queryBit(3).toString().equals("x[4]⟨3⟩"));
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    // contains the clauses for x[4]: x[4]⟨i⟩ → ¬x[4]⟨3⟩ for i ∈ {0..2}
    assertTrue(col.contains("¬x[4]⟨0⟩ ∨ ¬x[4]⟨3⟩"));
    assertTrue(col.contains("¬x[4]⟨1⟩ ∨ ¬x[4]⟨3⟩"));
    assertTrue(col.contains("¬x[4]⟨2⟩ ∨ ¬x[4]⟨3⟩"));
  }

  @Test(expected = java.lang.Error.class)
  public void testParamBinaryVarReliesOnOutsideParameter() throws ParserException {
    ParameterList lst = new ParameterList(InputReader.readParameterFromString("i ∈ {1..10}"));
    PExpression min = InputReader.readPExpressionFromString("i");
    PExpression max = InputReader.readPExpressionFromString("j");
    ParamBinaryVar y = new ParamBinaryVar("y", lst, min, max, truth());
  }
}

