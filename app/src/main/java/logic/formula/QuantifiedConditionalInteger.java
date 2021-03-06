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
import logic.number.*;
import java.util.Set;
import java.util.ArrayList;

/**
 * A QuantifiedConditionalInteger represents a conditional integer formula ? value : 0, where
 * the formula and value might contain parameters.
 */
public class QuantifiedConditionalInteger implements QuantifiedInteger {
  private Formula _condition;
  private QuantifiedInteger _value;
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

  public int queryKind() {
    return _value.queryKind();
  }

  public ClosedInteger instantiate(Assignment ass) {
    // variables used in a function pointer must be effectively final, so we set them here and
    // don't change them afterwards
    Formula cond = ass == null ? _condition : _condition.instantiate(ass);
    Atom conditionAtom = cond.queryAtom() != null ? null :
                            new Atom(new Variable("⟦" + cond.toString() + "⟧"), true);

    ClosedInteger val = _value.instantiate(ass);
    if (!cond.queryClosed()) {
      throw new Error("Quantified conditional " + toString() + " is not closed when " +
        "instantiated with " + ass);
    }
    if (conditionAtom == null) return new ConditionalInteger(cond.queryAtom(), val, _truth, null);
    ClauseAdder adder = new ClauseAdder() {
      public void add(ClauseCollection col) {
        if (col.isInMemory(cond.toString())) return;
        col.addToMemory(_condition.toString());
        cond.addClausesDef(conditionAtom, col);
      }
    };
    return new ConditionalInteger(conditionAtom, val, _truth, adder);
  }

  public String toString() {
    String cond, val;

    if ((_condition instanceof QuantifiedAtom) || _condition instanceof AtomicFormula) {
      cond = _condition.toString();
    }
    else cond = "(" + _condition.toString() + ")";

    if ((_value instanceof QuantifiedConstant) || (_value instanceof VariableInteger) ||
        (_value instanceof QuantifiedVariable)) val = _value.toString();
    else val = "(" + _value + ")";

    return cond + " ? " + val;
  }
}

