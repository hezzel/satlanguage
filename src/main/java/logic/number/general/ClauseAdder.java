package logic.number.general;

import logic.sat.ClauseCollection;

/**
 * A ClauseAdder is just a wrapper for a function that can be used to add some clauses to a given
 * Clause Collection.
 * This is particularly intended to avoid a dependency between packages when formulas are used
 * inside integers and vice versa.
 */
public interface ClauseAdder {
  public void add(ClauseCollection col);
}

