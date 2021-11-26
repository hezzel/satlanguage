package logic.sat;

import java.util.ArrayList;
import java.util.Collections;

/** A clause is a disjunction of atoms.  Clauses are immutable objects. */
public class Clause {
  private ArrayList<Atom> _atoms;

  /** Creates the empty clause, also known as ⊥. */
  public Clause() {
    _atoms = new ArrayList<Atom>();
  }

  /** Creates the clause with a single atom. */
  public Clause(Atom a) {
    _atoms = new ArrayList<Atom>();
    _atoms.add(a);
  }

  /** Creates the clause a \/ b. */
  public Clause(Atom a, Atom b) {
    _atoms = new ArrayList<Atom>();
    _atoms.add(a);
    _atoms.add(b);
    Collections.sort(_atoms);
  }

  /** Creates the clause a \/ b \/ c. */
  public Clause(Atom a, Atom b, Atom c) {
    _atoms = new ArrayList<Atom>();
    _atoms.add(a);
    _atoms.add(b);
    _atoms.add(c);
    Collections.sort(_atoms);
  }

  /** Creates the clause a \/ c. */
  public Clause(Atom a, Clause c) {
    _atoms = new ArrayList<Atom>(c._atoms);
    _atoms.add(a);
    Collections.sort(_atoms);
  }

  /** Creates the clause a \/ b. */
  public Clause(Clause a, Clause b) {
    _atoms = new ArrayList<Atom>(a._atoms);
    _atoms.addAll(b._atoms);
    Collections.sort(_atoms);
  }

  /** Creates a clause with exactly the given atoms. */
  public Clause(ArrayList<Atom> arr) {
    _atoms = new ArrayList<Atom>(arr);
    Collections.sort(_atoms);
  }

  /** Returns the index of the highest variable in the clause. */
  public int getHighestAtomIdentifier() {
    if (_atoms.size() == 0) return 0;
    return _atoms.get(_atoms.size()-1).queryIndex();
  }

  /** Returns the clause as a line in a SAT input file. */
  public String getSatDescription() {
    String ret = "";
    for (int i = 0; i < _atoms.size(); i++) ret += _atoms.get(i).getSatDescription() + " ";
    return ret + "0";
  }

  /** Returns a human-readable description of the clause. */
  public String toString() {
    StringBuilder ret = new StringBuilder();
    for (int i = 0; i < _atoms.size(); i++) {
      if (i != 0) ret.append(" ∨ ");
      ret.append(_atoms.get(i).toString());
    }
    return ret.toString();
  }

  /** Returns the atoms making up this Clause. */
  public ArrayList<Atom> getParts() {
    return new ArrayList<Atom>(_atoms);
  }

  /** Returns whether the given clause returns exactly the same atoms as the other one. */
  public boolean equals(Clause c) {
    if (_atoms.size() != c._atoms.size()) return false;
    for (int i = 0; i < _atoms.size(); i++) {
      if (!_atoms.get(i).equals(c._atoms.get(i))) return false;
    }
    return true;
  }
}

