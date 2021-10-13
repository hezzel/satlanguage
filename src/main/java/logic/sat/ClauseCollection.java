package logic.sat;

import java.util.ArrayList;

/** A ClauseCollection is simply a series of clauses, which can be added to. */
public class ClauseCollection {
  protected ArrayList<Clause> _clauses;

  public ClauseCollection() {
    _clauses = new ArrayList<Clause>();
  }

  /** Adds a clause to the collection. */
  public void addClause(Clause clause) {
    _clauses.add(clause);
  }

  /** Prints the current collection to a string in a human-readable way. */
  public String toString() {
    String ret = ""; 
    for (int i = 0; i < _clauses.size(); i++) {
      ret += _clauses.get(i).toString() + "\n";
    }   
    return ret;
  }
}

