package logic.formula;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.parameter.PExpression;
import logic.parameter.PConstraint;
import logic.parameter.ConstantExpression;
import logic.parameter.Parameter;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.number.range.RangeInteger;
import logic.number.QuantifiedRangeInteger;
import java.util.Set;
import java.util.ArrayList;

/**
 * A QuantifiedConditionalRangeInteger represents a conditional integer formula ? value : 0, where
 * the formula and value might contain parameters.
 */
public class QuantifiedConditionalRangeInteger implements QuantifiedRangeInteger {
  private Formula _condition;
  private QuantifiedRangeInteger _value;
  private Atom _truth;

  public QuantifiedConditionalRangeInteger(Formula formula, QuantifiedRangeInteger value,
                                           Atom truth) {
    _condition = formula;
    _value = value;
    _truth = truth;
  }

  public Set<String> queryParameters() {
    Set<String> ret = _condition.queryParameters();
    ret.addAll(_value.queryParameters());
    return ret;
  }

  public boolean queryClosed() {
    return queryParameters().size() == 0;
  }

  public QuantifiedConditionalRangeInteger substitute(Substitution subst) {
    Formula formula = _condition.substitute(subst);
    QuantifiedRangeInteger value = _value.substitute(subst);
    return new QuantifiedConditionalRangeInteger(formula, value, _truth);
  }

  public RangeInteger instantiate(Assignment ass) {
    Formula cond = _condition.instantiate(ass);
    RangeInteger val = _value.instantiate(ass);
    if (!cond.queryClosed()) {
      throw new Error("Range integer " + toString() + " is not closed when instantiated with " +
        ass);
    }
    return new ConditionalRangeInteger(cond, val, _truth);
  }

  public String toString() {
    if ((_condition instanceof QuantifiedAtom) || _condition instanceof AtomicFormula) {
      return _condition.toString() + " ? " + _value.toString();
    }
    else return "(" + _condition.toString() + ") ? " + _value.toString();
  }
}

