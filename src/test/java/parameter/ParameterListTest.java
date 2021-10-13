import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.*;

public class ParameterListTest {
  @Test
  public void testLegalList() {
    PExpression exp1 = new SumExpression(new ParameterExpression("a"), new ConstantExpression(1));
    PExpression exp2 = new ParameterExpression("c");
    Parameter a = new Parameter("a", 0, 5);
    Parameter b = new Parameter("b", 0, 6);
    Parameter c = new Parameter("c", 0, 3, new SmallerConstraint(exp1, exp2));
    ParameterList lst = new ParameterList(a, b, c);
    // no asserts: the check here is that no error is thrown!
  }

  @Test(expected = java.lang.Error.class)
  public void testListWithDuplicate() {
    PExpression exp1 = new SumExpression(new ParameterExpression("a"), new ConstantExpression(1));
    PExpression exp2 = new ParameterExpression("c");
    Parameter a = new Parameter("a", 0, 5);
    Parameter b = new Parameter("a", 0, 6);
    ParameterList lst = new ParameterList(a, b);
  }

  @Test(expected = java.lang.Error.class)
  public void testListWithIllegalConstraint() {
    PExpression exp1 = new SumExpression(new ParameterExpression("a"), new ConstantExpression(1));
    PExpression exp2 = new ParameterExpression("c");
    Parameter a = new Parameter("a", 0, 5);
    Parameter b = new Parameter("b", 0, 6, new SmallerConstraint(exp1, exp2));
    Parameter c = new Parameter("c", 0, 3);
    ParameterList lst = new ParameterList(a, b, c);
  }

  @Test(expected = java.lang.Error.class)
  public void testListWithIllegalMaximum() {
    PExpression expr = new SumExpression(new ParameterExpression("i"), new ConstantExpression(1));
    Parameter a = new Parameter("i", new ConstantExpression(0), expr, new TrueConstraint());
    ParameterList lst = new ParameterList(a);
  }
}

