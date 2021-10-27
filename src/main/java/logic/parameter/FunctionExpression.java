package logic.parameter;

import java.util.Set;

/**
 * A FunctionExpression is the application of a (user-defined) function/mapping to a PExpression.
 */
public class FunctionExpression implements PExpression {
  private Function _func;
  private PExpression _target;

  public FunctionExpression(Function f, PExpression applyOn) {
    _func = f;
    _target = applyOn;
  }

  public int queryKind() {
    return PExpression.FUNCTION;
  }

  public PExpression queryLeft() {
    return null;
  }

  public PExpression queryRight() {
      return _target;
  }

  public int evaluate(Assignment assignment) {
    return _func.lookup(_target.evaluate(assignment));
  }

  public PExpression substitute(Substitution substitution) {
    return new FunctionExpression(_func, _target.substitute(substitution));
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
    return _target.queryParameters();
  }

  public String toString() {
    return _func.toString(_target);
  }

  public boolean equals(PExpression expr) {
    if (expr.queryKind() != PExpression.FUNCTION) return false;
    FunctionExpression e = (FunctionExpression)expr;
    if (e._func != _func) return false;
    return _target.equals(e._target);
  }
}

