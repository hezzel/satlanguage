package logic.number;

import logic.sat.Atom;
import logic.parameter.PExpression;
import logic.parameter.PConstraint;
import logic.parameter.ConstantExpression;
import logic.parameter.Parameter;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import java.util.Set;
import java.util.ArrayList;

/**
 * A QuantifiedSum represents a conditional sum Σ{ <expr> | parameters in ranges } which is
 * essentially a big plus, but easier for the user to write down.
 */
public class QuantifiedSum implements QuantifiedInteger {
  private ArrayList<Parameter> _params;
  private QuantifiedInteger _expression;
  private Atom _truth;
  private int _kind;

  public QuantifiedSum(ArrayList<Parameter> params, QuantifiedInteger expr, int kind, Atom truth) {
    _params = new ArrayList<Parameter>(params);
    _expression = expr;
    _truth = truth;
    _kind = kind;
  }

  public QuantifiedSum(Parameter param, QuantifiedInteger expr, int kind, Atom truth) {
    _params = new ArrayList<Parameter>();
    _params.add(param);
    _expression = expr;
    _truth = truth;
    _kind = kind;
  }

  public Set<String> queryParameters() {
    Set<String> ret = _expression.queryParameters();
    for (int i = _params.size()-1; i >= 0; i--) {
      ret.addAll(_params.get(i).queryRestriction().queryParameters());
      ret.remove(_params.get(i).queryName());
      ret.addAll(_params.get(i).queryMinimum().queryParameters());
      ret.addAll(_params.get(i).queryMaximum().queryParameters());
    }
    return ret;
  }

  public boolean queryClosed() {
    return queryParameters().size() == 0;
  }

  public QuantifiedSum substitute(Substitution subst) {
    Substitution gamma = new Substitution(subst);
    ArrayList<Parameter> newparams = new ArrayList<Parameter>();
    for (int i = 0; i < _params.size(); i++) {
      PExpression min = _params.get(i).queryMinimum().substitute(gamma);
      PExpression max = _params.get(i).queryMaximum().substitute(gamma);
      gamma.remove(_params.get(i).queryName());
      PConstraint phi = _params.get(i).queryRestriction().substitute(gamma);
      newparams.add(new Parameter(_params.get(i).queryName(), min, max, phi));
    }
    QuantifiedInteger expr = _expression.substitute(gamma);
    return new QuantifiedSum(newparams, expr, _kind, _truth);
  }

  /** Public for the sake of unit-testing. */
  public void addComponents(int paramindex, Assignment ass, ArrayList<ClosedInteger> sofar) {
    if (paramindex >= _params.size()) {
      sofar.add(_expression.instantiate(ass));
      return;
    }
    Parameter p = _params.get(paramindex);
    int min = p.queryMinimum().evaluate(ass);
    int max = p.queryMaximum().evaluate(ass);
    Integer backup = null;
    if (ass.defines(p.queryName())) backup = ass.get(p.queryName());
    for (int i = min; i <= max; i++) {
      ass.put(p.queryName(), i);
      if (p.queryRestriction().evaluate(ass)) addComponents(paramindex+1, ass, sofar);
    }
    if (backup == null) ass.remove(p.queryName());
    else ass.put(p.queryName(), backup);
  }

  /** Returns parts[start] +...+ parts[end] by splitting the parts evenly and using PlusInteger. */
  private ClosedInteger split(ArrayList<ClosedInteger> parts, int start, int end) {
    if (start == end) return parts.get(start);
    int middle = (start + end) / 2;
    ClosedInteger part1 = split(parts, start, middle);
    ClosedInteger part2 = split(parts, middle + 1, end);
    return new PlusInteger(part1, part2, _kind, _truth);
  }

  public ClosedInteger instantiate(Assignment ass) {
    ArrayList<ClosedInteger> parts = new ArrayList<ClosedInteger>();
    if (ass == null) ass = new Assignment();
    addComponents(0, ass, parts);
    if (parts.size() == 0) return new ConstantInteger(0, _truth);
    return split(parts, 0, parts.size()-1);
  }

  public String toString() {
    String ret = "Σ { " + _expression.toString() + " | ";
    for (int i = 0; i < _params.size(); i++) {
      if (i > 0) ret += ", ";
      ret += _params.get(i).toString();
    }
    return ret + " }";
  }
}

