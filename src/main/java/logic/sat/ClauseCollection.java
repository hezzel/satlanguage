package logic.sat;

import java.util.ArrayList;
import java.util.TreeSet;

/** A ClauseCollection is simply a series of clauses, which can be added to. */
public class ClauseCollection {
  protected ArrayList<Clause> _clauses;
  private TreeSet<String> _memory;

  public ClauseCollection() {
    _clauses = new ArrayList<Clause>();
    _memory = new TreeSet<String>();
  }

  /** Adds a clause to the collection. */
  public void addClause(Clause clause) {
    _clauses.add(clause);
  }

  /**
   * Adds a string to the memory. This could be used to recall that a certain set of clauses has
   * already been added, and does not need to be added again.
   */
  public void addToMemory(String item) {
    _memory.add(item);
  }

  /** Returns whether the given string is in the memory. */
  public boolean isInMemory(String item) {
    return _memory.contains(item);
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

