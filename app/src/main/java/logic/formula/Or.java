package logic.formula;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.sat.ClauseCollection;
import logic.parameter.Substitution;
import java.util.ArrayList;

public class Or extends Formula {
  private ArrayList<Formula> _parts;

  /** Helper for the constructor: adds a formula to parts, and flattens Or parts. */
  private void addPart(Formula part) {
    if (part instanceof Or) _parts.addAll( ((Or)part)._parts );
    else _parts.add(part);
  }

  public Or(ArrayList<Formula> parts) {
    super(parts);
    _parts = new ArrayList<Formula>();
    for (int i = 0; i < parts.size(); i++) addPart(parts.get(i));
  }

  public Or(Formula first, Formula second) {
    super(first, second);
    _parts = new ArrayList<Formula>();
    addPart(first);
    addPart(second);
  }

  public Or(Formula first, Formula second, Formula third) {
    super(first, second, third);
    _parts = new ArrayList<Formula>();
    addPart(first);
    addPart(second);
    addPart(third);
  }

  public Formula negate() {
    ArrayList<Formula> parts = new ArrayList<Formula>();
    for (int i = 0; i < _parts.size(); i++) parts.add(_parts.get(i).negate());
    return new And(parts);
  }
  
  /** @return null because this is not an Atom, unless it has exactly one (atomic) child */
  public Atom queryAtom() {
    if (_parts.size() == 1) return _parts.get(0).queryAtom();
    return null;
  }

  /** Instantiates the current disjunction with an assignment. */
  public Formula substitute(Substitution subst) {
    ArrayList<Formula> parts = new ArrayList<Formula>();
    for (int i = 0; i < _parts.size(); i++) parts.add(_parts.get(i).substitute(subst));
    return new Or(parts);
  }

  /**
   * Adds clauses corresponding to the current disjunction.
   * This may result in the creation of additional fresh variables, which are set to be equivalent
   * to parts of the OR.
   */
  public void addClauses(ClauseCollection col) {
    ArrayList<Atom> clauseparts = new ArrayList<Atom>();
    for (int i = 0; i < _parts.size(); i++) {
      clauseparts.add(queryAtomFor(_parts.get(i), col));
    }
    col.addClause(new Clause(clauseparts));
  }

  /**
   * Adds clauses corresponding to ¬a ∨ this.
   * (Since that is still a disjunction, the result is very similar to addClauses, just with an
   * additional atom).
   */
  public void addClausesIfThisIsImpliedBy(Atom a, ClauseCollection col) {
    ArrayList<Atom> clauseparts = new ArrayList<Atom>();
    for (int i = 0; i < _parts.size(); i++) {
      clauseparts.add(queryAtomFor(_parts.get(i), col));
    }
    clauseparts.add(a.negate());
    col.addClause(new Clause(clauseparts));
  }

  /**
   * Adds clauses corresponding to this → a, so parts[i] → a for all i
   */
  public void addClausesIfThisImplies(Atom a, ClauseCollection col) {
    for (int i = 0; i < _parts.size(); i++) {
      _parts.get(i).addClausesIfThisImplies(a, col);
    }
  }

  /**
   * Adds clauses corresponding to this <-> a, so essentially the combination of
   * addClausesIfThisIsImpliedBy and addClausesIfThisImplies (but with a slight optimisation).
   */
  public void addClausesDef(Atom a, ClauseCollection col) {
    ArrayList<Atom> clauseparts = new ArrayList<Atom>();
    for (int i = 0; i < _parts.size(); i++) {
      Atom x = queryAtomFor(_parts.get(i), col);
      col.addClause(new Clause(x.negate(), a));     // parts[i] → x
      clauseparts.add(queryAtomFor(_parts.get(i), col));
    }
    clauseparts.add(a.negate());
    col.addClause(new Clause(clauseparts));
  }

  public int queryAssocLevel() {
    return Formula.JUNCTION;
  }

  public String toString() {
    if (_parts.size() == 0) return "⊥";
    String ret = "";
    for (int i = 0; i < _parts.size(); i++) {
      if (i > 0) ret += " ∨ ";
      if (_parts.get(i).queryAssocLevel() < queryAssocLevel()) ret += _parts.get(i).toString();
      else ret += "(" + _parts.get(i).toString() + ")";
    }
    return ret;
  }
}
