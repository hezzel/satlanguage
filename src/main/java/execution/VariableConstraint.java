package language.execution;

import logic.sat.Variable;
import logic.parameter.PConstraint;
import logic.parameter.Assignment;
import logic.parameter.Substitution;

import java.util.TreeSet;

public class VariableConstraint implements PConstraint {
  private Variable _var;
  private boolean _negated;

  public VariableConstraint(Variable x, boolean value) {
    _var = x;
    _negated = !value;
  }

  public PConstraint substitute(Substitution subst) {
    return this;
  }

  public boolean evaluate(Assignment ass) {
    if (ass instanceof ProofState) return ((ProofState)ass).queryValue(_var);
    throw new Error("Evaluating VariableConstraint with an Assignment that is not a ProofState.");
  }

  public PConstraint negate() {
    return new VariableConstraint(_var, _negated);
  }

  public boolean isTop() {
    return false;
  }

  public int queryKind() {
    return PConstraint.OTHER;
  }

  public String toString() {
    if (_negated) return "¬" + _var.toString();
    else return _var.toString();
  }

  public TreeSet<String> queryParameters() {
    return new TreeSet<String>();
  }
}
