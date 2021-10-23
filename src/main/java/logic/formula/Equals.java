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
    // a = b <-> a ≥ b and b ≥ a
    ArrayList<Clause> ret = Geq.generateGeqClauses(a, b);
    ret.addAll(Geq.generateGeqClauses(b, a));
    return ret;
  }

  /** Returns the clauses that imply a ≠ b. */
  private ArrayList<Clause> generateNeqClauses(RangeInteger a, RangeInteger b) {
    ArrayList<Clause> ret = new ArrayList<Clause>();
    // a != b <-> for all i: if a = i then b != i
    //        <-> for all i ∈ {amin..amax}: if a = i then b != i (as otherwise a = i does not hold)
    for (int i = a.queryMinimum(); i <= a.queryMaximum(); i++) {
      // if b cannot be equal to a, then we don't need clauses to get b != i
      if (i < b.queryMinimum() || i > b.queryMaximum()) continue;
      // a = i → b ≠ i <--> a ≥ i ∧ a < i+1 → b < i ∨ b ≥ i+1
      //               <--> ¬(a≥i) ∨ a≥i+1 ∨ ¬b≥i ∨ b≥i+1
      // (and don't add atoms that evaluate to false anyway)
      ArrayList<Atom> parts = new ArrayList<Atom>();
      if (i > a.queryMinimum()) parts.add(new Atom(a.queryGeqVariable(i), false));
      if (i < a.queryMaximum()) parts.add(new Atom(a.queryGeqVariable(i+1), true));
      if (i > b.queryMinimum()) parts.add(new Atom(b.queryGeqVariable(i), false));
      if (i < b.queryMaximum()) parts.add(new Atom(b.queryGeqVariable(i+1), true));
      if (parts.size() == 0) parts.add(new Atom(a.queryGeqVariable(a.queryMaximum()+1), true));
      ret.add(new Clause(parts));
    }
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

