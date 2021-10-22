package logic.range;

import logic.sat.Variable;
import logic.sat.ClauseCollection;

public class RangeConstant implements RangeInteger {
  private int _value;
  private Variable _falsevar;
  private Variable _truevar;

  public RangeConstant(int value, Variable falsevar, Variable truevar) {
    _value = value;
    _falsevar = falsevar;
    _truevar = truevar;
  }

  public int queryMinimum() {
    return _value;
  }

  public int queryMaximum() {
    return _value;
  }

  public Variable queryGeqVariable(int i) {
    if (_value >= i) return _truevar;
    else return _falsevar;
  }

  public void addWelldefinednessClauses(ClauseCollection col) {
    // nothing to add
  }

  public String toString() {
    return "" + _value;
  }
}

