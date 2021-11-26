import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.parameter.*;

public class ParamBoolVarTest {
  private Variable falseVariable() {
    return new Variable("FALSE");
  }
  
  private ParamBoolVar makeVar(String name) {
    PConstraint constr =
      new NeqConstraint(new ParameterExpression("a"), new ParameterExpression("b"));
    Parameter a = new Parameter("a", 0, 3); 
    Parameter b = new Parameter("b", 2, 4, constr); 
    ParameterList params = new ParameterList(a, b);
    return new ParamBoolVar(name, params, falseVariable());
  }

  @Test
  public void testGetValidVariable() {
    Assignment ass = new Assignment();
    ass.put("a", 0);
    ass.put("b", 4);
    ParamBoolVar test = makeVar("test");
    assertTrue(test.queryVar(ass) != null);
    assertTrue(test.queryVar(ass).toString().equals("test[0,4]"));
  }

  @Test
  public void testGetOutOfRangeVariable() {
    Assignment ass = new Assignment();
    ass.put("a", 1);
    ass.put("b", 5);
    ParamBoolVar test = makeVar("xxx");
    assertTrue(test.queryVar(ass) != null);
    assertTrue(test.queryVar(ass).equals(falseVariable()));
  }

  @Test
  public void testGetOutOfDynamicRangeVariable() {
    Assignment ass = new Assignment();
    ass.put("a", 1);
    ass.put("b", 2);
    PExpression ap2 = new SumExpression(new ParameterExpression("a"), new ConstantExpression(2));
    Parameter a = new Parameter("a", 0, 3); 
    Parameter b = new Parameter("b", ap2, new ConstantExpression(4), new TrueConstraint()); 
    ParamBoolVar test = new ParamBoolVar("qqq", new ParameterList(a, b), falseVariable());
    assertTrue(test.queryVar(ass).equals(falseVariable()));
  }

  @Test
  public void testGetRestrictionViolatingVariable() {
    Assignment ass = new Assignment();
    ass.put("a", 3);
    ass.put("b", 3);
    ParamBoolVar test = makeVar("yyy");
    assertTrue(test.queryVar(ass) != null);
    assertTrue(test.queryVar(ass).equals(falseVariable()));
  }

  @Test(expected = java.lang.Error.class)
  public void testInsufficientParameterGet() {
    Assignment ass = new Assignment();
    ass.put("a", 1);
    ParamBoolVar test = makeVar("zzz");
    test.queryVar(ass);
  }
}

