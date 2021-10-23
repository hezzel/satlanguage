package logic.formula;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.sat.ClauseCollection;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.range.RangeInteger;
import logic.range.QuantifiedRangeInteger;
import java.util.ArrayList;

public class Equals extends Formula {
  private QuantifiedRangeInteger _left;
  private QuantifiedRangeInteger _right;
  private boolean _negated;

  public Equals(QuantifiedRangeInteger left, QuantifiedRangeInteger right, boolean value) {
    super();
    _left = left;
    _right = right;
    _negated = !value;
    _usedParameters.addAll(left.queryParameters());
    _usedParameters.addAll(right.queryParameters());
  }

  public Equals negate() {
    return new Equals(_left, _right, _negated);
  }

  /** @return null */
  public Atom queryAtom() {
    return null;
  }

  /** Instantiates the current conjunction with an assignment. */
  public Formula substitute(Substitution subst) {
    QuantifiedRangeInteger left = _left.substitute(subst);
    QuantifiedRangeInteger right = _right.substitute(subst);
    return new Equals(left, right, !_negated);
  }

  /** Returns the clauses that imply a = b. */
  private ArrayList<Clause> generateEqualClauses(RangeInteger a, RangeInteger b) {
    ArrayList<Clause> ret = new ArrayList<Clause>();
    /* To see that a = b, we must certainly have:
     *
     * (a) amax ≥ bmin and bmax ≥ amin
     * (b) if amin > bmin then b ≥ amin
     * (c) if bmin > amin then a ≥ bmin
     * (d) if amax > bmax then a < bmax + 1
     * (e) if bmax > amax then b < amax + 1
     * (f) for all i in {MAX(amin,bmin)+1 .. MIN(amax,bmax)}: a ≥ i <-> b ≥ i.
     *
     * After all, a = b implies amax ≥ a ≥ b ≥ bmin and bmax ≥ b ≥ a ≥ amin.  We also have that if
     * those requirements are satisfied, then a = b, since we then have ∀ i. a ≥ i <-> b ≥ i:
     * - for i ≤ amin and i ≤ bmin, then both a ≥ i and b ≥ i are satisfied
     * - if i > amax and i > bmax, then a ≥ i and b ≥ i are both not satisfied
     * - if bmin < i ≤ amin, then a ≥ i certainly holds, and b ≥ i holds by (b): b ≥ amin ≥ i
     * - if amin < i ≤ bmin, then b ≥ i certainly holds, and a ≥ i holds by (c): a ≥ bmin ≥ i
     * - if amax ≥ i > bmax, then b ≥ i does not hold, and a ≥ i fails by (d): a < bmax+1 ≤ i
     * - if bmax ≥ i > amax, then a ≥ i does not hold, and b ≥ i fails by (e): b < amax+1 ≤ i
     */
    return ret;
  }

  /** Returns the clauses that imply a != b. */
  private ArrayList<Clause> generateNeqClauses(RangeInteger a, RangeInteger b) {
    ArrayList<Clause> ret = new ArrayList<Clause>();
    return ret;
  }

  public void addClauses(ClauseCollection col) {
    if (!queryClosed()) {
      throw new Error("Trying to addClauses for Equals formula with parameters: " + toString());
    }
    RangeInteger l = _left.instantiate(null);
    RangeInteger r = _right.instantiate(null);
    l.addWelldefinednessClauses(col);
    r.addWelldefinednessClauses(col);

    if (!_negated) {  // l = r
      ArrayList<Clause> clauses = generateEqualClauses(l, r);
      for (int i = 0; i < clauses.size(); i++) col.addClause(clauses.get(i));
    }
    else {  // l != r
      ArrayList<Clause> clauses = generateNeqClauses(l, r);
      for (int i = 0; i < clauses.size(); i++) col.addClause(clauses.get(i));
    }
  }

  public void addClausesIfThisIsImpliedBy(Atom a, ClauseCollection col) {
    if (!queryClosed()) {
      throw new Error("Trying to addClausesIfThisIsImpliedBy for Equals formula with parameters: "
        + toString());
    }
    RangeInteger l = _left.instantiate(null);
    RangeInteger r = _right.instantiate(null);
    l.addWelldefinednessClauses(col);
    r.addWelldefinednessClauses(col);

    ArrayList<Clause> clauses;
    // a → (b1 ∧ ... ∧ bn) iff (¬a ∨ b1) ∧ ... ∧ (¬a ∨ bn)
    if (!_negated) clauses = generateEqualClauses(l, r);
    else clauses = generateNeqClauses(l, r);
    Atom aneg = a.negate();

    for (int i = 0; i < clauses.size(); i++) {
      col.addClause(new Clause(aneg, clauses.get(i)));
    }
  }

  public void addClausesIfThisImplies(Atom a, ClauseCollection col) {
    if (!queryClosed()) {
      throw new Error("Trying to addClausesIfThisIsImpliedBy for Equals formula with parameters: "
        + toString());
    }
    RangeInteger l = _left.instantiate(null);
    RangeInteger r = _right.instantiate(null);
    l.addWelldefinednessClauses(col);
    r.addWelldefinednessClauses(col);

    ArrayList<Clause> clauses;
    // X → c iff ¬X ∨ c, and if ¬X is equivalent to x1 ∧ ... ∧ xn, then ¬X ∨ c is equivalent to
    // (x1 ∨ c) ∧ ... ∧ (xn ∨ c)
    if (_negated) clauses = generateEqualClauses(l, r);
    else clauses = generateNeqClauses(l, r);

    for (int i = 0; i < clauses.size(); i++) {
      col.addClause(new Clause(a, clauses.get(i)));
    }
  }

  public int queryAssocLevel() {
    return Formula.ATOM;
  }

  public String toString() {
    if (_negated) return _left.toString() + " ≠ " + _right.toString();
    else return _left.toString() + " = " + _right.toString();
  }
}

