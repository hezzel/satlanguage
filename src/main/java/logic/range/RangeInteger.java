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
   * For the integer to be well-defined, it might be necessary for some clauses on the corresponding
   * variables to be satisfied. This method adds these clauses to the ClauseCollector.
   */
  public void addWelldefinednessClauses(ClauseCollection col);
}

