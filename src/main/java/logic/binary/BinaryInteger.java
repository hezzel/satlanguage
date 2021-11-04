package logic.binary;

import logic.sat.Atom;
import logic.sat.ClauseCollection;

/**
 * A binary integer represents an integer value that is in a certain range, with different
 * variables representing its binary representation.
 */
public interface BinaryInteger {
  /** the bottom value that this integer may be (inclusive) */
  public int queryMinimum();

  /** the top value that this integer may be (inclusive) */
  public int queryMaximum();

  /** the number of bits used to represent this integer (not including the negation bit) */
  public int length();

  /**
   * This function returns a BinaryInteger y with the following properties:
   * - if x ≤ newmin then y ≤ newmin
   * - if y ≥ newmax then y ≥ newmax
   * - else y = x
   * This new BinaryInteger may be equal to x, or completely different outside the given range.
   * This is intended to be used to avoid creating more boolean variables and well-definedness
   * clauses than necessary.
   */
  public BinaryInteger setPracticalBounds(int newmin, int newmax);

  /**
   * An atom representing the ith bit, where queryBit(0) represents 1, queryBit(2) represents
   * 4, and so on.
   */
  public Atom queryBit(int i);

  /**
   * An atom representing the special bit indicating that this is a negative number.  Note that
   * we use 2's complement, so for negative numbers, queryBit(i) = 1 for all i ≥ length().
   */
  public Atom queryNegativeBit();

  /**
   * Adds clauses to the collection that guarantee that the variables defining this integer do
   * indeed define an integer in the intended way.  These clauses are guaranteed to always be
   * satisfiable (on their own).
   */
  public void addWelldefinednessClauses(ClauseCollection col);
}

