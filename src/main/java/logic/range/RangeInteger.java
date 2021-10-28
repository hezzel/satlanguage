package logic.range;

import logic.sat.Atom;
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

  /**
   * This function returns a RangeInteger y with the following properties:
   * - if x ≤ newmin then y ≤ newmin
   * - if y ≥ newmax then y ≥ newmax
   * - else y = x
   * This new RangeInteger may be equal to x, or completely different outside the given range.
   * This is intended to be used to avoid creating more boolean variables and well-definedness
   * clauses than necessary.
   */
  public RangeInteger setPracticalBounds(int newmin, int newmax);

  /** an atom representing x ≥ i */
  public Atom queryGeqAtom(int i);

  /**
   * Adds clauses to the collection that guarantee that the variables defining this integer do
   * indeed define an integer in the intended way.  These clauses are guaranteed to always be
   * satisfiable (on their own).
   */
  public void addWelldefinednessClauses(ClauseCollection col);
}

