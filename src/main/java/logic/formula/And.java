package logic.formula;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.sat.ClauseCollection;
import logic.parameter.Substitution;
import java.util.ArrayList;

public class And extends Formula {
  private ArrayList<Formula> _parts;

  /** Helper for the constructor: adds a formula to parts, and flattens And parts. */
  private void addPart(Formula part) {
    if (part instanceof And) _parts.addAll( ((And)part)._parts );
    else _parts.add(part);
  }

  public And(ArrayList<Formula> parts) {
    super(parts);
    _parts = new ArrayList<Formula>();
    for (int i = 0; i < parts.size(); i++) addPart(parts.get(i));
  }

  public And(Formula first, Formula second) {
    super(first, second);
    _parts = new ArrayList<Formula>();
    addPart(first);
    addPart(second);
  }

  public And(Formula first, Formula second, Formula third) {
    super(first, second, third);
    _parts = new ArrayList<Formula>();
    addPart(first);
    addPart(second);
    addPart(third);
  }

  public Formula negate() {
    ArrayList<Formula> parts = new ArrayList<Formula>();
    for (int i = 0; i < _parts.size(); i++) parts.add(_parts.get(i).negate());
    return new Or(parts);
  }

  /** @return null because this is not an AtomicFormula, unless it has only one component */
  public Atom queryAtom() {
    if (_parts.size() == 1) return _parts.get(0).queryAtom();
    return null;
  }

  /** Instantiates the current conjunction with an assignment. */
  public Formula substitute(Substitution subst) {
    ArrayList<Formula> parts = new ArrayList<Formula>();
    for (int i = 0; i < _parts.size(); i++) parts.add(_parts.get(i).substitute(subst));
    return new And(parts);
  }

  /** Adds clauses for each of the parts individually to the collection. */
  public void addClauses(ClauseCollection coll) {
    for (int i = 0; i < _parts.size(); i++) {
      _parts.get(i).addClauses(coll);
    }
  }

  /** Adds clauses for a → part for each part in the conjunction. */
  public void addClausesIfThisIsImpliedBy(Atom a, ClauseCollection col) {
    for (int i = 0; i < _parts.size(); i++) {
      _parts.get(i).addClausesIfThisIsImpliedBy(a, col);
    }
  }

  /** Adds clauses corresponding to (not this) ∨ a, so OR [parts] ∨ a. */
  public void addClausesIfThisImplies(Atom a, ClauseCollection col) {
    ArrayList<Atom> clauseparts = new ArrayList<Atom>();
    for (int i = 0; i < _parts.size(); i++) {
      clauseparts.add(queryAtomFor(_parts.get(i), col).negate());
    }
    clauseparts.add(a);
    col.addClause(new Clause(clauseparts));
  }

  /** 
   * Adds clauses corresponding to this <-> a, so essentially the combination of
   * addClausesIfThisIsImpliedBy and addClausesIfThisImplies (but with a slight optimisation).
   */
  public void addClausesDef(Atom a, ClauseCollection col) {
    ArrayList<Atom> clauseparts = new ArrayList<Atom>();
    Atom aneg = a.negate();
    for (int i = 0; i < _parts.size(); i++) {
      Atom x = queryAtomFor(_parts.get(i), col);
      col.addClause(new Clause(aneg, x));     // a → parts[i]
      clauseparts.add(x.negate());
    }
    clauseparts.add(a);
    col.addClause(new Clause(clauseparts));
  }

  public int queryAssocLevel() {
    return Formula.JUNCTION;
  }

  public String toString() {
    if (_parts.size() == 0) return "⊤";
    String ret = ""; 
    for (int i = 0; i < _parts.size(); i++) {
      if (i > 0) ret += " ∧ ";
      if (_parts.get(i).queryAssocLevel() < queryAssocLevel()) ret += _parts.get(i).toString();
      else ret += "(" + _parts.get(i).toString() + ")";
    }   
    return ret;
  }
}

