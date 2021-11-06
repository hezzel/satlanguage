package logic.number;

import logic.number.range.RangeInteger;
import logic.number.binary.BinaryInteger;

/**
 * A closed integer is an integer expression that may contain variables (either range or binary
 * variables) but does not contain unbound parameters (they may be bound by a sum quantifier).
 */
public interface ClosedInteger {
  static final int RANGE  = 0;
  static final int BINARY = 1;
  static final int BOTH   = 2;

  /** the bottom value for the range (inclusive) */
  public int queryMinimum();

  /** the top value for the range (inclusive) */
  public int queryMaximum();

  /** returns the kind of integer: range, binary or both */
  public int queryKind();

  /** if the kind is RANGE or BOTH, this returns the corresponding range integer; otherwise null */
  public RangeInteger getRange();

  /** if the kind is BINARY or BOTH, this returns a binary integer; otherwise null */
  public BinaryInteger getBinary();
}

