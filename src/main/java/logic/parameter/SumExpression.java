package logic.parameter;

import java.util.Set;

/** An Expression corresponding to the sum of two PExpressions. */
public class SumExpression implements PExpression {
  private PExpression _left;
  private PExpression _right;

  public SumExpression(PExpression l, PExpression r) {
    _left = l;
    _right = r;
  }

  public int queryKind() {
    return PExpression.SUM;
  }

  public PExpression queryLeft() {
    return _left;
  }

  public PExpression queryRight() {
    return _right;
  }

  public int evaluate(Assignment assignment) {
    return _left.evaluate(assignment) + _right.evaluate(assignment);
  }

  public PExpression substitute(Substitution substitution) {
    PExpression l = _left.substitute(substitution);
    PExpression r = _right.substitute(substitution);
    if (l.queryConstant()) {
      if (r.queryConstant()) return new ConstantExpression(l.evaluate(null) + r.evaluate(null));
      if (r.queryKind() == PExpression.SUM && r.queryRight().queryKind() == PExpression.CONSTANT) {
        PExpression ll = r.queryLeft();
        int k = l.evaluate(null) + r.queryRight().evaluate(null);
        if (k == 0) return ll;
        return new SumExpression(ll, new ConstantExpression(k));
      }
      else return new SumExpression(r, l);
    }
    if (r.queryConstant()) {
      if (l.queryKind() == PExpression.SUM && l.queryRight().queryKind() == PExpression.CONSTANT) {
        PExpression ll = l.queryLeft();
        int k = r.evaluate(null) + l.queryRight().evaluate(null);
        if (k == 0) return ll;
        return new SumExpression(ll, new ConstantExpression(k));
      }
    }
    if (l.queryKind() == PExpression.SUM && l.queryRight().queryConstant()) {
      PExpression ll = l.queryLeft();
      PExpression rr = r;
      int k = l.queryRight().evaluate(null);
      if (r.queryKind() == PExpression.SUM && r.queryRight().queryConstant()) {
        rr = r.queryLeft();
        k += r.queryRight().evaluate(null);
      }
      if (k == 0) return new SumExpression(ll, rr);
      else return new SumExpression(new SumExpression(ll, rr), new ConstantExpression(k));
    }
    if (l == _left && r == _right) return this;
    return new SumExpression(l, r);
  }

  public PExpression substitute(Assignment assignment) {
    return substitute(new Substitution(assignment));
  }

  public boolean queryConstant() {
    return false;
  }

  public PExpression add(int num) {
    if (_left.queryConstant()) return new SumExpression(_left.add(num), _right);
    else return new SumExpression(_left, _right.add(num));
  }

  public Set<String> queryParameters() {
    Set<String> ret = _left.queryParameters();
    ret.addAll(_right.queryParameters());
    return ret;
  }

  public String toString() {
    if (_right.queryConstant()) {
      int eval = _right.evaluate(null);
      if (eval < 0) return _left.toString() + "-" + (-eval);
      else return _left.toString() + "+" + eval;
    }
    else return _left.toString() + "+" + _right.toString();
  }

  public boolean equals(PExpression expr) {
    if (!(expr instanceof SumExpression)) return false;
    SumExpression other = (SumExpression)expr;
    return other._left.equals(_left) && other._right.equals(_right);
  }
}
