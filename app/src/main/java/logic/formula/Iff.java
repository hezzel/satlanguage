package logic.formula;

import logic.sat.Atom;
import logic.sat.Clause;
import logic.sat.ClauseCollection;
import logic.parameter.Substitution;

/** Iff is a formula of the shape a <--> b, where both a and b are also formulas. */
public class Iff extends Formula {
  private Formula _left;
  private Formula _right;

  public Iff(Formula a, Formula b) {
    super(a, b);
    _left = a;
    _right = b;
  }

  /** Returns the negation of this formula, which is exactly ¬a <-> b. */
  public Formula negate() {
    return new Iff(_left.negate(), _right);
  }

  /** @return null because this is not an AtomicFormula */
  public Atom queryAtom() {
    return null;
  }

  /** Instantiates the current formula with a substitution. */
  public Formula substitute(Substitution subst) {
    return new Iff(_left.substitute(subst), _right.substitute(subst));
  }

  /** Adds clauses defining the if-and-only-if to the collection. */
  public void addClauses(ClauseCollection col) {
    if (_left.queryAtom() != null) _right.addClausesDef(_left.queryAtom(), col);
    else if (_right.queryAtom() != null) _left.addClausesDef(_right.queryAtom(), col);
    else {
      Atom tmp = queryAtomFor(_left, col);
      _right.addClausesDef(tmp, col);
    }
  }

  /** Adds clauses for a → this. */
  public void addClausesIfThisIsImpliedBy(Atom a, ClauseCollection col) {
    Atom b = queryAtomFor(_left, col);
    Atom c = queryAtomFor(_right, col);
    col.addClause(new Clause(a.negate(), b.negate(), c));   // -a /\ b -> c
    col.addClause(new Clause(a.negate(), b, c.negate()));   // -a /\ -b -> -c
  }

  /** Adds clauses for this → a. */
  public void addClausesIfThisImplies(Atom a, ClauseCollection col) {
    Atom b = queryAtomFor(_left, col);
    Atom c = queryAtomFor(_right, col);
    col.addClause(new Clause(b.negate(), c.negate(), a)); // b /\ c -> a
    col.addClause(new Clause(b, c, a));                   // -b /\ -c -> a
  }

  public int queryAssocLevel() {
    return Formula.IMPLICATION;
  }

  public String toString() {
    String l = _left.toString();
    String r = _right.toString();
    if (_left.queryAssocLevel() >= queryAssocLevel()) l = "(" + l + ")";
    if (_right.queryAssocLevel() >= queryAssocLevel()) r = "(" + r + ")";
    return l + " ↔ " + r;
  }
}

