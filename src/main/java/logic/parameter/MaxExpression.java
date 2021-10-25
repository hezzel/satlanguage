package logic.parameter;

import java.util.Set;

/** An Expression corresponding to the maximum of two PExpressions. */
public class MaxExpression extends BinaryExpression {
  public MaxExpression(PExpression l, PExpression r) {
    super(l, r);
  }

  protected MaxExpression create(PExpression l, PExpression r) {
    return new MaxExpression(l, r);
  }

  public int queryKind() {
    return PExpression.MAXIMUM;
  }

  public int evaluate(Assignment assignment) {
    int l = _left.evaluate(assignment);
    int r = _right.evaluate(assignment);
    return l > r ? l : r;
  }

  public String toString() {
    return "max(" + _left.toString() + "," + _right.toString() + ")";
  }
}
