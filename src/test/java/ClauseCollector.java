import logic.sat.Variable;
import logic.sat.Clause;
import logic.sat.ClauseCollection;

/** A helper class for the various tests that use a clause collection. */
class ClauseCollector extends ClauseCollection {
  int size() { return _clauses.size(); }
  Clause get(int i) { return _clauses.get(i); }
  boolean contains(String desc) {
    for (int i = 0; i < size(); i++) {
      if (_clauses.get(i).toString().equals(desc)) return true;
    }   
    return false;
  }   
}

