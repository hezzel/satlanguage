package logic.formula;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.sat.ClauseCollection;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.number.range.RangeInteger;
import logic.number.range.RangeComparison;
import logic.number.QuantifiedInteger;
import java.util.ArrayList;

public class Equals extends Formula {
  private QuantifiedInteger _left;
  private QuantifiedInteger _right;
  private boolean _negated;

  public Equals(QuantifiedInteger left, QuantifiedInteger right, boolean value) {
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
    QuantifiedInteger left = _left.substitute(subst);
    QuantifiedInteger right = _right.substitute(subst);
    return new Equals(left, right, !_negated);
  }

  public void addClauses(ClauseCollection col) {
    if (!queryClosed()) {
      throw new Error("Trying to addClauses for Equals formula with parameters: " + toString());
    }
    RangeInteger l = _left.instantiate(null).getRange();
    RangeInteger r = _right.instantiate(null).getRange();
    l = l.setPracticalBounds(r.queryMinimum() - 1, r.queryMaximum() + 1);
    r = r.setPracticalBounds(l.queryMinimum() - 1, l.queryMaximum() + 1);
    l.addWelldefinednessClauses(col);
    r.addWelldefinednessClauses(col);

    if (!_negated) {  // l = r
      ArrayList<Clause> clauses = RangeComparison.generateEqualClauses(l, r);
      for (int i = 0; i < clauses.size(); i++) col.addClause(clauses.get(i));
    }
    else {  // l != r
      ArrayList<Clause> clauses = RangeComparison.generateNeqClauses(l, r);
      for (int i = 0; i < clauses.size(); i++) col.addClause(clauses.get(i));
    }
  }

  public void addClausesIfThisIsImpliedBy(Atom a, ClauseCollection col) {
    if (!queryClosed()) {
      throw new Error("Trying to addClausesIfThisIsImpliedBy for Equals formula with parameters: "
        + toString());
    }
    RangeInteger l = _left.instantiate(null).getRange();
    RangeInteger r = _right.instantiate(null).getRange();
    l = l.setPracticalBounds(r.queryMinimum() - 1, r.queryMaximum() + 1);
    r = r.setPracticalBounds(l.queryMinimum() - 1, l.queryMaximum() + 1);
    l.addWelldefinednessClauses(col);
    r.addWelldefinednessClauses(col);

    ArrayList<Clause> clauses;
    // a → (b1 ∧ ... ∧ bn) iff (¬a ∨ b1) ∧ ... ∧ (¬a ∨ bn)
    if (!_negated) clauses = RangeComparison.generateEqualClauses(l, r);
    else clauses = RangeComparison.generateNeqClauses(l, r);
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
    RangeInteger l = _left.instantiate(null).getRange();
    RangeInteger r = _right.instantiate(null).getRange();
    l = l.setPracticalBounds(r.queryMinimum() - 1, r.queryMaximum() + 1);
    r = r.setPracticalBounds(l.queryMinimum() - 1, l.queryMaximum() + 1);
    l.addWelldefinednessClauses(col);
    r.addWelldefinednessClauses(col);

    ArrayList<Clause> clauses;
    // X → c iff ¬X ∨ c, and if ¬X is equivalent to x1 ∧ ... ∧ xn, then ¬X ∨ c is equivalent to
    // (x1 ∨ c) ∧ ... ∧ (xn ∨ c)
    if (_negated) clauses = RangeComparison.generateEqualClauses(l, r);
    else clauses = RangeComparison.generateNeqClauses(l, r);

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

