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

public class Geq extends Formula {
  private QuantifiedRangeInteger _left;
  private QuantifiedRangeInteger _right;
  private boolean _negated;

  public Geq(QuantifiedRangeInteger left, QuantifiedRangeInteger right, boolean value) {
    super();
    _left = left;
    _right = right;
    _negated = !value;
    _usedParameters.addAll(left.queryParameters());
    _usedParameters.addAll(right.queryParameters());
  }

  public Geq negate() {
    return new Geq(_left, _right, _negated);
  }

  /**
   * @return null because while this *might* be exactly an Atom in some cases, we generally have to
   * add well-definedness clauses for the range integers.
   */
  public Atom queryAtom() {
    return null;
  }

  /** Instantiates the current conjunction with an assignment. */
  public Formula substitute(Substitution subst) {
    QuantifiedRangeInteger left = _left.substitute(subst);
    QuantifiedRangeInteger right = _right.substitute(subst);
    return new Geq(left, right, !_negated);
  }

  /** Returns the clauses that imply a ≥ b. */
  private ArrayList<Clause> generateGeqClauses(RangeInteger a, RangeInteger b) {
  /* Suppose a ∈ {amin..amax} and b ∈ {bmin..bmax}.  To see that a ≥ b, we must clearly have:
   * - amax ≥ bmin
   * - if bmin > amin, then: a ≥ bmin
   * - if bmax > amax, then: NOT b ≥ amax + 1
   * - ∀ i ∈ {MAX(amin+1,bmin+1) .. MIN(amax,bmax)}. b ≥ i → a ≥ i.
   * The first three items should all hold by transitivity: if a ≥ b, then amax ≥ a ≥ b ≥ bmin
   * and certainly not amax ≥ a ≥ b ≥ amax + 1.  The last item should hold because for ALL
   * integers i we should have that b ≥ i → a ≥ i, again by transitivity of ≥.
   *
   * The above four requirements are also sufficient to show that a ≥ b.  To see this, observe
   * that what we really need to prove is that b ≥ i → a ≥ i for all integers.  Consider i:
   * - If i ≤ amin, then a ≥ i certainly holds, so there is nothing to show.
   * - If amin < i ≤ bmin, then by the second requirement a ≥ bmin, so certainly a ≥ i.
   * - If i > bmax, then b ≥ i does not hold, so there is nothing to show.
   * - If bmax ≥ i > amax, then by the third requirement b < amax+1, so certainly b < i, so b ≥ i
   *   does not hold, and there is nothing to show.
   * - In all other cases, we require b ≥ i → a ≥ i
   */
    ArrayList<Clause> ret = new ArrayList<Clause>();
    if (a.queryMaximum() < b.queryMinimum()) {  // add a ≥ bmin, which is exactly FALSE
      ret.add(new Clause(new Atom(a.queryGeqVariable(b.queryMinimum()), true)));
      return ret;
    }
    if (b.queryMinimum() > a.queryMinimum()) {  // add a ≥ bmin
      ret.add(new Clause(new Atom(a.queryGeqVariable(b.queryMinimum()), true)));
    }
    if (b.queryMaximum() > a.queryMaximum()) {  // add b < amax + 1
      ret.add(new Clause(new Atom(b.queryGeqVariable(a.queryMaximum()+1), false)));
    }
    int min = (b.queryMinimum() > a.queryMinimum() ? b.queryMinimum() : a.queryMinimum()) + 1;
    int max = b.queryMaximum() < a.queryMaximum() ? b.queryMaximum() : a.queryMaximum();
    for (int i = min; i <= max; i++) {
      ret.add(new Clause(new Atom(b.queryGeqVariable(i), false),
                         new Atom(a.queryGeqVariable(i), true)));
    }
    return ret;
  }

