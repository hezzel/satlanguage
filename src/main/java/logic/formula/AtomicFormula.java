package logic.formula;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.sat.ClauseCollection;
import logic.parameter.Substitution;

/** A formula representing a single atom (a variable or negated variable). */
public class AtomicFormula extends Formula {
  private Atom _atom;

  public AtomicFormula(Atom atom) {
    super();
    _atom = atom;
  }

  public AtomicFormula(Variable x) {
    super();
    _atom = new Atom(x, true);
  }

  public AtomicFormula(Variable x, boolean value) {
    super();
    _atom = new Atom(x, value);
  }

  public AtomicFormula negate() {
    return new AtomicFormula(_atom.negate());
  }

  public Atom queryAtom() {
    return _atom;
  }

  public Formula substitute(Substitution subst) {
    return this;
  }

  public void addClauses(ClauseCollection col) {
    col.addClause(new Clause(_atom));
  }

  /** Adds clauses corresponding to x → this to col. */
  public void addClausesIfThisIsImpliedBy(Atom x, ClauseCollection col) {
    col.addClause(new Clause(x.negate(), _atom));
  }

  /** Adds clauses corresponding to this → x to col. */
  public void addClausesIfThisImplies(Atom x, ClauseCollection col) {
    col.addClause(new Clause(_atom.negate(), x));
  }

  /** @return 0 */
  public int queryAssocLevel() {
    return Formula.ATOM;
  }

  public String toString() {
    return _atom.toString();
  }
}

