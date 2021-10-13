package logic.parameter;

import java.util.TreeSet;

public class ConstantExpression implements PExpression {
  private int _value;

  public ConstantExpression(int value) {
    _value = value;
  }

  public int queryKind() {
    return PExpression.CONSTANT;
  }

  public PExpression queryLeft() {
    return null;
  }

  public PExpression queryRight() {
    return null;
  }

  public int evaluate(Assignment assignment) {
    return _value;
  }

  public ConstantExpression substitute(Assignment assignment) {
    return this;
  }

  public ConstantExpression substitute(Substitution substitution) {
    return this;
  }

  public boolean queryConstant() {
    return true;
  }

  public PExpression add(int num) {
    return new ConstantExpression(_value + num);
  }

  public TreeSet<String> queryParameters() {
    return new TreeSet<String>();
  }

  public String toString() {
    return "" + _value;
  }

  public boolean equals(PExpression expr) {
    if (!expr.queryConstant()) return false;
    return expr.evaluate(null) == _value;
  }
}
