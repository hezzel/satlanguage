package logic.parameter;

/** An Expression corresponding to the sum of two PExpressions. */
public class SumExpression extends BinaryExpression {
  public SumExpression(PExpression l, PExpression r) {
    super(l, r);
  }

  protected SumExpression create(PExpression l, PExpression r) {
    return new SumExpression(l, r);
  }

  public int queryKind() {
    return PExpression.SUM;
  }

  public int evaluate(Assignment assignment) {
    return _left.evaluate(assignment) + _right.evaluate(assignment);
  }

  public PExpression substitute(Substitution substitution) {
    PExpression l = _left.substitute(substitution);
    PExpression r = _right.substitute(substitution);

    if (l.queryConstant() && r.queryConstant()) return l.add(r.evaluate(null));
    if (l.queryConstant()) return r.add(l.evaluate(null));
    if (r.queryConstant()) return l.add(r.evaluate(null));

    int c = 0;
    if (l.queryKind() == PExpression.SUM && l.queryRight().queryConstant()) {
      c += l.queryRight().evaluate(null);
      l = l.queryLeft();
    }
    if (l.queryKind() == PExpression.SUM && l.queryRight().queryConstant()) {
      c += r.queryRight().evaluate(null);
      r = r.queryLeft();
    }

    if (c == 0) return new SumExpression(l, r);
    return new SumExpression(new SumExpression(l, r), new ConstantExpression(c));
  }

  public PExpression add(int num) {
    if (num == 0) return this;

    PExpression l = _left;
    PExpression r = _right;
    if (l.queryConstant()) l = l.add(num);
    else r = r.add(num);

    if (l.queryConstant() && l.evaluate(null) == 0) return r;
    if (r.queryConstant() && r.evaluate(null) == 0) return l;
    if (l.queryConstant()) return new SumExpression(r, l);
    if (r.queryConstant()) return new SumExpression(l, r);

    if (r.queryKind() == PExpression.SUM && r.queryRight().queryConstant()) {
      return new SumExpression(new SumExpression(l, r.queryLeft()),
                               r.queryRight());
    }
    return new SumExpression(l, r);
  }

  public PExpression multiply(int num) {
    if (num == 0) return new ConstantExpression(0);
    PExpression l = _left.multiply(num);
    PExpression r = _right.multiply(num);
    return new SumExpression(l, r);
  }

  public String toString() {
    String r = _right.toString();
    if (r.charAt(0) == '-') return _left.toString() + r;
    else return _left.toString() + "+" + r;
  }
}
