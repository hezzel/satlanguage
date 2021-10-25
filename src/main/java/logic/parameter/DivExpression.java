package logic.parameter;

import java.util.Set;

/** An Expression corresponding to the (truncated) division of two PExpressions. */
public class DivExpression extends BinaryExpression {
  public DivExpression(PExpression l, PExpression r) {
    super(l, r);
  }

  protected DivExpression create(PExpression l, PExpression r) {
    return new DivExpression(l, r);
  }

  public int queryKind() {
    return PExpression.DIVISION;
  }

  public int evaluate(Assignment assignment) {
    int k = _right.evaluate(assignment);
    if (k == 0) throw new Error("Division by 0 in expression " + toString());
    return _left.evaluate(assignment) / _right.evaluate(assignment);
  }

  public String toString() {
    String left = _left.toString();
    String right = _right.toString();
    if (_left.queryKind() >= PExpression.PRODUCT) left = "(" + left + ")";
    if (_right.queryKind() >= PExpression.PRODUCT) right = "(" + right + ")";
    return left + "/" + right;
  }
}
