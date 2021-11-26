package logic.parameter;

import java.util.Set;

/** A Constraint of the form expr1 < expr2. */
public class SmallerConstraint implements PConstraint {
  private PExpression _left;
  private PExpression _right;

  public SmallerConstraint(PExpression l, PExpression r) {
    _left = l;
    _right = r;
  }

  public boolean evaluate(Assignment assignment) {
    return _left.evaluate(assignment) < _right.evaluate(assignment);
  }

  public boolean isTop() {
    return false;
  }

  public int queryKind() {
    return PConstraint.RELATION;
  }

  private static PConstraint rebalance(PExpression left, PExpression right) {
    PExpression ll = left, rr = right;
    int lc = 0, rc = 0;

    if (left.queryConstant()) { ll = null; lc = left.evaluate(null); }
    else if (left.queryKind() == PExpression.SUM) {
      PExpression a = left.queryLeft();
      PExpression b = left.queryRight();
      if (b.queryConstant()) { ll = a; lc = b.evaluate(null); }
    }

    if (right.queryConstant()) { rr = null; rc = right.evaluate(null); }
    if (right.queryKind() == PExpression.SUM) {
      PExpression a = right.queryLeft();
      PExpression b = right.queryRight();
      if (b.queryConstant()) { rr = a; rc = b.evaluate(null); }
    }

    // both sides are constants
    if (ll == null && rr == null) {
      if (lc < rc) return new TrueConstraint();
      else return new FalseConstraint();
    }
    // only one side is a constant -- move all constants there
    if (ll == null) return new SmallerConstraint(new ConstantExpression(lc - rc), rr);
    if (rr == null) return new SmallerConstraint(ll, new ConstantExpression(rc - lc));
    // neither side is a constant; move both additions to the right
    return new SmallerConstraint(ll, rr.add(rc - lc));
  }

  public PConstraint substitute(Substitution subst) {
    PExpression l = _left.substitute(subst);
    PExpression r = _right.substitute(subst);
    if (l.queryConstant() && r.queryConstant()) {
      if (l.evaluate(null) < r.evaluate(null)) return new TrueConstraint();
      else return new FalseConstraint();
    }   
    return rebalance(l, r);
  }

  public PConstraint negate() {
    // x < y <--> y < x + 1
    Substitution empty = new Substitution();  // substitute(empty) gives a simplification
    return rebalance(_right.substitute(empty), _left.substitute(empty).add(1));
  }

  public Set<String> queryParameters() {
    Set<String> ret = _left.queryParameters();
    ret.addAll(_right.queryParameters());
    return ret;
  }

  public String toString() {
    return _left.toString() + " < " + _right.toString();
  }
}

