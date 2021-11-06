package logic.number;

import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.number.range.RangeInteger;
import java.util.Set;

/**
 * A quantified range integer is an expression containing parameters which evaluates to a
 * RangeInteger when fully instantiated.
 */
public interface QuantifiedRangeInteger {
  /** Returns the set of parameters for this quantified range integer. */
  public Set<String> queryParameters();

  /** Returns true if and only if queryParameters() is empty. */
  public boolean queryClosed();

  /** Replaces parameters by their associated expression. */
  public QuantifiedRangeInteger substitute(Substitution subst);

  /**
   * Replaces all parameters in the quantified integer by their value, and returns the
   * corresponding range integer.  An Error is thrown if some parameter is missing.  If the
   * quantified integer is closed, then it is allowed to use null in place of the assignment.
   */
  public RangeInteger instantiate(Assignment ass);
}

