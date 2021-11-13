package logic.formula;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.sat.ClauseCollection;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.number.ClosedInteger;
import logic.number.range.RangeInteger;
import logic.number.range.RangeComparison;
import logic.number.binary.BinaryInteger;
import logic.number.binary.BinaryComparison;
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

  private ArrayList<Clause> generateClauses(String method, ClauseCollection col, boolean neg) {
    if (!queryClosed()) {
      throw new Error("Trying to " + method + " for Equals formula with parameters: " +
        toString());
    }
    ClosedInteger cl = _left.instantiate(null);
    ClosedInteger cr = _right.instantiate(null);
    if (cl.queryKind() != cr.queryKind() && cl.queryKind() != ClosedInteger.BOTH &&
        cr.queryKind() != ClosedInteger.BOTH) {
      throw new Error("Required to compare binary and range integer: " + toString());
    }
    if (cl.queryKind() == ClosedInteger.BINARY || cr.queryKind() == ClosedInteger.BINARY) {
      BinaryInteger l = cl.getBinary();
      BinaryInteger r = cr.getBinary();
      l.addWelldefinednessClauses(col);
      r.addWelldefinednessClauses(col);
      if (neg) return BinaryComparison.generateNeqClauses(l, r);
      else return BinaryComparison.generateEqualClauses(l, r);
    }
    else {
      RangeInteger l = cl.getRange();
      RangeInteger r = cr.getRange();
      l = l.setPracticalBounds(r.queryMinimum() - 1, r.queryMaximum() + 1);
      r = r.setPracticalBounds(l.queryMinimum() - 1, l.queryMaximum() + 1);
      l.addWelldefinednessClauses(col);
      r.addWelldefinednessClauses(col);
      if (neg) return RangeComparison.generateNeqClauses(l, r);
      else return RangeComparison.generateEqualClauses(l, r);
    }
  }

  public void addClauses(ClauseCollection col) {
    ArrayList<Clause> clauses = generateClauses("addClauses", col, _negated);
    for (int i = 0; i < clauses.size(); i++) col.addClause(clauses.get(i));
  }

  public void addClausesIfThisIsImpliedBy(Atom a, ClauseCollection col) {
    ArrayList<Clause> clauses = generateClauses("addClausesIfThisIsImpliedBy", col, _negated);
    Atom aneg = a.negate();
    for (int i = 0; i < clauses.size(); i++) {
      col.addClause(new Clause(aneg, clauses.get(i)));
    }
  }

  public void addClausesIfThisImplies(Atom a, ClauseCollection col) {
    ArrayList<Clause> clauses = generateClauses("addClausesIfThisImplies", col, !_negated);
    // X → c iff ¬X ∨ c, and if ¬X is equivalent to x1 ∧ ... ∧ xn, then ¬X ∨ c is equivalent to
    // (x1 ∨ c) ∧ ... ∧ (xn ∨ c)
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

