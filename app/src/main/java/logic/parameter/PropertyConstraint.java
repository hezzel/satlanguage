package logic.parameter;

import java.util.Set;
import java.util.ArrayList;

/**
 * A PropertyConstraint is the application of a (user-defined) property to a tuple of PExpressions.
 */
public class PropertyConstraint implements PConstraint {
  private Property _prop;
  private ArrayList<PExpression> _targets;
  private boolean _negated;

  public PropertyConstraint(Property p, ArrayList<PExpression> applyOn, boolean value) {
    _prop = p;
    _targets = new ArrayList<PExpression>(applyOn);
    _negated = !value;
  }

  public PropertyConstraint(Property p, PExpression applyOn) {
    _prop = p;
    _targets = new ArrayList<PExpression>();
    _targets.add(applyOn);
    _negated = false;
  }

  public int queryKind() {
    return PConstraint.RELATION;
  }

  public PExpression queryLeft() {
    return null;
  }

  public PExpression queryRight() {
      return null;
  }

  public PropertyConstraint negate() {
    return new PropertyConstraint(_prop, _targets, _negated);
  }

  public boolean evaluate(Assignment assignment) {
    ArrayList<Integer> parts = new ArrayList<Integer>();
    for (int i = 0; i < _targets.size(); i++) {
      parts.add(_targets.get(i).evaluate(assignment));
    }
    if (_negated) return !_prop.lookup(parts);
    return _prop.lookup(parts);
  }

  public PConstraint substitute(Substitution substitution) {
    ArrayList<PExpression> parts = new ArrayList<PExpression>();
    for (int i = 0; i < _targets.size(); i++) {
      parts.add(_targets.get(i).substitute(substitution));
    }
    return new PropertyConstraint(_prop, parts, !_negated);
  }

  public boolean isTop() {
    return false;
  }

  public Set<String> queryParameters() {
    Set<String> ret = _targets.get(0).queryParameters();
    for (int i = 1; i < _targets.size(); i++) {
      ret.addAll(_targets.get(i).queryParameters());
    }
    return ret;
  }

  public String toString() {
    String ret = _prop.queryName() + "(";
    for (int i = 0; i < _targets.size(); i++) {
      if (i > 0) ret += ",";
      ret += _targets.get(i).toString();
    }
    if (_negated) ret = "¬" + ret;
    return ret + ")";
  }
}

