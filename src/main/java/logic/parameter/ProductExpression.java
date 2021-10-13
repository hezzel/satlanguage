package logic.parameter;

import java.util.Set;

/** An Expression corresponding to the product of two PExpressions. */
public class ProductExpression implements PExpression {
  private PExpression _left;
  private PExpression _right;

  public ProductExpression(PExpression l, PExpression r) {
    _left = l;
    _right = r;
  }

  public int queryKind() {
    return PExpression.PRODUCT;
  }

  public PExpression queryLeft() {
    return _left;
  }

  public PExpression queryRight() {
    return _right;
  }

  public int evaluate(Assignment assignment) {
    return _left.evaluate(assignment) * _right.evaluate(assignment);
  }

  public PExpression substitute(Substitution substitution) {
    PExpression l = _left.substitute(substitution);
    PExpression r = _right.substitute(substitution);
    if (l.queryConstant()) {
      int k = l.evaluate(null);
      if (k == 0) return l;
      if (k == 1) return r;
      if (r.queryConstant()) return new ConstantExpression(k * r.evaluate(null));
      if (r.queryKind() == PExpression.PRODUCT && r.queryLeft().queryConstant()) {
        PExpression ll = new ConstantExpression(k * r.queryLeft().evaluate(null));
        return new ProductExpression(ll, r.queryRight());
      }
    }
    if (r.queryConstant()) {
      int k = r.evaluate(null);
      if (k == 0) return r;
      if (k == 1) return l;
      if (l.queryKind() == PExpression.PRODUCT && l.queryLeft().queryConstant()) {
        PExpression ll = new ConstantExpression(k * l.queryLeft().evaluate(null));
        return new ProductExpression(ll, l.queryRight());
      }
      return new ProductExpression(r, l);
    }
    if (r.queryKind() == PExpression.PRODUCT && r.queryLeft().queryConstant()) {
      int k = r.queryLeft().evaluate(null);
      PExpression rr = r.queryRight();
      PExpression ll = l;
      if (l.queryKind() == PExpression.PRODUCT && l.queryLeft().queryConstant()) {
        k *= l.queryLeft().evaluate(null);
        ll = l.queryRight();
      }
      return new ProductExpression(new ConstantExpression(k), new ProductExpression(ll, rr));
    }
    if (l == _left && r == _right) return this;
    return new ProductExpression(l, r);
  }

  public PExpression substitute(Assignment assignment) {
    return substitute(new Substitution(assignment));
  }

  public boolean queryConstant() {
    return false;
  }

  public PExpression add(int num) {
    if (num == 0) return this;
    return new SumExpression(this, new ConstantExpression(num));
  }

  public Set<String> queryParameters() {
    Set<String> ret = _left.queryParameters();
    ret.addAll(_right.queryParameters());
    return ret;
  }

  public String toString() {
    String left = _left.toString();
    String right = _right.toString();
    if (_left.queryKind() > queryKind()) left = "(" + left + ")";
    if (_right.queryKind() > queryKind()) right = "(" + right + ")";
    return left + "*" + right;
  }

  public boolean equals(PExpression expr) {
    if (!(expr instanceof ProductExpression)) return false;
    ProductExpression other = (ProductExpression)expr;
    return other._left.equals(_left) && other._right.equals(_right);
  }
}
