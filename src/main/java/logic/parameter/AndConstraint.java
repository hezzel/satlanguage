package logic.parameter;

import java.util.Set;

/** A Constraint of the form constr1 ∧ constr2. */
public class AndConstraint implements PConstraint {
  private PConstraint _left;
  private PConstraint _right;

  public AndConstraint(PConstraint l, PConstraint r) {
    _left = l;
    _right = r;
  }

  public boolean evaluate(Assignment assignment) {
    return _left.evaluate(assignment) && _right.evaluate(assignment);
  }

  public boolean isTop() {
    return false;
  }

  public int queryKind() {
    return PConstraint.AND;
  }

  public PConstraint queryLeft() {
    return _left;
  }

  public PConstraint queryRight() {
    return _right;
  }

  public PConstraint substitute(Substitution subst) {
    PConstraint fst = _left.substitute(subst);
    if (fst.isTop()) return _right.substitute(subst);
    if (fst.queryKind() == PConstraint.CONSTANT) return fst;  // it's ⊥
    PConstraint snd = _right.substitute(subst);
    if (snd.isTop()) return fst;
    if (snd.queryKind() == PConstraint.CONSTANT) return snd;  // it's ⊥
    if (fst == _left && snd == _right) return this;
    return new AndConstraint(fst, snd);
  }

  public Set<String> queryParameters() {
    Set<String> ret = _left.queryParameters();
    ret.addAll(_right.queryParameters());
    return ret;
  }

  public String toString() {
    String l = _left.toString();
    String r = _right.toString();
    if (_left.queryKind() == PConstraint.OR) l = "(" + l + ")";
    if (_right.queryKind() == PConstraint.OR) r = "(" + r + ")";
    return l + " ∧ " + r;
  }
}
