package logic.parameter;

import java.util.TreeSet;

/** An expression corresonding to the value of a specific parameter. */
public class ParameterExpression implements PExpression {
  private String _param;

  public ParameterExpression(String name) {
    _param = name;
  }

  public int queryKind() {
    return PExpression.PARAMETER;
  }

  public PExpression queryLeft() {
    return null;
  }

  public PExpression queryRight() {
    return null;
  }

  public int evaluate(Assignment assignment) {
    return assignment.get(_param);
  }

  public PExpression substitute(Substitution substitution) {
    PExpression replacement = substitution.get(_param);
    if (replacement == null) return this;
    else return replacement;
  }

  public boolean queryConstant() {
    return false;
  }

  public PExpression add(int number) {
    if (number == 0) return this;
    return new SumExpression(this, new ConstantExpression(number));
  }

  public PExpression multiply(int number) {
    if (number == 0) return new ConstantExpression(0);
    if (number == 1) return this;
    return new ProductExpression(new ConstantExpression(number), this);
  }

  public TreeSet<String> queryParameters() {
    TreeSet<String> ret = new TreeSet<String>();
    ret.add(_param);
    return ret;
  }

  public String toString() {
    return _param;
  }

  public boolean equals(PExpression expr) {
    return expr.toString().equals(_param);
  }
}
