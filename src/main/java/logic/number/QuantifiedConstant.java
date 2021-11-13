package logic.number;

import logic.sat.Atom;
import logic.parameter.PExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.number.ConstantInteger;
import java.util.Set;

/**
 * A quantified constant is a wrapper class for a PExpression, which can be evaluated to an
 * integer constant.
 */
public class QuantifiedConstant implements QuantifiedInteger {
  private PExpression _constant;
  private Atom _truth;

  public QuantifiedConstant(PExpression expression, Atom truth) {
    _constant = expression;
    _truth = truth;
  }

  public Set<String> queryParameters() {
    return _constant.queryParameters();
  }

  public boolean queryClosed() {
    return queryParameters().size() == 0;
  }

  public QuantifiedConstant substitute(Substitution subst) {
    return new QuantifiedConstant(_constant.substitute(subst), _truth);
  }

  public int queryKind() {
    return ClosedInteger.BOTH;
  }

  public ConstantInteger instantiate(Assignment ass) {
    return new ConstantInteger(_constant.evaluate(ass), _truth);
  }

  public String toString() {
    return _constant.toString();
  }
}

