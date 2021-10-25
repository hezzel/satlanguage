package logic.parameter;

import java.util.Set;

/** An Expression corresponding to the minimum of two PExpressions. */
public class MinExpression extends BinaryExpression {
  public MinExpression(PExpression l, PExpression r) {
    super(l, r);
  }

  protected MinExpression create(PExpression l, PExpression r) {
    return new MinExpression(l, r);
  }

  public int queryKind() {
    return PExpression.MINIMUM;
  }

  public int evaluate(Assignment assignment) {
    int l = _left.evaluate(assignment);
    int r = _right.evaluate(assignment);
    return l > r ? r : l;
  }

  public String toString() {
    return "min(" + _left.toString() + "," + _right.toString() + ")";
  }
}
