package logic.number;

import logic.sat.Atom;
import logic.parameter.PExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.number.range.RangeConstant;
import java.util.Set;

/**
 * A quantified range constant is a wrapper class for a PExpression, which can be evaluated to an
 * integer constant.
 */
public class QuantifiedRangeConstant implements QuantifiedRangeInteger {
  private PExpression _constant;
  private Atom _truth;

  public QuantifiedRangeConstant(PExpression expression, Atom truth) {
    _constant = expression;
    _truth = truth;
  }

  public Set<String> queryParameters() {
    return _constant.queryParameters();
  }

  public boolean queryClosed() {
    return queryParameters().size() == 0;
  }

  public QuantifiedRangeConstant substitute(Substitution subst) {
    return new QuantifiedRangeConstant(_constant.substitute(subst), _truth);
  }

  public RangeConstant instantiate(Assignment ass) {
    return new RangeConstant(_constant.evaluate(ass), _truth);
  }

  public String toString() {
    return _constant.toString();
  }
}

