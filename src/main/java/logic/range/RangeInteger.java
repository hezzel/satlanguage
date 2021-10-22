package logic.range;

import logic.sat.Variable;
import logic.sat.ClauseCollection;

/**
 * A range integer represents an integer value that is in a certain range, with different variables
 * representing x ≥ i for all i in the range.
 */
public interface RangeInteger {
  /** the bottom value for the range (inclusive) */
  public int queryMinimum();

  /** the top value for the range (inclusive) */
  public int queryMaximum();

  /** a variable representing x ≥ i */
  public Variable queryGeqVariable(int i);

  /**
   * Adds clauses to the collection that guarantee that the variables defining this integer do
   * indeed define an integer in the intended way.  These clauses are guaranteed to always be
   * satisfiable (on their own).
   */
  public void addWelldefinednessClauses(ClauseCollection col);
}

