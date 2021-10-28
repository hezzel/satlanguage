package logic.range;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.ClauseCollection;

public class RangeConstant implements RangeInteger {
  private int _value;
  private Atom _trueAtom;

  public RangeConstant(int value, Variable truevar) {
    _value = value;
    _trueAtom = new Atom(truevar, true);
  }

  public RangeConstant(int value, Atom trueatom) {
    _value = value;
    _trueAtom = trueatom;
  }

  public int queryMinimum() {
    return _value;
  }

  public int queryMaximum() {
    return _value;
  }

  public RangeInteger setPracticalBounds(int newmin, int newmax) {
    return this;
  }

  public Atom queryGeqAtom(int i) {
    if (_value >= i) return _trueAtom;
    else return _trueAtom.negate();
  }

  public void addWelldefinednessClauses(ClauseCollection col) {
    // nothing to add
  }

  public String toString() {
    return "" + _value;
  }
}

