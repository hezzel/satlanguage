package language.execution;

import logic.sat.Variable;
import logic.parameter.*;

import java.util.ArrayList;
import java.util.TreeSet;

public class ParamBoolVarConstraint implements PConstraint {
  private ParamBoolVar _var;
  private ArrayList<PExpression> _arguments;
  private boolean _negated;

  public ParamBoolVarConstraint(ParamBoolVar x, ArrayList<PExpression> args, boolean value) {
    _var = x;
    _arguments = new ArrayList<PExpression>(args);
    _negated = !value;
    if (_arguments.size() != x.queryParameters().size()) {
      throw new Error("ParamBoolVarConstraint constraint created with " + _arguments.size() +
        " arguments given; " + x.queryParameters().size() + " expected.");
    }
  }

  public PConstraint substitute(Substitution subst) {
    ArrayList<PExpression> args = new ArrayList<PExpression>(_arguments);
    for (int i = 0; i < args.size(); i++) args.set(i, args.get(i).substitute(subst));
    return new ParamBoolVarConstraint(_var, args, !_negated);
  }

  public boolean evaluate(Assignment ass) {
    Assignment indexes = new Assignment();
    ParameterList params = _var.queryParameters();
    for (int i = 0; i < params.size(); i++) {
      indexes.put(params.get(i).queryName(), _arguments.get(i).evaluate(ass));
    }
    Variable v = _var.queryVar(indexes);
    if (ass instanceof ProofState) return ((ProofState)ass).queryValue(v);
    throw new Error("Evaluating ParamBoolVarConstraint with Assignment that is not a ProofState.");
  }

  public PConstraint negate() {
    return new ParamBoolVarConstraint(_var, _arguments, _negated);
  }

  public boolean isTop() {
    return false;
  }

  public int queryKind() {
    return PConstraint.OTHER;
  }

  public String toString() {
    if (_negated) return "¬" + _var.toString(_arguments);
    else return _var.toString(_arguments);
  }

  public TreeSet<String> queryParameters() {
    TreeSet<String> ret = new TreeSet<String>();
    for (int i = 0; i < _arguments.size(); i++) {
      ret.addAll(_arguments.get(i).queryParameters());
    }
    return ret;
  }
}

