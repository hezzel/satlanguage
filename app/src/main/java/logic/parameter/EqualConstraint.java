package logic.parameter;

import java.util.Set;

/** A Constraint of the form expr1 = expr2. */
public class EqualConstraint implements PConstraint {
  private PExpression _left;
  private PExpression _right;

  public EqualConstraint(PExpression l, PExpression r) {
    _left = l;
    _right = r;
  }

  public boolean evaluate(Assignment assignment) {
    return _left.evaluate(assignment) == _right.evaluate(assignment);
  }

  public boolean isTop() {
    return false;
  }

  public int queryKind() {
    return PConstraint.RELATION;
  }

  public PConstraint substitute(Substitution subst) {
    PExpression l = _left.substitute(subst);
    PExpression r = _right.substitute(subst);
    if (l.queryConstant() && r.queryConstant()) {
      if (l.evaluate(null) == r.evaluate(null)) return new TrueConstraint();
      else return new FalseConstraint();
    }
    if (_left == l && _right == r) return this;
    return new EqualConstraint(_left.substitute(subst), _right.substitute(subst));
  }

  public PConstraint negate() {
    return new NeqConstraint(_left, _right);
  }

  public Set<String> queryParameters() {
    Set<String> ret = _left.queryParameters();
    ret.addAll(_right.queryParameters());
    return ret;
  }

  public String toString() {
    return _left.toString() + " = " + _right.toString();
  }
}

