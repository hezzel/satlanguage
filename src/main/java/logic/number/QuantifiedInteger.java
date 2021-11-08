package logic.number;

import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.number.ClosedInteger;
import java.util.Set;

/**
 * A quantified integer is an expression containing parameters which evaluates to a RangeInteger
 * when fully instantiated.
 */
public interface QuantifiedInteger {
  /** Returns the set of parameters for this quantified integer. */
  public Set<String> queryParameters();

  /** Returns true if and only if queryParameters() is empty. */
  public boolean queryClosed();

  /** Replaces parameters by their associated expression. */
  public QuantifiedInteger substitute(Substitution subst);

  /**
   * Replaces all parameters in the quantified integer by their value, and returns the
   * corresponding closed integer.  An Error is thrown if some parameter is missing.  If the
   * quantified integer is closed, then it is allowed to use null in place of the assignment.
   */
  public ClosedInteger instantiate(Assignment ass);
}

