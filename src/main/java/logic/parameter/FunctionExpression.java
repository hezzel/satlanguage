package logic.parameter;

import java.util.Set;
import java.util.ArrayList;

/**
 * A FunctionExpression is the application of a (user-defined) function/mapping to a PExpression.
 */
public class FunctionExpression implements PExpression {
  private Function _func;
  private ArrayList<PExpression> _targets;

  public FunctionExpression(Function f, ArrayList<PExpression> applyOn) {
    _func = f;
    _targets = new ArrayList<PExpression>(applyOn);
  }

  public FunctionExpression(Function f, PExpression applyOn) {
    _func = f;
    _targets = new ArrayList<PExpression>();
    _targets.add(applyOn);
  }

  public int queryKind() {
    return PExpression.FUNCTION;
  }

  public PExpression queryLeft() {
    return null;
  }

  public PExpression queryRight() {
      return null;
  }

  public int evaluate(Assignment assignment) {
    ArrayList<Integer> parts = new ArrayList<Integer>();
    for (int i = 0; i < _targets.size(); i++) {
      parts.add(_targets.get(i).evaluate(assignment));
    }
    return _func.lookup(parts);
  }

  public PExpression substitute(Substitution substitution) {
    ArrayList<PExpression> parts = new ArrayList<PExpression>();
    for (int i = 0; i < _targets.size(); i++) {
      parts.add(_targets.get(i).substitute(substitution));
    }
    return new FunctionExpression(_func, parts);
  }

  public boolean queryConstant() {
    return false;
  }

  public PExpression add(int num) {
    if (num == 0) return this;
    return new SumExpression(this, new ConstantExpression(num));
  }

  public PExpression multiply(int num) {
    if (num == 0) return new ConstantExpression(0);
    if (num == 1) return this;
    return new ProductExpression(new ConstantExpression(num), this);
  }

  public Set<String> queryParameters() {
    Set<String> ret = _targets.get(0).queryParameters();
    for (int i = 1; i < _targets.size(); i++) {
      ret.addAll(_targets.get(i).queryParameters());
    }
    return ret;
  }

  public String toString() {
    String ret = _func.queryName() + "(";
    for (int i = 0; i < _targets.size(); i++) {
      if (i > 0) ret += ",";
      ret += _targets.get(i).toString();
    }
    return ret + ")";
  }

  public boolean equals(PExpression expr) {
    if (expr.queryKind() != PExpression.FUNCTION) return false;
    FunctionExpression e = (FunctionExpression)expr;
    if (e._func != _func) return false;
    if (e._targets.size() != _targets.size()) return false;
    for (int i = 0; i < _targets.size(); i++) {
      if (!_targets.get(i).equals(e._targets.get(i))) return false;
    }
    return true;
  }
}

