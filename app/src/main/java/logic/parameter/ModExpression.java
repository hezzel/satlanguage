package logic.parameter;

/** An Expression corresponding to the modulo division of two PExpressions. */
public class ModExpression extends BinaryExpression {
  public ModExpression(PExpression l, PExpression r) {
    super(l, r);
  }

  protected ModExpression create(PExpression l, PExpression r) {
    return new ModExpression(l, r);
  }

  public int queryKind() {
    return PExpression.MODULO;
  }

  public int evaluate(Assignment assignment) {
    int k = _right.evaluate(assignment);
    if (k == 0) throw new Error("Division by 0 in Modulo expression " + toString());
    return _left.evaluate(assignment) % k;
  }

  public String toString() {
    String left = _left.toString();
    String right = _right.toString();
    if (_left.queryKind() >= PExpression.PRODUCT) left = "(" + left + ")";
    if (_right.queryKind() >= PExpression.PRODUCT) right = "(" + right + ")";
    return left + "%" + right;
  }
}
