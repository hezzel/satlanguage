package logic.formula;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.sat.ClauseCollection;
import logic.parameter.Substitution;

/** IfThenElse is a formula that means "a → b ∧ ¬a → c, where a, b and c are all formulas. */
public class IfThenElse extends Formula {
  private Formula _condition;
  private Formula _thenform;
  private Formula _elseform;

  public IfThenElse(Formula cond, Formula a, Formula b) {
    super(cond, a, b);
    _condition = cond;
    _thenform = a;
    _elseform = b;
  }

  /** Returns the negation of this formula, which is exactly ite(a, →b, ¬c). */
  public Formula negate() {
    return new IfThenElse(_condition, _thenform.negate(), _elseform.negate());
  }

  /** @return null because this is not an AtomicFormula */
  public Atom queryAtom() {
    return null;
  }

  /** Substitutes the current formula by passing the substitution on to the children. */
  public Formula substitute(Substitution subst) {
    return new IfThenElse(_condition.substitute(subst),
                          _thenform.substitute(subst), _elseform.substitute(subst));
  }

  /** Adds clauses defining the if-then-else to the collection. */
  public void addClauses(ClauseCollection col) {
    Atom c = queryAtomFor(_condition, col);
    _thenform.addClausesIfThisIsImpliedBy(c, col);
    _elseform.addClausesIfThisIsImpliedBy(c.negate(), col);
  }

  /** Adds clauses for a → this. */
  public void addClausesIfThisIsImpliedBy(Atom a, ClauseCollection col) {
    Atom c = queryAtomFor(_condition, col);
    Atom t = queryAtomFor(_thenform, col);
    Atom e = queryAtomFor(_elseform, col);
    Atom aneg = a.negate();
    col.addClause(new Clause(aneg, c.negate(), t));   // a /\ c -> t
    col.addClause(new Clause(aneg, c, e));            // a /\ -c -> e
  }

  /** Adds clauses for this → a. */
  public void addClausesIfThisImplies(Atom a, ClauseCollection col) {
    Atom c = queryAtomFor(_condition, col);
    Atom t = queryAtomFor(_thenform, col);
    Atom e = queryAtomFor(_elseform, col);
    col.addClause(new Clause(c.negate(), t.negate(), a));   // c /\ t -> a
    col.addClause(new Clause(c, e.negate(), a));            // -c /\ e -> a
  }

  public int queryAssocLevel() {
    return Formula.ATOM;
  }

  public String toString() {
    String c = _condition.toString();
    String t = _thenform.toString();
    String e = _elseform.toString();
    return "ite(" + c + ", " + t + ", " + e + ")";
  }
}