  /** Returns the clauses that imply a < b, so b ≥ a+1. */
  private ArrayList<Clause> generateSmallerClauses(RangeInteger a, RangeInteger b) {
    ArrayList<Clause> ret = new ArrayList<Clause>();
    if (b.queryMaximum() < a.queryMinimum() + 1) {  // add b ≥ amax+1, which is exactly FALSE
      ret.add(new Clause(new Atom(b.queryGeqVariable(b.queryMaximum()+1), true)));
      return ret;
    }
    if (a.queryMinimum() + 1 > b.queryMinimum()) {  // add b ≥ (a+1)min = amin+1
      ret.add(new Clause(new Atom(b.queryGeqVariable(a.queryMinimum()+1), true)));
    }
    if (a.queryMaximum() + 1 > b.queryMaximum()) {  // add a+1 < bmax+1, so a < bmax
      ret.add(new Clause(new Atom(a.queryGeqVariable(b.queryMaximum()), false)));
    }
    // ∀ i ∈ {MAX(bmin+1,amin+2) .. MIN(bmax,amax+1)}. a+1 ≥ i → b ≥ i
    int min = (b.queryMinimum() > a.queryMinimum() ? b.queryMinimum() : a.queryMinimum()+1) + 1;
    int max = b.queryMaximum() <= a.queryMaximum() ? b.queryMaximum() : a.queryMaximum() + 1;
    for (int i = min; i <= max; i++) {  // a ≥ i-1 → b ≥ i
      ret.add(new Clause(new Atom(a.queryGeqVariable(i-1), false),
                         new Atom(b.queryGeqVariable(i), true)));
    }
    return ret;
  }

  public void addClauses(ClauseCollection col) {
    if (!queryClosed()) {
      throw new Error("Trying to addClauses for Geq formula with parameters: " + toString());
    }
    RangeInteger l = _left.instantiate(null);
    RangeInteger r = _right.instantiate(null);
    l.addWelldefinednessClauses(col);
    r.addWelldefinednessClauses(col);

    if (!_negated) {  // l ≥ r
      ArrayList<Clause> clauses = generateGeqClauses(l, r);
      for (int i = 0; i < clauses.size(); i++) col.addClause(clauses.get(i));
    }
    else {  // l < r, which holds iff r ≥ l+1
      ArrayList<Clause> clauses = generateSmallerClauses(l, r);
      for (int i = 0; i < clauses.size(); i++) col.addClause(clauses.get(i));
    }
  }

  public void addClausesIfThisIsImpliedBy(Atom a, ClauseCollection col) {
    if (!queryClosed()) {
      throw new Error("Trying to addClausesIfThisIsImpliedBy for Geq formula with parameters: " +
        toString());
    }
    RangeInteger l = _left.instantiate(null);
    RangeInteger r = _right.instantiate(null);
    l.addWelldefinednessClauses(col);
    r.addWelldefinednessClauses(col);

    ArrayList<Clause> clauses;
    // a → (b1 ∧ ... ∧ bn) iff (¬a ∨ b1) ∧ ... ∧ (¬a ∨ bn)
    if (!_negated) clauses = generateGeqClauses(l, r);
    else clauses = generateSmallerClauses(l, r);
    Atom aneg = a.negate();

    for (int i = 0; i < clauses.size(); i++) {
      col.addClause(new Clause(aneg, clauses.get(i)));
    }
  }

  public void addClausesIfThisImplies(Atom a, ClauseCollection col) {
    if (!queryClosed()) {
      throw new Error("Trying to addClausesIfThisIsImpliedBy for Geq formula with parameters: " +
        toString());
    }
    RangeInteger l = _left.instantiate(null);
    RangeInteger r = _right.instantiate(null);
    l.addWelldefinednessClauses(col);
    r.addWelldefinednessClauses(col);

    ArrayList<Clause> clauses;
    // X → c iff ¬X ∨ c, and if ¬X is equivalent to x1 ∧ ... ∧ xn, then ¬X ∨ c is equivalent to
    // (x1 ∨ c) ∧ ... ∧ (xn ∨ c)
    if (_negated) clauses = generateGeqClauses(l, r);
    else clauses = generateSmallerClauses(l, r);

    for (int i = 0; i < clauses.size(); i++) {
      col.addClause(new Clause(a, clauses.get(i)));
    }
  }

  public int queryAssocLevel() {
    return Formula.ATOM;
  }

  public String toString() {
    if (_negated) return _left.toString() + " < " + _right.toString();
    else return _left.toString() + " ≥ " + _right.toString();
  }
}

