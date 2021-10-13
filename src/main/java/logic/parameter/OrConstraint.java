package logic.parameter;

import java.util.Set;

/** A Constraint of the form constr1 ∨ constr2. */
public class OrConstraint implements PConstraint {
  private PConstraint _left;
  private PConstraint _right;

  public OrConstraint(PConstraint l, PConstraint r) {
    _left = l;
    _right = r;
  }

  public boolean evaluate(Assignment assignment) {
    return _left.evaluate(assignment) || _right.evaluate(assignment);
  }

  public boolean isTop() {
    return false;
  }

  public int queryKind() {
    return PConstraint.OR;
  }

  public PConstraint substitute(Substitution subst) {
    PConstraint fst = _left.substitute(subst);
    if (fst.isTop()) return fst;
    if (fst.queryKind() == PConstraint.CONSTANT) return _right.substitute(subst);
    PConstraint snd = _right.substitute(subst);
    if (snd.isTop()) return snd;
    if (snd.queryKind() == PConstraint.CONSTANT) return fst;
    if (fst == _left && snd == _right) return this;
    return new OrConstraint(fst, snd);
  }

  public Set<String> queryParameters() {
    Set<String> ret = _left.queryParameters();
    ret.addAll(_right.queryParameters());
    return ret;
  }

  public String toString() {
    String l = _left.toString();
    String r = _right.toString();
    if (_left.queryKind() == PConstraint.AND) l = "(" + l + ")";
    if (_right.queryKind() == PConstraint.AND) r = "(" + r + ")";
    return l + " ∨ " + r;
  }
}
