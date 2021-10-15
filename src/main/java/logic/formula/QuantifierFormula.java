package logic.formula;

import logic.parameter.Parameter;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.parameter.PExpression;
import logic.parameter.PConstraint;
import java.util.ArrayList;

/**
 * A QuantifierFormula is a formula of the form ?? i ∈ {lower..upper} with cond.formula, where ??
 * is a quantifier (e.g., ∀, ∃).  A quantifier formula is syntactic sugar for the formula which
 * iterates over all the parameters.
 */
public abstract class QuantifierFormula extends SugarFormula {
  protected Parameter _param;
  protected Formula _formula;

  /** Creates the formula ?? param.formula. */
  public QuantifierFormula(Parameter param, Formula formula) {
    super(formula);
    _usedParameters.addAll(param.queryRestriction().queryParameters());
    _usedParameters.remove(param.queryName());
    _usedParameters.addAll(param.queryMinimum().queryParameters());
    _usedParameters.addAll(param.queryMaximum().queryParameters());
    _param = param;
    _formula = formula;
  }

  /** This should implement a call to the constructor for each particular instance. */
  protected abstract QuantifierFormula create(Parameter param, Formula formula);

  /** This should return the string representation of just the quantifier, e.g., "∀". */
  protected abstract String queryQuantifierName();

  /**
   * This method yields all the formulas that are implicitly used in the quantification, to be
   * used for the translate() function.
   * This can only be used if the formula is closed, so no parameters are used in it.
   */
  protected ArrayList<Formula> enumerateParts() {
    if (_usedParameters.size() != 0) {
      throw new Error("Cannot enumerate parts of a bounded quantifier if it is not closed: " +
        toString());
    }
    ArrayList<Formula> parts = new ArrayList<Formula>();
    int min = _param.queryMinimum().evaluate(null);
    int max = _param.queryMaximum().evaluate(null);
    for (int i = min; i <= max; i++) {
      Assignment ass = new Assignment(_param.queryName(), i);
      if (_param.queryRestriction().evaluate(ass)) {
        parts.add(_formula.instantiate(ass));
      }
    }
    return parts;
  }

  /**
   * This method instantiates variables free in this formula, so not bound by the current
   * quantifier.
   */
  public Formula substitute(Substitution subst) {
    Formula instform;
    PConstraint constr;
    
    PExpression min = _param.queryMinimum().substitute(subst);
    PExpression max = _param.queryMaximum().substitute(subst);

    PExpression tmp = subst.get(_param.queryName());
    if (tmp != null) {
      subst.remove(_param.queryName());
      instform = _formula.substitute(subst);
      constr = _param.queryRestriction().substitute(subst);
      subst.put(_param.queryName(), tmp);
    }
    else {
      instform = _formula.substitute(subst);
      constr = _param.queryRestriction().substitute(subst);
    }

    Parameter p = new Parameter(_param.queryName(), min, max, constr);
    return create(p, instform);
  }

  /** Returns the assoc level for printing. */
  public int queryAssocLevel() {
    return Formula.OTHER;
  }

  /** Prints the quantifier formula, using the quantifier name. */
  public String toString() {
    return queryQuantifierName() + " " + _param.toString() + ". " + _formula.toString();
  }
}

