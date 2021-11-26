package logic.parameter;

import java.util.Set;

/** An Expression that is built up from two given PExpressions. */
abstract class BinaryExpression implements PExpression {
  protected PExpression _left;
  protected PExpression _right;

  public BinaryExpression(PExpression l, PExpression r) {
    _left = l;
    _right = r;
  }

  /** Should call the constructor. */
  protected abstract BinaryExpression create(PExpression l, PExpression r);

  public PExpression queryLeft() {
    return _left;
  }

  public PExpression queryRight() {
    return _right;
  }

  public int evaluate(Assignment assignment) {
    return _left.evaluate(assignment) % _right.evaluate(assignment);
  }

  public PExpression substitute(Substitution substitution) {
    PExpression l = _left.substitute(substitution);
    PExpression r = _right.substitute(substitution);
    if (l == _left && r == _right) return this;
    else return create(l, r);
  }

  public boolean queryConstant() {
    return false;
  }

  public PExpression add(int num) {
    if (num == 0) return this;
    return new SumExpression(this, new ConstantExpression(num));
  }

  public PExpression multiply(int num) {
    if (num == 0) return new ConstantExpression(0);
    if (num == 1) return this;
    return new ProductExpression(new ConstantExpression(num), this);
  }

  public Set<String> queryParameters() {
    Set<String> ret = _left.queryParameters();
    ret.addAll(_right.queryParameters());
    return ret;
  }

  public boolean equals(PExpression expr) {
    return expr.queryKind() == queryKind() &&
           _left.equals(expr.queryLeft()) &&
           _right.equals(expr.queryRight());
  }
}
