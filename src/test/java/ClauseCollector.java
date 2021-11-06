import logic.sat.*;
import java.util.ArrayList;
import java.util.TreeSet;

/** A helper class for the various tests that use a clause collection. */
class ClauseCollector extends ClauseCollection {
  TreeSet<String> _solution;

  public ClauseCollector() { _solution = new TreeSet<String>(); }

  int size() { return _clauses.size(); }
  
  Clause get(int i) { return _clauses.get(i); }
  
  boolean contains(String desc) {
    for (int i = 0; i < size(); i++) {
      if (_clauses.get(i).toString().equals(desc)) return true;
    }   
    return false;
  }

  /**
   * This forces the variable of the given name to hold, and hence removes all clauses that contain
   * it positively, while removing the negation of this variable from the remaining clauses.
   */
  void force(String v, boolean value) {
    Atom v1 = new Atom(new Variable(v), value);
    Atom v0 = new Atom(new Variable(v), !value);
    for (int i = 0; i < size(); i++) {
      ArrayList<Atom> parts = _clauses.get(i).getParts();
      boolean changed = false;
      for (int j = 0; j < parts.size(); j++) {
        if (parts.get(j).equals(v1)) {
          _clauses.remove(i);
          i--;
          break;
        }
        if (parts.get(j).equals(v0)) {
          parts.remove(j);
          changed = true;
          j--;
        }
      }
      if (changed) _clauses.set(i, new Clause(parts));
    }
  }

  /**
   * This repeatedly removes clauses with only a single atom, storing them in the _solution
   * variable, and forcing that atom to true in the rest of the clauses (thus removing all
   * clauses that contain it, and removing its negation from the rest).  If either no clauses
   * remain, or an empty clause (⊥) is found, then true is returned; otherwise false.
   * If an empty clause is found, then _solution is set to null since the problem is not solvable.
   */
  boolean unitPropagate() {
    boolean didSomething = true;
    while (didSomething) {
      didSomething = false;
      for (int i = 0; i < size() && !didSomething; i++) {
        ArrayList<Atom> parts = _clauses.get(i).getParts();
        if (parts.size() == 0) {
          _solution = null;
          return true;
        }
        if (parts.size() == 1) {
          Atom p = parts.get(0);
          _solution.add(p.toString());
          _clauses.remove(i);
          Variable x = p.queryVariable();
          if (p.queryNegative()) force(x.toString(), false);
          else force(x.toString(), true);
          didSomething = true;
        }
      }
    }
    return size() == 0;
  }
}

