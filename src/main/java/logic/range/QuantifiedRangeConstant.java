package logic.range;

import logic.sat.Variable;
import logic.parameter.PExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import java.util.Set;

/**
 * A quantified range constant is a wrapper class for a PExpression, which can be evaluated to an
 * integer constant.
 */
public class QuantifiedRangeConstant implements QuantifiedRangeInteger {
  private PExpression _constant;
  private Variable _truevar;

  public QuantifiedRangeConstant(PExpression expression, Variable truevar) {
    _constant = expression;
    _truevar = truevar;
  }

  public Set<String> queryParameters() {
    return _constant.queryParameters();
  }

  public boolean queryClosed() {
    return queryParameters().size() == 0;
  }

  public QuantifiedRangeConstant substitute(Substitution subst) {
    return new QuantifiedRangeConstant(_constant.substitute(subst), _truevar);
  }

  public RangeConstant instantiate(Assignment ass) {
    return new RangeConstant(_constant.evaluate(ass), _truevar);
  }

  public String toString() {
    return _constant.toString();
  }
}

