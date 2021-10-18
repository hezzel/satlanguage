package logic.range;

import logic.sat.Variable;
import logic.sat.ClauseCollection;

public class ConstantInteger implements RangeInteger {
  private int _value;
  private Variable _truevar;
  private Variable _falsevar;

  public ConstantInteger(int value, Variable truevar, Variable falsevar) {
    _value = value;
    _truevar = truevar;
    _falsevar = falsevar;
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
}

