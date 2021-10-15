package logic.formula;

import logic.sat.ClauseCollection;
import logic.parameter.Substitution;
import java.util.ArrayList;

/** An Implication has the form a → b, where a and b are formulas. */
public class Implication extends SugarFormula {
  private Formula _left;
  private Formula _right;

  /** Creates the implication a → b. */
  public Implication(Formula a, Formula b) {
    super(a, b);
    _left = a;
    _right = b;
  }

  /** Returns the negation of this formula, which is exactly a ∧ ¬b. */
  public Formula negate() {
    return new And(_left, _right.negate());
  }

  /** Translates the implication into the corresponding disjunction. */
  public Formula translate() {
    return new Or(_left.negate(), _right);
  }

  public Formula substitute(Substitution subst) {
    return new Implication(_left.substitute(subst), _right.substitute(subst));
  }

  public void addClauses(ClauseCollection coll) {
    if (_left.queryAtom() != null) _right.addClausesIfThisIsImpliedBy(_left.queryAtom(), coll);
    else if (_right.queryAtom() != null) _left.addClausesIfThisImplies(_right.queryAtom(), coll);
    else super.addClauses(coll);
  }

  public int queryAssocLevel() {
    return Formula.IMPLICATION;
  }

  public String toString() {
    String l = _left.toString();
    String r = _right.toString();
    if (_left.queryAssocLevel() >= queryAssocLevel()) l = "(" + l + ")";
    if (_right.queryAssocLevel() >= queryAssocLevel()) r = "(" + r + ")";
    return l + " → " + r;
  }
}

