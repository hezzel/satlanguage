package logic.number;

import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.number.range.RangeInteger;
import java.util.TreeSet;

/**
 * A quantified range wrapper is just a wrapper class casting a RangeInteger to a quantified
 * RangeInteger.
 */
public class QuantifiedRangeWrapper implements QuantifiedRangeInteger {
  private RangeInteger _wrapped;

  public QuantifiedRangeWrapper(RangeInteger ri) {
    _wrapped = ri;
  }

  public TreeSet<String> queryParameters() {
    return new TreeSet<String>();
  }

  public boolean queryClosed() {
    return true;
  }

  public QuantifiedRangeInteger substitute(Substitution subst) {
    return this;
  }

  public RangeInteger instantiate(Assignment ass) {
    return _wrapped;
  }

  public String toString() {
    return _wrapped.toString();
  }
}

