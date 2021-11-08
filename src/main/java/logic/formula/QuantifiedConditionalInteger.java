package logic.formula;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.ClauseCollection;
import logic.parameter.PExpression;
import logic.parameter.PConstraint;
import logic.parameter.ConstantExpression;
import logic.parameter.Parameter;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.number.general.ClauseAdder;
import logic.number.ClosedInteger;
import logic.number.ConditionalInteger;
import logic.number.QuantifiedInteger;
import java.util.Set;
import java.util.ArrayList;

/**
 * A QuantifiedConditionalInteger represents a conditional integer formula ? value : 0, where
 * the formula and value might contain parameters.
 */
public class QuantifiedConditionalInteger implements QuantifiedInteger {
  private Formula _condition;
  private QuantifiedInteger _value;
  private Atom _conditionAtom;
  private Atom _truth;

  public QuantifiedConditionalInteger(Formula formula, QuantifiedInteger value, Atom truth) {
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

  public QuantifiedConditionalInteger substitute(Substitution subst) {
    Formula formula = _condition.substitute(subst);
    QuantifiedInteger value = _value.substitute(subst);
    return new QuantifiedConditionalInteger(formula, value, _truth);
  }

  public ClosedInteger instantiate(Assignment ass) {
    Formula cond = _condition.instantiate(ass);
    ClosedInteger val = _value.instantiate(ass);
    if (!cond.queryClosed()) {
      throw new Error("Quantified conditional " + toString() + " is not closed when " +
        "instantiated with " + ass);
    }
    _conditionAtom = cond.queryAtom();
    if (_conditionAtom != null) return new ConditionalInteger(_conditionAtom, val, _truth, null);
    _conditionAtom = new Atom(new Variable("⟦" + cond.toString() + "⟧"), true);
    ClauseAdder adder = new ClauseAdder() {
      public void add(ClauseCollection col) {
        if (col.isInMemory(cond.toString())) return;
        col.addToMemory(_condition.toString());
        cond.addClausesDef(_conditionAtom, col);
      }
    };
    return new ConditionalInteger(_conditionAtom, val, _truth, adder);
  }

  public String toString() {
    if ((_condition instanceof QuantifiedAtom) || _condition instanceof AtomicFormula) {
      return _condition.toString() + " ? " + _value.toString();
    }
    else return "(" + _condition.toString() + ") ? " + _value.toString();
  }
}

